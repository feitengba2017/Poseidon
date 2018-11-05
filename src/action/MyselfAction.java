package action;

import action.service.MyselfService;
import action.service.UserService;
import action.service.WalletService;
import action.service.WechatPayService;
import cache.BaseCache;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import common.PropertiesConf;
import servlet.BaseServlet;
import utils.WXCore;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.annotation.WebServlet;
import java.security.spec.AlgorithmParameterSpec;
import java.util.*;


@WebServlet(name = "Myself", urlPatterns = "/myself")
public class MyselfAction extends BaseServlet {

	private static final long serialVersionUID = 1L;

	public static String inviteFriends(String token) {
		String useId = UserService.getUserIdByToken(token);
		if (useId == null) {
			return creatResult(2, "亲，未登录....", null).toString();
		}

		//查询用户信息，用户手机号就是邀请码
		JSONArray inviteCode = MyselfService.inviteCode(useId);
		//查询邀请列表信息
		JSONArray inviteResult = MyselfService.inviteFriends(useId);


		HashMap<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("OrderList", inviteResult);
		resMap.put("inviteCode", inviteCode);
		return creatResult(1, "亲，数据包回来了", resMap).toString();
	}

	/**
	 * 我的钱包
	 */
	public static String Mywallet(String token, String nowMonth) {
		String useId = UserService.getUserIdByToken(token);
		if (useId == null) {
			return creatResult(2, "亲，未登录....", null).toString();
		}

		//查询用户信息，用户手机号就是邀请码
		String balanceStr = WalletService.getBalance(useId);
		JSONObject balanceJson = JSONObject.parseObject(balanceStr);
		JSONArray balance = balanceJson.getJSONObject("result").getJSONArray("rs");
		//查询钱包列表信息
		JSONArray wallet = MyselfService.findWalletList(useId, nowMonth);

		HashMap<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("balance", balance);
		resMap.put("wallet", wallet);
		return creatResult(1, "亲，数据包回来了", resMap).toString();
	}

	/**
	 * 我的模块下礼包属性商品列表	sku_id层级的列表
	 */
	public static String findGiftGoodList(String begin, String end) {
		//mysql limit的起始索引为0,便于前台理解，所以用此方式
		Integer beginNum = Integer.valueOf(begin) - 1;
		Integer endNum = Integer.valueOf(end);

		String searchGiftGoodsStr = MyselfService.findGiftGoodList(beginNum, endNum);
		JSONObject searchGiftGoods = JSONObject.parseObject(searchGiftGoodsStr);
		HashMap<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("searchGiftGoods", searchGiftGoods);
		return creatResult(1, "亲,数据包回来了哦...", resMap).toString();
	}

	/**
	 * 我的邀请好友 确定下级接口
	 */
	public static String confirmSupmember(String token, String phone) {
		String useId = UserService.getUserIdByToken(token);
		if (useId == null) {
			return creatResult(2, "亲，未登录....", null).toString();
		}

		Integer hasId = UserService.hasUserByPhone(phone);
		if (hasId == null) {
			return creatResult(3, "邀请用户不存在", null).toString();
		}
		//判断该用户是否已经有了上级
		String userMsgStr = MyselfService.getUserMsg(useId);
		JSONObject userMsg = JSONObject.parseObject(userMsgStr);
		//用户表分类不为空的情况
		String parentUser = userMsg.getJSONObject("result").getJSONArray("rs").getJSONObject(0).getString("parent_user_id");
//		Integer parentUserId = parentUser.endsWith("") ? 0 : Integer.valueOf(parentUser);
//		int parentUserId = Integer.parseInt(parentUser);
		//切记测试邀请人的时候,手动改parentUserId,导致邀请人重复,前后台对不上
		if ( !"".equals(parentUser)){
			return creatResult(5, "该用户已经绑定了邀请人", null).toString();
		}

		//(先判断你是否是我的下级,而我刚好没有上级的情况)
		String findIsNotSonStr = MyselfService.findIsNotSon(useId);
		JSONArray findIsNotSon = JSONObject.parseObject(findIsNotSonStr).getJSONObject("result").getJSONArray("rs");
		for (int i = 0; i < findIsNotSon.size(); i++) {
			if (findIsNotSon.getJSONObject(i).getString("phone").equals(phone)) {
				return creatResult(4, "邀请人是你自己或下级哦", null).toString();
			}
		}

		String confirmSupmemberStr = MyselfService.confirmSupmember(useId, phone);
		JSONObject result = JSONObject.parseObject(confirmSupmemberStr);
		HashMap<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("result", result);
		return creatResult(1, "亲,数据包回来了哦...", resMap).toString();
	}

