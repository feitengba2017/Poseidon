package action;

import action.service.*;
import cache.BaseCache;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import constant.RspCode;
import servlet.BaseServlet;
import utils.StringUtil;
import utils.UploadImageUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.*;

import static action.WechatNotityAction.getFieldValue;

@WebServlet(name = "Order", urlPatterns = "/order")
public class OrderAction extends BaseServlet {
    private static final long serialVersionUID = 1L;

    /**
     * 创建订单
     *
     * @param skuId
     * @param token
     * @param deliveryAddress
     * @param consignee
     * @param consigneeTel
     * @param dataSource
     * @param orderType       会过，自营，礼包
     * @param skuNumber
     * @param remark
     * @param paymentWaykey
     * @param jsCode
     * @param transactionBody
     * @param spbillCreateIp
     * @param sceneInfo
     * @param deviceInfo
     * @return
     * @throws Exception
     */
    public static String createOrder(String skuId, String token, String deliveryAddress, String consignee, String consigneeTel,
                                     String dataSource, String orderType, String skuNumber, String remark, String paymentWaykey, String jsCode, String transactionBody, String spbillCreateIp, String sceneInfo, String deviceInfo) throws Exception {

        String useId = UserService.getUserIdByToken(token);
        if (useId == null) {
            return creatResult(0, "亲，未登录....", null).toString();
        }

        //判断商品库存   订单表里存useId
        String result_str = GoodService.getGoodsBySkuId(skuId);
        //String userPurchase_str = GoodService.userPurchase(useId,skuId);
        JSONObject resultArray = JSONObject.parseObject(result_str).getJSONObject("result").getJSONArray("rs").getJSONObject(0);
        int stock = Integer.parseInt(resultArray.getString("stock"));
        int buyconfine = Integer.parseInt(resultArray.getString("buyconfine"));
        int skuStatus = Integer.parseInt(resultArray.getString("sku_status"));
        //由于运营修改商品信息后的造成的订单和商品价格存在不时效的不同步问题
        int marketPrice = Integer.parseInt(resultArray.getString("market_price"));
        /**
         * 判断SKU规格是否参加了进行中的秒杀时段 获取秒杀价格 进行下单金额计算
         */
        String seckillPrice = OrderService.getSeckillGoodsPrice(skuId);
        System.out.println(null != seckillPrice);
        if (null == seckillPrice || "".equals(seckillPrice) || "0".equals(seckillPrice)) {

        } else {
            try {
                marketPrice = Integer.parseInt(seckillPrice);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        int originalPrice = Integer.parseInt(resultArray.getString("original_price"));
        //JSONObject userPurchase_json = JSONObject.parseObject(userPurchase_str);
        //取消用户限购
//			int size= userPurchase_json.getJSONObject("result").getJSONArray("rs").size();	
//			if(size != 0){
//				return creatResult(5, "亲，该用户限购了", null).toString();
//			}
        if (skuStatus == 0) {
            return creatResult(2, "亲，商品下架了", null).toString();
        }
        if (buyconfine != 0 && Integer.parseInt(skuNumber) > buyconfine) {
            return creatResult(3, "亲,限购买数量了", null).toString();
        }
        if (stock < Integer.parseInt(skuNumber)) {
            return creatResult(4, "亲,库存不足", null).toString();
        }

        //生成订单号
        String orderNo = OrderService.getOrderNo(useId);
        //生成当前时间
        String createdDate = BaseCache.getTIME();
        int rps_code = OrderService.createOrder(skuId, useId, deliveryAddress, consignee, consigneeTel, orderNo, dataSource, orderType, skuNumber, createdDate, remark, paymentWaykey, marketPrice, originalPrice);
        if (rps_code != 1) {
            return creatResult(6, "亲，系统忙，请稍等......", null).toString();
        }

        //为了优化下单速度，先把创建订单和预支付环境整合到一起
        //微信预支付金额不能前台传，只能后台获取
        String resultStr = OrderService.findOrderByNo(orderNo);
        JSONObject resultJson = JSONObject.parseObject(resultStr);
        String totalPrice = resultJson.getJSONObject("result").getJSONArray("rs").getJSONObject(0).getString("total_price");
        //获取财务流水号
        String transactionNo = FinanceService.getTradeNo(useId);

        //订单支付整合小程序支付和webapp支付
        HashMap<String, Object> wechatPayInfoMap = null;
        if (paymentWaykey.equals("WetCat")) {
            wechatPayInfoMap = WechatPayService.miniWetCatOrderCreate(jsCode, transactionBody, transactionNo, totalPrice);
        } else {
            wechatPayInfoMap = WechatPayService.winFruitWechatH5Pay(transactionBody, transactionNo, totalPrice, spbillCreateIp, sceneInfo, deviceInfo);
        }
        //与微信平台交互生成prePayId
        if (wechatPayInfoMap == null) {
            return creatResult(1, RspCode.WECHAT_ERROR, null).toString();
        }
        //定义微信支付
        //交易表插入交易记录,将其交易id更新到订单表（便于财务对账）
        String financeStr = FinanceService.createFinance(useId, transactionNo, transactionBody, orderNo, totalPrice, paymentWaykey);
        JSONObject financeJson = JSONObject.parseObject(financeStr);
        String financeId = financeJson.getJSONObject("result").getJSONArray("ids").get(0).toString();
        //由于下单单线流程过于复杂，现在考虑用线程异步的方式复杂的数据库操作
        new Thread() {
            public void run() {
                //补全订单计算佣金状态 	  补全订单状态的过程中,存在无父级情况下,父级和顶级会员级别和会员级别和id不可以避免为空的情况,故留到算佣金时处理
                OrderService.updateOrderDate(orderNo);
                //将其交易id及其流水号更新到订单表（便于财务对账）
                OrderService.updateOrderFinance(financeId, orderNo);
            }

            ;
        }.start();

        return creatResult(7, "", wechatPayInfoMap).toString();
    }


    /**
     * 订单列表去支付时调用,首次创建订单预支付环节已经整合到创建订单逻辑里了
     *
     * @param orderNo
     * @param skuId
     * @param token
     * @param deliveryAddress
     * @param consignee
     * @param consigneeTel
     * @param skuNumber
     * @param remark
     * @param jsCode
     * @param transactionBody
     * @return
     * @throws Exception
     */
    public static String repOrderCreate(String orderNo, String skuId, String token, String deliveryAddress, String consignee, String consigneeTel, String skuNumber, String remark, String jsCode, String transactionBody, String spbillCreateIp, String sceneInfo, String deviceInfo, String paymentWaykey) throws Exception {

        String useId = UserService.getUserIdByToken(token);
        if (useId == null) {
            return creatResult(0, "亲，未登录....", null).toString();
        }

        //判断商品库存   订单表里存useId
        String result_str = GoodService.getGoodsBySkuId(skuId);
        JSONObject result_json = JSONObject.parseObject(result_str);
        int stock = Integer.parseInt(result_json.getJSONObject("result").getJSONArray("rs").getJSONObject(0).getString("stock"));
        int buyconfine = Integer.parseInt(result_json.getJSONObject("result").getJSONArray("rs").getJSONObject(0).getString("buyconfine"));
        int skuStatus = Integer.parseInt(result_json.getJSONObject("result").getJSONArray("rs").getJSONObject(0).getString("sku_status"));
        //由于运营修改商品信息后的造成的订单和商品价格存在不时效的不同步问题
        //int marketPrice = Integer.parseInt(result_json.getJSONObject("result").getJSONArray("rs").getJSONObject(0).getString("market_price"));
        //int originalPrice = Integer.parseInt(result_json.getJSONObject("result").getJSONArray("rs").getJSONObject(0).getString("original_price"));

        if (skuStatus == 0) {
            return creatResult(2, "亲，商品下架了", null).toString();
        }
        if (buyconfine != 0 && Integer.parseInt(skuNumber) > buyconfine) {
            return creatResult(3, "亲,限购买数量了", null).toString();
        }
        if (stock < Integer.parseInt(skuNumber)) {
            return creatResult(4, "亲,库存不足", null).toString();
        }
        //计算订单支付总价  获取交易商户流水号
        //int skuNum =  Integer.valueOf(skuNumber);
        //String totalPrice =String.valueOf(marketPrice*skuNum);
        //String orderByNo = OrderService.findOrderByNo(orderNo);
        String resultStr = OrderService.findOrderByNo(orderNo);
        JSONObject resultJson = JSONObject.parseObject(resultStr);
        String totalPrice = resultJson.getJSONObject("result").getJSONArray("rs").getJSONObject(0).getString("total_price");
        String originalPrice = resultJson.getJSONObject("result").getJSONArray("rs").getJSONObject(0).getString("original_price");
        String marketPrice = resultJson.getJSONObject("result").getJSONArray("rs").getJSONObject(0).getString("market_price");
        String transactionNo = resultJson.getJSONObject("result").getJSONArray("rs").getJSONObject(0).getString("transaction_no");
        String transaction_id = resultJson.getJSONObject("result").getJSONArray("rs").getJSONObject(0).getString("transaction_id");
        //获取财务流水号
        //String transactionNo = FinanceService.getTradeNo(useId);

        //订单支付整合小程序支付和webapp支付
        HashMap<String, Object> wechatPayInfoMap = null;
        if (paymentWaykey.equals("WetCat")) {
            wechatPayInfoMap = WechatPayService.miniWetCatOrderCreate(jsCode, transactionBody, transactionNo, totalPrice);
        } else {
            wechatPayInfoMap = WechatPayService.winFruitWechatH5Pay(transactionBody, transactionNo, totalPrice, spbillCreateIp, sceneInfo, deviceInfo);
        }
        if (wechatPayInfoMap == null) {
            return creatResult(1, RspCode.WECHAT_ERROR, null).toString();
        }

        //String financeStr = FinanceService.createFinance(useId,transactionNo,transactionBody, orderNo,totalPrice,paymentWaykey);
        //JSONObject financeJson = JSONObject.parseObject(financeStr);
        //String financeId = financeJson.getJSONObject("result").getJSONArray("ids").get(0).toString();

        //由于下单单线流程过于复杂，现在考虑用线程异步的方式复杂的数据库操作   再去支付由于存在时间的对应性,所以需要再次更新各个佣金的计算状态
        new Thread() {
            public void run() {
                //补全订单计算佣金状态 	  补全订单状态的过程中,存在无父级情况下,父级和顶级会员级别和会员级别和id不可以避免为空的情况,故留到算佣金时处理,一些的订单修改操作
                OrderService.updateGoOrderDate(deliveryAddress, consignee, consigneeTel, skuNumber, totalPrice, remark, orderNo, Integer.parseInt(originalPrice), Integer.parseInt(marketPrice));
                //将其交易id及其流水号更新到订单表（便于财务对账）
                OrderService.updateOrderFinance(transaction_id, orderNo);
            }

            ;
        }.start();

        return creatResult(1, "", wechatPayInfoMap).toString();
    }


    /**
     * 取消订单
     *
     * @param orderNo
     * @param token
     * @return
     */
    public static String cancelOrder(String token, String orderNo, String remark) {

        //判断商品库存   订单表里存useId
        String useId = UserService.getUserIdByToken(token);
        if (useId == null) {
            return creatResult(0, "亲，未登录....", null).toString();
        }
        OrderService.cancelOrder(orderNo, remark);

        //取消订单需要做得三件是工作:1，改变订单状态;2，退回库存;3，修改交易表状态
        return creatResult(1, "亲,不要走...", null).toString();
    }


    /**
     * 用户订单确认收货
     *
     * @param orderNo
     * @param token
     * @return
     */
    public static String complteOrder(String token, String orderNo) {

        //判断商品库存   订单表里存useId
        String useId = UserService.getUserIdByToken(token);
        if (useId == null) {
            return creatResult(0, "亲，未登录....", null).toString();
        }
        OrderService.complteOrder(orderNo);

        //取消订单需要做得三件是工作:1，改变订单状态;2，退回库存;3，修改交易表状态
        return creatResult(1, "亲,订单完成喽...", null).toString();
    }


    /**
     * 根据不同状态查询订单列表
     *
     * @param
     * @param token
     * @param status
     * @return
     */
    public static String findOrderList(String token, String status, String begin, String end) throws ParseException{
        String useId = UserService.getUserIdByToken(token);
        if (useId == null) {
            return creatResult(2, "亲，未登录....", null).toString();
        }
        //mysql limit的起始索引为0,便于前台理解，所以用此方式
        Integer beginNum = Integer.valueOf(begin) - 1;
        Integer endNum = Integer.valueOf(end);
        String OrderListStr = OrderService.findOrderList(useId, status, beginNum, endNum);
        JSONObject OrderList = JSONObject.parseObject(OrderListStr);
        int orderListSize = OrderList.getJSONObject("result").getJSONArray("rs").size();

        String pddOrderList = OrderService.findPddOrderList(useId, status, beginNum, endNum);
        JSONObject pddOrderListJson = JSONObject.parseObject(pddOrderList);
        int pddOrderListsize = pddOrderListJson.getJSONObject("result").getJSONArray("rs").size();

        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();

        if(orderListSize !=0 && pddOrderListsize == 0){
            for (int i=0;i<orderListSize;i++){

                HashMap<String, Object> map = new HashMap<String, Object>();

                JSONObject jsonObject = OrderList.getJSONObject("result").getJSONArray("rs").getJSONObject(i);
                String one_buyer_id = jsonObject.getString("buyer_id");
                String one_order_no = jsonObject.getString("order_no");
                String one_status = jsonObject.getString("status");
                String one_created_date = jsonObject.getString("created_date");
                String one_image = jsonObject.getString("image");
                String one_total_price = jsonObject.getString("total_price");
                String one_sku_num = jsonObject.getString("sku_num");
                String one_sku_id = jsonObject.getString("sku_id");
                String one_sku_name = jsonObject.getString("sku_name");
                String one_order_type = jsonObject.getString("order_type");
                String one_delivery_address = jsonObject.getString("delivery_address");
                String one_consignee_tel = jsonObject.getString("consignee_tel");
                String one_consignee = jsonObject.getString("consignee");
                String one_logistics_numbers = jsonObject.getString("logistics_numbers");
                String one_express_company = jsonObject.getString("express_company");
                String one_backMoney = jsonObject.getString("backMoney");
                String one_spu_attribute = jsonObject.getString("spu_attribute");

                map.put("buyer_id",one_buyer_id);
                map.put("order_no",one_order_no);
                map.put("status",one_status);
                map.put("created_date",one_created_date);
                map.put("image",one_image);
                map.put("total_price",one_total_price);
                map.put("sku_num",one_sku_num);
                map.put("sku_id",one_sku_id);
                map.put("sku_name",one_sku_name);
                map.put("order_type",one_order_type);
                map.put("delivery_address",one_delivery_address);
                map.put("consignee_tel",one_consignee_tel);
                map.put("consignee",one_consignee);
                map.put("logistics_numbers",one_logistics_numbers);
                map.put("express_company",one_express_company);
                map.put("backMoney",one_backMoney);
                map.put("spu_attribute",one_spu_attribute);
                list.add(map);

            }
        }
        if(orderListSize ==0 && pddOrderListsize != 0){
            for (int y=0;y<pddOrderListsize;y++){
                HashMap<String, Object> pddMap = new HashMap<String, Object>();

                JSONObject jsonObject = pddOrderListJson.getJSONObject("result").getJSONArray("rs").getJSONObject(y);
                String p_buyer_id = jsonObject.getString("buyer_id");
                String p_order_no = jsonObject.getString("order_no");
                String p_status = jsonObject.getString("status");
                String p_created_date = jsonObject.getString("created_date");
                String p_image = jsonObject.getString("image");
                String p_total_price = jsonObject.getString("total_price");
                String p_sku_num = jsonObject.getString("sku_num");
                String p_sku_id = jsonObject.getString("sku_id");
                String p_sku_name = jsonObject.getString("sku_name");
                String p_phone = jsonObject.getString("phone");
                String p_order_type = jsonObject.getString("order_type");

                long l = Long.valueOf(p_created_date) * 1000;
                String pddTime = StringUtil.timeStamp2Date(String.valueOf(l)).substring(2);

                pddMap.put("buyer_id",p_buyer_id);
                pddMap.put("order_no",p_order_no);
                pddMap.put("status",p_status);
                pddMap.put("created_date",pddTime);
                pddMap.put("image",p_image);
                pddMap.put("total_price",p_total_price);
                pddMap.put("sku_num",p_sku_num);
                pddMap.put("sku_id",p_sku_id);
                pddMap.put("sku_name",p_sku_name);
                pddMap.put("order_type",p_order_type);
                pddMap.put("delivery_address","");
                pddMap.put("consignee_tel",p_phone);
                pddMap.put("consignee","");
                pddMap.put("logistics_numbers","");
                pddMap.put("express_company","");
                pddMap.put("backMoney","");
                pddMap.put("spu_attribute","");

                list.add(pddMap);
            }
        }

        if(orderListSize !=0 && pddOrderListsize != 0){
            for (int n=0;n<orderListSize;n++){

                HashMap<String, Object> orderMap = new HashMap<String, Object>();

                JSONObject jsonObject = OrderList.getJSONObject("result").getJSONArray("rs").getJSONObject(n);
                String order_buyer_id = jsonObject.getString("buyer_id");
                String order_order_no = jsonObject.getString("order_no");
                String order_status = jsonObject.getString("status");
                String order_created_date = jsonObject.getString("created_date");
                String order_image = jsonObject.getString("image");
                String order_total_price = jsonObject.getString("total_price");
                String order_sku_num = jsonObject.getString("sku_num");
                String order_sku_id = jsonObject.getString("sku_id");
                String order_sku_name = jsonObject.getString("sku_name");
                String order_order_type = jsonObject.getString("order_type");
                String order_delivery_address = jsonObject.getString("delivery_address");
                String order_consignee_tel = jsonObject.getString("consignee_tel");
                String order_consignee = jsonObject.getString("consignee");
                String order_logistics_numbers = jsonObject.getString("logistics_numbers");
                String order_express_company = jsonObject.getString("express_company");
                String order_backMoney = jsonObject.getString("backMoney");
                String order_spu_attribute = jsonObject.getString("spu_attribute");

                orderMap.put("buyer_id",order_buyer_id);
                orderMap.put("order_no",order_order_no);
                orderMap.put("status",order_status);
                orderMap.put("created_date",order_created_date);
                orderMap.put("image",order_image);
                orderMap.put("total_price",order_total_price);
                orderMap.put("sku_num",order_sku_num);
                orderMap.put("sku_id",order_sku_id);
                orderMap.put("sku_name",order_sku_name);
                orderMap.put("order_type",order_order_type);
                orderMap.put("delivery_address",order_delivery_address);
                orderMap.put("consignee_tel",order_consignee_tel);
                orderMap.put("consignee",order_consignee);
                orderMap.put("logistics_numbers",order_logistics_numbers);
                orderMap.put("express_company",order_express_company);
                orderMap.put("backMoney",order_backMoney);
                orderMap.put("spu_attribute",order_spu_attribute);

                list.add(orderMap);

            }
            for (int m=0;m<pddOrderListsize;m++){

                HashMap<String, Object> pddOrderMap = new HashMap<String, Object>();

                JSONObject jsonObject = pddOrderListJson.getJSONObject("result").getJSONArray("rs").getJSONObject(m);
                String pdd_buyer_id = jsonObject.getString("buyer_id");
                String pdd_order_no = jsonObject.getString("order_no");
                String pdd_status = jsonObject.getString("status");
                String pdd_created_date = jsonObject.getString("created_date");
                String pdd_image = jsonObject.getString("image");
                String pdd_total_price = jsonObject.getString("total_price");
                String pdd_sku_num = jsonObject.getString("sku_num");
                String pdd_sku_id = jsonObject.getString("sku_id");
                String pdd_sku_name = jsonObject.getString("sku_name");
                String pdd_phone = jsonObject.getString("phone");
                String pdd_order_type = jsonObject.getString("order_type");

                long l = Long.valueOf(pdd_created_date) * 1000;
                String time = StringUtil.timeStamp2Date(String.valueOf(l)).substring(2);

                pddOrderMap.put("buyer_id",pdd_buyer_id);
                pddOrderMap.put("order_no",pdd_order_no);
                pddOrderMap.put("status",pdd_status);
                pddOrderMap.put("created_date",time);
                pddOrderMap.put("image",pdd_image);
                pddOrderMap.put("total_price",pdd_total_price);
                pddOrderMap.put("sku_num",pdd_sku_num);
                pddOrderMap.put("sku_id",pdd_sku_id);
                pddOrderMap.put("sku_name",pdd_sku_name);
                pddOrderMap.put("order_type",pdd_order_type);
                pddOrderMap.put("delivery_address","");
                pddOrderMap.put("consignee_tel",pdd_phone);
                pddOrderMap.put("consignee","");
                pddOrderMap.put("logistics_numbers","");
                pddOrderMap.put("express_company","");
                pddOrderMap.put("backMoney","");
                pddOrderMap.put("spu_attribute","");

                list.add(pddOrderMap);

            }

        }

        Collections.sort(list, (o1, o2) -> {
            String name1 = (String) o1.get("created_date");
            String name2 = (String) o2.get("created_date");
            return name2.compareTo(name1);
        });

        HashMap<String,Object> resMap = new HashMap<String,Object>();
        resMap.put("result", list);
        String smap = JSON.toJSONString(resMap, SerializerFeature.DisableCircularReferenceDetect);
        System.out.println(smap);
        return smap;
    }

    /**
     * 根据快递公司编号和运单编号查询快递信息
     *
     * @param expressType 快递公司类型 {@link String}
     * @param expressNo   快递单号 {@link String}
     * @return 快递100返回的json数据 {@link String}
     */
    public static String execLookKuaiDi(String expressType, String expressNo) throws UnsupportedEncodingException {
        //汉字转化成拼音
        //String com = PinYinUtil.getPingYin(expressType);
        //String expressInfo = kuaidi100.getExpressInfo(expressType, expressNo);
        String _expressType = URLEncoder.encode(expressType, "utf-8");
        String _expressNo = URLEncoder.encode(expressNo, "utf-8");
        String expressInfo = OrderService.sendPostLogistic(_expressType, _expressNo);
        return expressInfo;
    }

    /**
     * 保存掌达赚下单记录   有截图上传
     *
     * @return
     */
    public static String addZhangDZOrderImg(HttpServletRequest request) {
        //上传订单图片
        Map<String, String> map = UploadImageUtil.uploadImg(request);
			/*if(map.get("orderNo") == null || "".equals(map.get("orderNo"))){
				return creatResult(3, "订单号和商品都不能为空哦！", null).toString();
			}*/
        String zhangDZOrderByOrderNo = OrderService.findZhangDZOrderByOrderNo(map.get("orderNo"));
        String orderNo1 = (String) getFieldValue(zhangDZOrderByOrderNo, "order_no", String.class);
        if (orderNo1 != null) {
            return creatResult(2, "该订单号已存在！", null).toString();
        }
        String user = UserService.getUserByToken(map.get("token"));
        if (user == null) {
            return creatResult(0, "亲，未登录....", null).toString();
        }
        String phone = (String) getFieldValue(user, "phone", String.class);
        String userId = (String) getFieldValue(user, "id", String.class);
        int i = OrderService.saveZhangDZOrder(map.get("orderNo"), userId, phone, map.get("goodsName"), map.get("skuId"), map.get("imagePath"), map.get("remark"), map.get("payTime"));
        if (i != 1) {
            return creatResult(6, "亲，系统忙，请稍等......", null).toString();
        } else {
            return creatResult(1, "提交成功", null).toString();
        }
    }

    /**
     * 保存掌达赚下单记录   无截图上传
     *
     * @param token
     * @param orderNo
     * @param goodsName
     * @param remark
     * @return
     */
    public static String addZhangDZOrder(String token, String orderNo, String goodsName, String skuid, String remark) {

		/*if(orderNo == null || "".equals(orderNo)){
			return creatResult(3, "订单号不能为空哦！", null).toString();
		}*/
        String zhangDZOrderByOrderNo = OrderService.findZhangDZOrderByOrderNo(orderNo);
        String orderNo1 = (String) getFieldValue(zhangDZOrderByOrderNo, "order_no", String.class);
        if (orderNo1 != null) {
            return creatResult(2, "该订单号已存在！", null).toString();
        }
        String user = UserService.getUserByToken(token);
        if (user == null) {
            return creatResult(0, "亲，未登录....", null).toString();
        }
        String phone = (String) getFieldValue(user, "phone", String.class);
        String userId = (String) getFieldValue(user, "id", String.class);
        int i = OrderService.saveZhangDZOrder(orderNo, userId, phone, goodsName, skuid, "", remark, "");
        if (i != 1) {
            return creatResult(6, "亲，系统忙，请稍等......", null).toString();
        } else {
            return creatResult(1, "提交成功", null).toString();
        }
    }


    //所有订单佣金
    public static String getPddCommissionTest(String token,String dayTime){
        String userId = UserService.getUserIdByToken(token);
        if (userId == null) {
            return creatResult(0, "亲，未登录....", null).toString();
        }
        String dayPddCommission = OrderService.getPddCommissionTest(userId,dayTime);
        JSONObject result = JSONObject.parseObject(dayPddCommission);
        HashMap<String, Object> resMap = new HashMap<String, Object>();
        resMap.put("result", result);
        return creatResult(1, "亲,数据包回来了哦...", resMap).toString();
    }

    //每月订单佣金
    public static String getMonthPddCommission(String token){
        String userId = UserService.getUserIdByToken(token);
        if (userId == null) {
            return creatResult(0, "亲，未登录....", null).toString();
        }
        String monthPddCommission = OrderService.getMonthPddCommission(userId);
        JSONObject result = JSONObject.parseObject(monthPddCommission);
        HashMap<String, Object> resMap = new HashMap<String, Object>();
        resMap.put("result", result);
        return creatResult(1, "亲,数据包回来了哦...", resMap).toString();
    }
}