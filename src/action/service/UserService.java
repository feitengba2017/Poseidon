package action.service;

import cache.AioTcpCache;
import cache.BaseCache;
import cache.ResultPoor;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import common.RedisClient;
import utils.SmsUtil;

import java.util.*;

public class UserService extends BaseService {
	/**  
	* @Title: login  
	* @Description: TODO(登陆接口)  
	* @param @param loginName
	* @param @param pwd
	* @param @return    参数  
	* @return String    返回类型  
	* @throws  
	*/
	public static String login(String loginName, String pwd) {
		int sid = sendObject(AioTcpCache.gtc, 0, "", loginName, pwd);
		
		Integer gaiaId = (Integer) getFieldValue(ResultPoor.getResult(sid), "id", Integer.class);
		if(gaiaId == null) {
			return creatResult(3, "用户名密码错误", null).toString();
		}
		
		//如果没有用户，需要处理
		int userSid = sendObject(4, gaiaId);
		String rsTest = ResultPoor.getResult(userSid);
		int userStatus = (int) getFieldValue(rsTest, "status", Integer.class);
		//除以会员页面会员非会员的考虑
		String memberLevel = (String) getFieldValue(rsTest, "member_level", String.class);
		//返回是否有上级表示，出于安全考虑只返回标识字段
		String parentUserId = (String) getFieldValue(rsTest, "parent_user_id", String.class);
		int parentUserHasMark = 0;
		//考虑到后台修改数据时常常会产生parentUserId为空的情况，先如此处理
		if(parentUserId.equals("")||parentUserId==null||Integer.valueOf(parentUserId)==0){
			parentUserHasMark = 0;
		}else{
			parentUserHasMark = 1;
		}
		String token = "";
		if(userStatus==1) {
			token = UUID.randomUUID().toString();
			String userId = (String) getFieldValue(rsTest, "id", String.class);
			String oldToken = RedisClient.hget("user_token",userId,"token");
			//如果已登陆过，存在redis信息，先删除
			if(oldToken != null) {
				RedisClient.hdel("token_user", oldToken) ;
				RedisClient.hdel("user_token", userId) ;
			}
			
			RedisClient.hset("token_user",token,"userText",rsTest,60000000);
			RedisClient.hset("token_user",token,"userId",userId,60000000);
			
			RedisClient.hset("user_token",userId,"userText",rsTest,60000000);
			RedisClient.hset("user_token",userId,"token",token,60000000);
		}
		String res = "";
		if(token!="") {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("token", token);
			map.put("memberLevel", memberLevel);
			map.put("parentUserHasMark", Integer.toString(parentUserHasMark));
			res = creatResult(1, "", map).toString();
		}else {
			res = creatResult(3, "用户名在赢果不存在", null).toString();
		}
		return res;
	}
	
	/**  
	* @Title: getUserByToken  
	* @Description: TODO(根据token取用户信息)  
	* @param @param token
	* @param @return    参数  
	* @return String    返回类型  
	* @throws  
	*/
	public static String getUserByToken(String token) {
		return RedisClient.hget("token_user",token,"userText");
	}
	
	public static String getUserIdByToken(String token) {
		return RedisClient.hget("token_user",token,"userId");
	}
	
	/**  
	* @Title: getVerificationCode  
	* @Description: TODO(生成验证码)  
	* @param @param phone
	* @param @return    参数
	* @return String    返回类型  
	* @throws  
	*/
	public static String getVerificationCode(String phone) {
		//, "A", "B", "C", "D", "E", "F","G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
		String[] beforeShuffle = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
		List<String> list = Arrays.asList(beforeShuffle);
		Collections.shuffle(list);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			sb.append(list.get(i));
		}
		String afterShuffle = sb.toString();
		String result = afterShuffle.substring(3, 9);

		try {
			SendSmsResponse response = SmsUtil.sendSms(phone, "惠点科技", "SMS_128790134", "{\"code\":\""+ result +"\"}", phone);
			System.out.println("短信接口返回的数据----------------");
	        System.out.println("Code=" + response.getCode());
	        System.out.println("Message=" + response.getMessage());
	        System.out.println("RequestId=" + response.getRequestId());
	        System.out.println("BizId=" + response.getBizId());

//	        String code = response.getCode();
//	        String message = response.getMessage();
//	        String requestId = response.getRequestId();
//	        String bizId = response.getBizId();

	        int sid = sendObjectCreate(217, phone, "惠点科技", "SMS_128790134", "{\"code\":\""+ result +"\"}", phone, BaseCache.getTIME());
			ResultPoor.getResult(sid);

		} catch (ClientException e) {
			e.printStackTrace();
			return "000000";
		}