	/**
	 * 查询我的上级接口
	 */
	public static String findSupmember(String token) {
		String useId = UserService.getUserIdByToken(token);
		if (useId == null) {
			return creatResult(2, "亲，未登录....", null).toString();
		}
		String findSupmemberStr = MyselfService.findSupmember(useId);
		JSONObject result = JSONObject.parseObject(findSupmemberStr);
		JSONArray resultJson = result.getJSONObject("result").getJSONArray("rs");
		if (resultJson.size() == 0) {
			return creatResult(3, "亲,该用户无上级...", null).toString();
		}
		String nickName = resultJson.getJSONObject(0).getString("nick_name");
		String phone = resultJson.getJSONObject(0).getString("phone");
		HashMap<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("nickName", nickName);
		resMap.put("phone", phone);
		return creatResult(1, "亲,数据包回来了哦...", resMap).toString();
	}

	/**
	 * 查询我的列表(用户是会员状态下)
	 */
	public static String findMember(String token) {
		String useId = UserService.getUserIdByToken(token);
		if (useId == null) {
			return creatResult(2, "亲，未登录....", null).toString();
		}
		//生成当前时间
		String nowTime = BaseCache.getTIME();
		String findMemberStr = MyselfService.findMember(useId, nowTime);
		JSONObject result = JSONObject.parseObject(findMemberStr);
		HashMap<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("result", result);
		return creatResult(1, "亲,数据包回来了哦...", resMap).toString();
	}

	/**
	 * 收藏列表（商品详情页商品收藏功能）
	 */
	public static String storeGoods(String token, String spuId) {
		String useId = UserService.getUserIdByToken(token);
		if (useId == null) {
			return creatResult(2, "亲，未登录....", null).toString();
		}
		//生成当前时间
		String nowTime = BaseCache.getTIME();
		String storeGoodsStr = MyselfService.storeGoods(useId, nowTime, spuId);
		JSONObject result = JSONObject.parseObject(storeGoodsStr);
		HashMap<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("result", result);
		return creatResult(1, "亲,数据包回来了哦...", resMap).toString();
	}

	/**
	 * 批量取消收藏
	 */
	public static String unStoreGoodsList(String token, String spuIdStr) {
		String useId = UserService.getUserIdByToken(token);
		if (useId == null) {
			return creatResult(2, "亲，未登录....", null).toString();
		}
		String unStoreGoodsListInt = MyselfService.unStoreGoodsList(useId, spuIdStr);
		JSONObject result = JSONObject.parseObject(unStoreGoodsListInt);
		HashMap<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("result", result);
		return creatResult(1, "亲,数据包回来了哦...", resMap).toString();
	}

	/**
	 * 我的收藏列表
	 */
	public static String findStoreGoodsList(String token, String begin, String end) {
		String useId = UserService.getUserIdByToken(token);
		if (useId == null) {
			return creatResult(2, "亲，未登录....", null).toString();
		}

		//mysql limit的起始索引为0,便于前台理解，所以用此方式
		Integer beginNum = Integer.valueOf(begin) - 1;
		Integer endNum = Integer.valueOf(end);

		String storeGoodsListInt = MyselfService.findStoreGoodsList(useId, beginNum, endNum);
		JSONObject result = JSONObject.parseObject(storeGoodsListInt);
		HashMap<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("result", result);
		return creatResult(1, "亲,数据包回来了哦...", resMap).toString();
	}


