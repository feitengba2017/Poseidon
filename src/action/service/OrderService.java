package action.service;

import cache.AioTcpCache;
import cache.BaseCache;
import cache.ResultPoor;
import com.alibaba.fastjson.JSONObject;
import common.PropertiesConf;
import org.apache.commons.httpclient.protocol.Protocol;
import utils.HttpRequest;
import utils.MD5;
import utils.MySSLSocketFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class OrderService extends BaseService{
	
    /** 
     * 生成订单编号 
     * @return 
     */  
    public static String getOrderNo(String useId) {  
    	  long currentTimeMillis = System.currentTimeMillis();
    	  Random random = new Random();
    	  int rannum = (int) (random.nextDouble() * (99999 - 10000 + 1)) + 10000;// 获取5位随机数
    	  return  String.valueOf(rannum)+String.valueOf(currentTimeMillis)+useId.substring(0,1);// 当前时间
    }  
    
    /** 
     * 创建订单 
     * @return 
     */  
    public static int createOrder(String skuId,String useId,String deliveryAddress,String consignee,String consigneeTel,
    		String orderNo,String dataSource,String orderType,String skuNum,String createdDate,String remark,String paymentWaykey,int marketPrice,int originalPrice) { 
    	//新建订单 待付款状态
    	String states = "101";
    	int sid = sendObjectCreate(461, skuId, useId, deliveryAddress, consignee, consigneeTel,orderNo, dataSource, orderType, skuNum,skuNum , marketPrice, createdDate,states,remark,paymentWaykey,marketPrice,originalPrice);
		String result_str = ResultPoor.getResult(sid);
		JSONObject result_json = JSONObject.parseObject(result_str);
		int ids = result_json.getInteger("success");
		return ids;	
    }  
    
    /** 
     * 补全创建订单的一些状态 分销用哦
     * @return 
     */  
    public static String updateOrderDate(String orderNo) { 
    	int sid = sendObjectCreate(191, orderNo);
		String result_str = ResultPoor.getResult(sid);
		return result_str;	
    }  
    
    /** 
     * 再次创建订单	创建订单的一些状态 分销用哦
     * String deliveryAddress,String consignee,String consigneeTel
     * @return 
     */  
    public static String updateGoOrderDate(String orderNo,String deliveryAddress,String consignee,String consigneeTel,String skuNumber,String totalPrice,String remark,int marketPrice, int originalPrice) { 
    	int sid = sendObjectCreate(510,deliveryAddress,consignee,consigneeTel,skuNumber,totalPrice,remark,marketPrice,originalPrice,orderNo);
		String result_str = ResultPoor.getResult(sid);
		return result_str;	
    } 
    
    /** 
     * 取消订单   取消订单需要做得三件是工作:1，改变订单状态;2，退回库存;3，修改交易表状态
     * @return 
     */  
    public static String cancelOrder(String orderNo,String remark) {  	
    	//生成当前时间
		String nowDate = BaseCache.getTIME();
		
    	String sql1 = "UPDATE uranus.b_order set status = ?, remark = ?, cancel_date = ? WHERE order_no = ?";
        String sql2 = "UPDATE uranus.b_sku a,(select sku_id,sku_num from uranus.b_order where order_no = ?) AS b SET a.stock = b.sku_num + a.stock WHERE a.id = b.sku_id";
        String sql3 = "UPDATE uranus.b_transactions a,(select transaction_no from uranus.b_transactions where order_no = ?) AS b SET a.state = ? WHERE a.transaction_no = b.transaction_no";
    	//事物操作，同时执行多张表 	110
    	ArrayList<Object> ay = new ArrayList<Object>();
    	//1，改变订单状态
    	ay = addTransJa(ay, sql1, "110", remark, nowDate, orderNo);
    	//2，退回库存;
    	ay = addTransJa(ay, sql2, orderNo);
    	//3，修改交易表状态
    	ay = addTransJa(ay, sql3, orderNo,"202");
    	int sid = sendTransJa(ay);	
    	String returnStr = ResultPoor.getResult(sid);
		return returnStr;
    }
    
    /** 
     * 用户完成收货
     * @return 
     */  
    public static String complteOrder(String orderNo) {  	
    	int sid = sendObjectCreate(505, orderNo);
    	String returnStr = ResultPoor.getResult(sid);
		return returnStr;
    }
    
    /** 
     * 根据订单编号获取订单金额
     * @return 
     */  
    public static String findOrderByNo(String orderNo) {  
    	int sid = sendObject(176, orderNo);
		return ResultPoor.getResult(sid);			
    } 
    
    /** 
     * 获取订单列表		待支付 101;待收货 103,104;已收货 107;已取消 110
     * @return 
     */  
    public static String findOrderList(String useId,String status,Integer beginNum,Integer endNum) {  
    	int orderListId;
    	//String sql = "GROUP BY o.order_no ORDER BY o.created_date desc";
        String sql = null;
    	// 分页查询sql
		if(status != null && !status.equals("")) {
		    if(status.equals("101")){
		       sql  =" and o.status = 101 GROUP BY o.order_no ORDER BY o.created_date desc";
            }else if(status.equals("102")){
                sql = " and o.status in (102,103,104,105,106) GROUP BY o.order_no ORDER BY o.created_date desc";
            }else if(status.equals("107")){
                sql = " and o.status in (107,108) GROUP BY o.order_no ORDER BY o.created_date desc";
            }else if(status.equals("110")){
                sql = " and o.status in (110,109,111,112,113) GROUP BY o.order_no ORDER BY o.created_date desc";
            }
			//sql =  " and o.status = ? " + sql  ;
			//orderListId = sendObject(AioTcpCache.ctc,177,sql,PropertiesConf.HESTIA_URL,useId,status,beginNum,endNum);
            orderListId = sendObject(AioTcpCache.ctc,177,sql,PropertiesConf.HESTIA_URL,useId,beginNum,endNum);
		}else {
            String sql1 = "GROUP BY o.order_no ORDER BY o.created_date desc";
			orderListId = sendObject(AioTcpCache.ctc,177,sql1,PropertiesConf.HESTIA_URL,useId,beginNum,endNum);
		}
		return ResultPoor.getResult(orderListId);  	
    }

	//待支付 101;待收货 103,104;已收货 107;已取消 110
    public static String findPddOrderList(String useId,String status,Integer beginNum,Integer endNum){
		int pddOrderListId;
		String sql = null;
		if(status != null && !status.equals("")) {
			if(status.equals("101")){
				sql  =" and p.order_status = -1 GROUP BY p.order_sn ORDER BY p.order_create_time desc";
			}else if(status.equals("102")){
				sql  =" and p.order_status in (0,1) GROUP BY p.order_sn ORDER BY p.order_create_time desc";
			}else if(status.equals("107")){
				sql  =" and p.order_status = 2 GROUP BY p.order_sn ORDER BY p.order_create_time desc";
			}else if(status.equals("110")){
				sql  =" and p.order_status = 4 GROUP BY p.order_sn ORDER BY p.order_create_time desc";
			}
			pddOrderListId = sendObject(AioTcpCache.ctc,739,sql,useId,beginNum,endNum);
		}else {
			String sql1 = " GROUP BY p.order_sn ORDER BY p.order_create_time desc";
			pddOrderListId = sendObject(AioTcpCache.ctc,739,sql1,useId,beginNum,endNum);
		}
    	return ResultPoor.getResult(pddOrderListId);
	}
    
    /** 
     * 补全创建订单的一些状态 分销用哦
     * @return 
     */  
    public static String updateOrderFinance(String financeId,String orderNo) { 
    	int sid = sendObjectCreate(195, financeId,orderNo);
		String result_str = ResultPoor.getResult(sid);
		return result_str;	
    }

	/**
	 * @author cuiw
	 * @Date 2018/6/25
	 * @param expressType 快递公司
	 * @param expressNo 快递单号
	 * @return
	 */
	public static String sendPostLogistic(String expressType,String expressNo) {
		String param = "{\"com\":\"" + expressType + "\",\"num\":\"" + expressNo + "\"}";
		String customer = PropertiesConf.CUSTOMERID;
		String key = PropertiesConf.LOGISTIC_KEY;
		String logistic_normal = PropertiesConf.LOGISTIC_NORMAL;
		//增加下面两行代码 不添加会报错SSL认证缺失
		Protocol myhttps = new Protocol("https", new MySSLSocketFactory(), 443);
		Protocol.registerProtocol("https", myhttps);

		String sign = MD5.encode(param + key + customer);
		HashMap params = new HashMap();
		params.put("param", param);
		params.put("sign", sign);
		params.put("customer", customer);
		String resp = null;
		try {
			resp = new HttpRequest().postData(logistic_normal, params, "utf-8").toString();
			System.out.println(resp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;

		
	}

	public static int saveZhangDZOrder(String orderNo, String userId,String phone, String goodsName,String skuid,String imgPath, String remark,String payTime) {
		//生成当前时间
		String createdDate = BaseCache.getTIME();
		String  memberSelfMoney = null;
		if(!"".equals(skuid) || !"".equals(goodsName)){
			if(!"".equals(skuid)) {
				String sku = findSkuById(skuid);
				memberSelfMoney = (String) getFieldValue(sku, "bounty", String.class);
				if ("".equals(memberSelfMoney) || null == memberSelfMoney) {
					memberSelfMoney = "0";
				}
			}else if (!"".equals(goodsName)) {
				String skubyName = findSkubyName(goodsName);
				memberSelfMoney = (String) getFieldValue(skubyName, "bounty", String.class);
				skuid = (String) getFieldValue(skubyName, "id", String.class);
				goodsName = (String) getFieldValue(skubyName, "sku_name", String.class);
				if(memberSelfMoney == null || skuid == null || goodsName == null){
					memberSelfMoney = "0";
					skuid = "0";
					goodsName = "";
				}
			}
		}else {
			memberSelfMoney = "0";
			skuid = "0";
			goodsName = "";
		}
		if(payTime == null){
			payTime = "";
		}
		int sid = sendObjectCreate(608, orderNo,userId, phone,goodsName,skuid,memberSelfMoney,imgPath, createdDate, remark,payTime);
		String result = ResultPoor.getResult(sid);
		JSONObject result_json = JSONObject.parseObject(result);
		int ids = result_json.getInteger("success");
		return ids;
	}


	public static String  findZhangDZOrderByOrderNo(String orderNo){
		int sid = sendObject(611, orderNo);
		return ResultPoor.getResult(sid);
	}

	public static String findSkuById(String skuId){
		int sid = sendObject(613, skuId);
		return ResultPoor.getResult(sid);
	}
	public static String findSkubyName(String goodsName){
		String sql = " and spu_name like '%"+goodsName+"%'";
		int i = sendObjectBase(627, sql);
		return ResultPoor.getResult(i);
	}

	/**
	 * 判断此SKU规格是否参与了当前秒杀时段
	 * @param skuId
	 * @return res
	 */
	public static String getSeckillGoodsPrice(String skuId){
		String currentTime = BaseCache.getTIME();
		int sid = sendObject(460, skuId, currentTime, currentTime);
		String res =  ResultPoor.getResult(sid);
		String seckillPrice = (String) OrderService.getFieldValue(res, "seckill_price", String.class);
		return seckillPrice;
	}


	//订单佣金
	public static String getPddCommissionTest(String useId,String dayTime) {

		int sid =  sendObject(740,useId,useId,useId,useId,useId,useId,useId,dayTime);
		return ResultPoor.getResult(sid);
	}

	//每月订单佣金
	public static String getMonthPddCommission(String useId) {

		int sid =  sendObject(741,useId,useId,useId,useId,useId,useId);
		return ResultPoor.getResult(sid);
	}

}