		return result;
	}

	/**  
	* @Title: hasGiaiUser  
	* @Description: TODO(用户中心是否存在用户)  
	* @param @param phone
	* @param @return    参数  
	* @return Integer    返回类型  
	* @throws  
	*/
	public static Integer hasUserByPhone(String phone) {
		int sid = sendObject(517, phone);
		String resText = ResultPoor.getResult(sid);
		return (Integer) getFieldValue(resText, "id", Integer.class);
	}
	
	/**  
	* @Title: hasGiaiUser  
	* @Description: TODO(用户中心是否存在用户)  
	* @param @param phone
	* @param @return    参数  
	* @return Integer    返回类型  
	* @throws  
	*/
	public static Integer hasGiaiUser(String phone) {
		int sid = sendObject(AioTcpCache.gtc, 4, "", phone);
		String resText = ResultPoor.getResult(sid);
		return (Integer) getFieldValue(resText, "id", Integer.class);
	}
	
	/**  
	* @Title: addGiaiUser  
	* @Description: TODO(增加用户中心用户)  
	* @param @param phone
	* @param @param pwd
	* @param @param source
	* @param @return    参数  
	* @return Integer    返回类型  
	* @throws  
	*/
	public static Integer addGiaiUser(String phone,String pwd,String source) {
		int sid = sendObject(AioTcpCache.gtc, 2, "", phone, pwd,"","","",phone,1,Integer.valueOf(source));
		String resText = ResultPoor.getResult(sid);
		JSONArray ja = JSONObject.parseObject(resText).getJSONObject("result").getJSONArray("ids");
		if(ja.size() == 0) {
			return null;
		}else {
			return ja.getInteger(0);
		}
	}

	/**  
	* @Title: addUranusUser  
	* @Description: TODO(增加uranus用户)  
	* @param @param phone
	* @param @param openid
	* @param @param gaiaId
	* @param @param source
	* @param @return    参数  
	* @return String    返回类型  
	* @throws  
	*/
	public static Integer addUranusUser(String phone, String openid, Integer gaiaId, String source) {
		int sid = sendObjectCreate(200, openid, phone, gaiaId, BaseCache.getTIME(), source, 1, 0, 1,"",phone);
		String resText = ResultPoor.getResult(sid);
		JSONArray ja = JSONObject.parseObject(resText).getJSONObject("result").getJSONArray("ids");
		return ja.getInteger(0);
	}
	
	/**  
	* @Title: resetPWD  
	* @Description: TODO(重置密码)  
	* @param @param phone
	* @param @param pwd
	* @param @return    参数  
	* @return Integer    返回类型  
	* @throws
	*/
	public static Integer resetPWD(String phone,String pwd) {
		int sid = sendObject(AioTcpCache.gtc, 3,"",pwd, phone);
		String resText = ResultPoor.getResult(sid);
		JSONObject ja = JSONObject.parseObject(resText).getJSONObject("result");
		return ja.getInteger("num");
	}
	
	/**  
	* @Title: resetPWD  
	* @Description: TODO(重置密码)  
	* @param @param phone
	* @param @param pwd
	* @param @return    参数  
	* @return Integer    返回类型  
	* @throws
	*/
	public static String updataMemberLevel(String userId) {
		int sid = sendObjectCreate(188, userId);
		String resText = ResultPoor.getResult(sid);
		return resText;
	}
	
	
	public static String grouByWetCat(String phone,String useId) {
		int sid = sendObjectCreate(512,phone, useId);
		String resText = ResultPoor.getResult(sid);
		return resText;
	}
	
	public static String loginByWetCat(String phone) {
		int sid = sendObject(513,phone);
		String resText = ResultPoor.getResult(sid);
		//JSONArray array =JSONObject.parseObject(resText).getJSONObject("result").getJSONArray("rs");
		int userStatus = (int) getFieldValue(resText, "status", Integer.class);
		//除以会员页面会员非会员的考虑
		String memberLevel = (String) getFieldValue(resText, "member_level", String.class);
		//返回是否有上级表示，出于安全考虑只返回标识字段
		String parentUserId = (String) getFieldValue(resText, "parent_user_id", String.class);
		int parentUserHasMark = 0;
		//考虑到后台修改数据时常常会产生parentUserId为空的情况，先如此处理
		if(parentUserId.equals("")||parentUserId==null||Integer.valueOf(parentUserId)==0){
			parentUserHasMark = 0;
		}else{
			parentUserHasMark = 1;
		}
		
		String token = "";
		if(userStatus==1) {
			token = UUID.randomUUID().toString();
			String userId = (String) getFieldValue(resText, "id", String.class);
			String oldToken = RedisClient.hget("user_token",userId,"token");
			//如果已登陆过，存在redis信息，先删除
			if(oldToken != null) {
				RedisClient.hdel("token_user", oldToken) ;
				RedisClient.hdel("user_token", userId) ;
			}
			
			RedisClient.hset("token_user",token,"userText",resText,60000000);
			RedisClient.hset("token_user",token,"userId",userId,60000000);
			
			RedisClient.hset("user_token",userId,"userText",resText,60000000);
			RedisClient.hset("user_token",userId,"token",token,60000000);
		}
		String res = "";
		if(token!="") {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("token", token);
			map.put("memberLevel", memberLevel);
			map.put("parentUserHasMark", Integer.toString(parentUserHasMark));
			res = creatResult(1, "", map).toString();
		}else {
			res = creatResult(3, "用户名在赢果不存在", null).toString();
		}
		return res;
	}
}