	/**
	 * 解密手机号
	 */
	public static String getSignPhone(String encryptedData, String iv, String js_code) throws Exception {
		String sessionId = "";
		if(iv.equals("undefined")){
			return "";
		}
		Map<String, Object> resultMap = WechatPayService.findOpenid(js_code);
		if (resultMap != null) {
			sessionId = resultMap.get("session_key").toString();
		}
		String json = WXCore.decrypt(PropertiesConf.APP_ID, encryptedData, sessionId, iv);
		if (json == "") {
			return "";
		}
		JSONObject parse = (JSONObject) JSONObject.parse(json);
		//appid为机密文件必选在文件中予以排除
		return (String) parse.get("phoneNumber");
	}

	/**
	 * 废弃
	 *
	 * @param key
	 * @param iv
	 * @param encData
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(byte[] key, byte[] iv, byte[] encData) throws Exception {
		AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
		//解析解密后的字符串
		return new String(cipher.doFinal(encData), "UTF-8");
	}

	//获取当月收益列表
	public static String getMonthIncomeList(String token,String monthTime){

		String useId = UserService.getUserIdByToken(token);
		String monthIncomeList = MyselfService.getMonthIncomeList(useId,monthTime);
		System.out.println(monthIncomeList);
		JSONObject getIncomeList = JSONObject.parseObject(monthIncomeList);
		//查询掌大赚收益列表
		String monthIncomeListZDZ = MyselfService.getMonthIncomeListZDZ(useId,monthTime);
		JSONObject getIncomeListZDZ = JSONObject.parseObject(monthIncomeListZDZ);
		int size = getIncomeList.getJSONObject("result").getJSONArray("rs").size();
		int sizeZDZ = getIncomeListZDZ.getJSONObject("result").getJSONArray("rs").size();

		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();

		for(int i=0;i<size;i++){
            JSONObject jsonObject = getIncomeList.getJSONObject("result").getJSONArray("rs").getJSONObject(i);
            String day_time = jsonObject.getString("day_time");
            String day_money = jsonObject.getString("day_money");
            String day_count = jsonObject.getString("day_count");

            String temp = null;
            for (int j = 0;j<sizeZDZ;j++){
                HashMap<String,Object> resultMap = new HashMap<String,Object>();

                JSONObject jsonObject_zdz = getIncomeListZDZ.getJSONObject("result").getJSONArray("rs").getJSONObject(j);
                String day_time_zdz = jsonObject_zdz.getString("day_time_zdz");
                String day_money_zdz = jsonObject_zdz.getString("day_money_zdz");
                String day_count_zdz = jsonObject_zdz.getString("day_count_zdz");
                if (day_time.equals(day_time_zdz)){
                    resultMap.put("day_time", day_time);
                    resultMap.put("day_money", Integer.valueOf(day_money)/100 + Integer.valueOf(day_money_zdz));
                    resultMap.put("day_count", Integer.valueOf(day_count) +  Integer.valueOf(day_count_zdz));
                    list.add(resultMap);
                    temp = null;
                    break;
                }else {
                    temp = day_time;
                }
            }
            if (temp != null){
                HashMap<String,Object> resultMapZD = new HashMap<String,Object>();
                resultMapZD.put("day_time", day_time);
                resultMapZD.put("day_money", Integer.valueOf(day_money)/100);
                resultMapZD.put("day_count", day_count);
                list.add(resultMapZD);
            }
		}
		for (int n=0;n<sizeZDZ;n++){
            JSONObject jsonObject_zdz = getIncomeListZDZ.getJSONObject("result").getJSONArray("rs").getJSONObject(n);
            String day_time_zdz = jsonObject_zdz.getString("day_time_zdz");
            String temp2 = null;
		    for (int m=0;m<size;m++){
                JSONObject jsonObject = getIncomeList.getJSONObject("result").getJSONArray("rs").getJSONObject(m);
                String day_time = jsonObject.getString("day_time");
                if (day_time.equals(day_time_zdz)){
                    temp2 = null;
                    break;
                }else {
                    temp2 = day_time;
                }
            }
            if (temp2 != null){
                HashMap<String,Object> resultMapZDZ = new HashMap<String,Object>();
                String day_money_zdz = jsonObject_zdz.getString("day_money_zdz");
                String day_count_zdz = jsonObject_zdz.getString("day_count_zdz");
                resultMapZDZ.put("day_time", day_time_zdz);
                resultMapZDZ.put("day_money", day_money_zdz);
                resultMapZDZ.put("day_count", day_count_zdz);
                list.add(resultMapZDZ);
            }

        }
        if (size == 0){
			for (int h = 0;h<sizeZDZ;h++){
				JSONObject jsonObject_zdz2 = getIncomeListZDZ.getJSONObject("result").getJSONArray("rs").getJSONObject(h);
				HashMap<String,Object> resultMapZDZ2 = new HashMap<String,Object>();
				String day_time_zdz = jsonObject_zdz2.getString("day_time_zdz");
				String day_money_zdz = jsonObject_zdz2.getString("day_money_zdz");
				String day_count_zdz = jsonObject_zdz2.getString("day_count_zdz");
				resultMapZDZ2.put("day_time", day_time_zdz);
				resultMapZDZ2.put("day_money", day_money_zdz);
				resultMapZDZ2.put("day_count", day_count_zdz);
				list.add(resultMapZDZ2);
			}
		}
		if (sizeZDZ==0){
			for (int k = 0;k <size;k++){
				JSONObject jsonObject2 = getIncomeList.getJSONObject("result").getJSONArray("rs").getJSONObject(k);
				HashMap<String,Object> resultMapZD2 = new HashMap<String,Object>();
				String day_time = jsonObject2.getString("day_time");
				String day_money = jsonObject2.getString("day_money");
				String day_count = jsonObject2.getString("day_count");
				resultMapZD2.put("day_time", day_time);
				resultMapZD2.put("day_money", day_money);
				resultMapZD2.put("day_count", day_count);
				list.add(resultMapZD2);
			}
		}
        Collections.sort(list, (o1, o2) -> {
            String name1 = (String) o1.get("day_time");
            String name2 = (String) o2.get("day_time");
            return name2.compareTo(name1);
        });

		HashMap<String,Object> resMap = new HashMap<String,Object>();
		resMap.put("result", list);
        String smap = JSON.toJSONString(resMap, SerializerFeature.DisableCircularReferenceDetect);
        System.out.println(smap);
        return smap;
	}

	//获取当天收益列表
	public static String getDayIncomeList(String token,String dayTime){

		String useId = UserService.getUserIdByToken(token);
		String dayIncomeList = MyselfService.getDayIncomeList(useId,dayTime);
		System.out.println(dayIncomeList);
		JSONObject getIncomeList = JSONObject.parseObject(dayIncomeList);
		//查询掌大赚收益列表
		String dayIncomeListZDZ = MyselfService.getDayIncomeListZDZ(useId,dayTime);
		JSONObject getIncomeListZDZ = JSONObject.parseObject(dayIncomeListZDZ);
		int size = getIncomeList.getJSONObject("result").getJSONArray("rs").size();
		int sizeZDZ = getIncomeListZDZ.getJSONObject("result").getJSONArray("rs").size();

		HashMap<String,Object> resultMapAll = new HashMap<String,Object>();
        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();


		for (int i=0;i<size;i++){
			JSONObject jsonObject = getIncomeList.getJSONObject("result").getJSONArray("rs").getJSONObject(i);
			String status = jsonObject.getString("status");
			String created_date = jsonObject.getString("created_date");
			String money = jsonObject.getString("money");
			String phone = jsonObject.getString("phone");
			String profit_source = jsonObject.getString("profit_source");
			//status  0：预估佣金 1：取消佣金 2：实际佣金 3：提现佣金
			HashMap<String,Object> resultMap = new HashMap<String,Object>();
			resultMap.put("status",Integer.valueOf(status));
			resultMap.put("created_date",created_date);
			resultMap.put("money",Integer.valueOf(money)/100);
			resultMap.put("phone",phone);
			resultMap.put("profit_source",profit_source);
			list.add(resultMap);


		}
		for (int j=0;j<sizeZDZ;j++){
			JSONObject jsonObject_zdz = getIncomeListZDZ.getJSONObject("result").getJSONArray("rs").getJSONObject(j);
			String status_zdz = jsonObject_zdz.getString("status");
			String created_date_zdz = jsonObject_zdz.getString("create_date");
			String money_zdz = jsonObject_zdz.getString("member_self_money");
			String phone_zdz = jsonObject_zdz.getString("phone");
			//status  4:掌大赚预估佣金  5：提现佣金  6：订单失败  7：退款
			HashMap<String,Object> resultMapZDZ = new HashMap<String,Object>();
			resultMapZDZ.put("status",Integer.valueOf(status_zdz)+4);
			resultMapZDZ.put("created_date",created_date_zdz);
			resultMapZDZ.put("money",Integer.valueOf(money_zdz));
			resultMapZDZ.put("phone",phone_zdz);
			resultMapZDZ.put("profit_source","4");
			list.add(resultMapZDZ);


		}
        Collections.sort(list, (o1, o2) -> {
            String name1 = (String) o1.get("created_date");
            String name2 = (String) o2.get("created_date");
            return name2.compareTo(name1);
        });

		resultMapAll.put("result", list);
        String smap = JSON.toJSONString(resultMapAll, SerializerFeature.DisableCircularReferenceDetect);
        System.out.println(smap);
        return smap;
	}

	/**
	 * 会员页面数据
	 * @param token
	 * @return
	 */
	public static String findMyself(String token){
		String userId = UserService.getUserIdByToken(token);
		if (userId == null) {
			return creatResult(2, "亲，未登录....", null).toString();
		}
		String  wallet = MyselfService.findWallet(userId);
		JSONObject result = JSONObject.parseObject(wallet);
		HashMap<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("result", result);
		return creatResult(1, "亲,数据包回来了哦...", resMap).toString();
	}

	//查询掌达赚入账记录
	public static String getAccountRecordZDZ(String token){
		String useId = UserService.getUserIdByToken(token);
		if (useId == null) {
			return creatResult(2, "亲，未登录....", null).toString();
		}
		String accountList = MyselfService.getAccountRecordZDZ(useId);
		System.out.println(accountList);
		JSONObject result = JSONObject.parseObject(accountList);
		HashMap<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("result", result);
		return creatResult(1, "亲,数据包回来了哦...", resMap).toString();
	}

	//查询用户会员等级
	public static String getMemberLevel(String token){
		String userId = UserService.getUserIdByToken(token);
		if (userId == null) {
			return creatResult(0, "亲，未登录....", null).toString();
		}
		String memberLevel = MyselfService.getMemberLevel(userId);
		JSONObject result = JSONObject.parseObject(memberLevel);
		HashMap<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("result", result);
		return creatResult(1, "亲,数据包回来了哦...", resMap).toString();
	}

	//查询我的粉丝
	public static String getMyFans(String token,String memberLevel){
		String userId = UserService.getUserIdByToken(token);
		if (userId == null) {
			return creatResult(0, "亲，未登录....", null).toString();
		}
		String myFans = MyselfService.getMyFans(userId,memberLevel);
		JSONObject result = JSONObject.parseObject(myFans);

		String levelCount = MyselfService.getLevelCount(userId);
		JSONObject jsonLevelCount = JSONObject.parseObject(levelCount);

		HashMap<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("result", result);
		resMap.put("count", jsonLevelCount);
		return creatResult(1, "亲,数据包回来了哦...", resMap).toString();
	}

	public static String getLevelCount(String token){
		String userId = UserService.getUserIdByToken(token);
		if (userId == null) {
			return creatResult(0, "亲，未登录....", null).toString();
		}
		String levelCount = MyselfService.getLevelCount(userId);
		JSONObject jsonLevelCount = JSONObject.parseObject(levelCount);

		HashMap<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("result", jsonLevelCount);
		return creatResult(1, "亲,数据包回来了哦...", resMap).toString();
	}

	/**
	 * 用户等级申请
	 * @param token
	 * @param applyLevel
	 * @return
	 */
	public static String addApplyMemberLevel(String token,String applyLevel){
		String userId = UserService.getUserIdByToken(token);
		if (userId == null) {
			return creatResult(2, "亲，未登录....", null).toString();
		}
		String checkResult = MyselfService.checkCondition(userId);
		if(null != checkResult){
			return checkResult;
		}
		int applyLevelResult = MyselfService.addApplyLevel(userId, applyLevel);
		if (applyLevelResult != 1) {
			return creatResult(6, "亲，系统忙，请稍等......", null).toString();
		} else {
			return creatResult(1, "提交成功", null).toString();
		}
	}
}