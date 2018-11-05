package action.service;

import cache.AioTcpCache;
import cache.BaseCache;
import cache.ResultPoor;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import common.PropertiesConf;
import common.RedisClient;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import utils.MD5Util;
import utils.StringUtil;
import utils.WechatRequest;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;


public  class MyselfService extends BaseService {
    /** 
     * 返回邀请人列表
     * @return 
     */  
    public static JSONArray inviteFriends(String useId) {
    	int sid = sendObject(179, useId,useId);
    	String inviteStr = ResultPoor.getResult(sid);
    	JSONObject invite_json = JSONObject.parseObject(inviteStr);
    	JSONArray inviteResult = invite_json.getJSONObject("result").getJSONArray("rs");
		return inviteResult;	
     }  
    
    /** 
     * 返回邀请码	邀请码先定位手机号
     * @return 
     */  
    public static JSONArray inviteCode(String useId) {
    	int sid = sendObject(178, useId);
    	String inviteCodeStr = ResultPoor.getResult(sid);
    	JSONObject inviteCode_json = JSONObject.parseObject(inviteCodeStr);
    	JSONArray inviteCode = inviteCode_json.getJSONObject("result").getJSONArray("rs");
		return inviteCode;	
     }  
    
    /** 
     * 返回钱包列表
     * @return 
     */  
    public static JSONArray findWalletList(String useId, String nowMonth) {
    	int sid = sendObject(180, useId,nowMonth);
    	String findWalletStr = ResultPoor.getResult(sid);
    	JSONObject findWallet_json = JSONObject.parseObject(findWalletStr);
    	JSONArray findWallet = findWallet_json.getJSONObject("result").getJSONArray("rs");
		return findWallet;	
     }
    
	/**
	 * 我的模块,礼包商品列表搜索
	 */
	public static String findGiftGoodList(Integer beginNum,Integer endNum) {  
    	String sql = " order by p.create_time desc";	        
		int searchGoodsGiftInt =  sendObject(AioTcpCache.ctc,186,sql,PropertiesConf.HESTIA_URL,beginNum,endNum);
		return ResultPoor.getResult(searchGoodsGiftInt);
    }  
	
	/**
	 * 确然上级和加入被邀请人列表
	 */
	public static String confirmSupmember(String useId,String phone) {  
		//更新我的上级
		sendObjectCreate(189,phone,useId);
		//查询邀请人信息
		int insertBeInvite =  sendObject(192,useId);
		//生成当前时间  插入邀请表
		String inviteDate = BaseCache.getTIME();
		JSONObject beInvite = JSONObject.parseObject(ResultPoor.getResult(insertBeInvite));
		String phoneInvite = beInvite.getJSONObject("result").getJSONArray("rs").getJSONObject(0).getString("phone");
		String nickNameInvite = beInvite.getJSONObject("result").getJSONArray("rs").getJSONObject(0).getString("nick_name"); 		
		int insertBeInvites =  sendObjectCreate(193,useId,phoneInvite,nickNameInvite,inviteDate);
		return ResultPoor.getResult(insertBeInvites);
    }  
	
	/**
	 * 查询我的上级
	 */
	public static String findSupmember(String useId) {  
		int findSupmember =  sendObject(198,useId);
		return ResultPoor.getResult(findSupmember);
    } 
	
	/**
	 * 根据用户Id查询用户信息
	 */
	public static String getUserMsg(String useId) {  
		int getUserMsg =  sendObject(514,useId);
		return ResultPoor.getResult(getUserMsg);
    } 
	
	/**
	 * 根据用户Id查询用户信息
	 */
	public static String getUsersgByPhone(String phone) {  
		int getUserMsg = sendObject(517,phone);
		return ResultPoor.getResult(getUserMsg);
    } 
	
	/**
	 * 先判断你是否是我的下级,而我刚好没有上级的情况
	 */
	public static String findIsNotSon(String useId) {  
		int findIsNotSon =  sendObject(199,useId,useId,useId);
		return ResultPoor.getResult(findIsNotSon);
    } 
	
	/**
	 * 用户会员状态下展示页面，显示分销计算
	 */
	public static String findMember(String useId,String nowTime) {  
		int findMember =  sendObject(504,nowTime.substring(0, 6),nowTime.substring(0, 4),useId);
		return ResultPoor.getResult(findMember);
    } 
	
	/**
	 * 收藏商品列表
	 */
	public static String storeGoods(String useId,String nowTime,String spuId) {  
		int storeGoodsInt =  sendObjectCreate(506,useId,spuId,nowTime);
		return ResultPoor.getResult(storeGoodsInt);
    } 
	
	/**
	 * 批量取消收藏
	 */
	public static String unStoreGoodsList(String useId,String spuIdStr) {  
		StringBuffer sql = new StringBuffer();
        sql.append(" and user_id = ").append(useId);
		if (spuIdStr != null && !spuIdStr.equals("")) {
            sql.append(" and spu_id in (").append(spuIdStr).append(")");
        }
		int storeGoodsInt = sendObjectBase(507,sql.toString());
		return ResultPoor.getResult(storeGoodsInt);
    } 
	
	/**
	 * 查询我的收藏列表
	 */
	public static String findStoreGoodsList(String useId,Integer beginNum,Integer endNum) {  
		int storeGoodsInt =  sendObject(508,PropertiesConf.HESTIA_URL,useId,1,beginNum,endNum);
		return ResultPoor.getResult(storeGoodsInt);
    }

	//获取当月收益列表
	public static String getMonthIncomeList(String useId,String monthTime) {


		int sid =  sendObject(704,useId,monthTime);
		return ResultPoor.getResult(sid);
	}
	//获取掌大赚当月收益列表
	public static String getMonthIncomeListZDZ(String useId,String monthTime) {

		int sid =  sendObject(721,useId,monthTime);
		return ResultPoor.getResult(sid);
	}

	//获取当天收益列表
	public static String getDayIncomeList(String useId,String dayTime) {
		int sid =  sendObject(705,dayTime,useId);
		return ResultPoor.getResult(sid);
	}
	//获取掌大赚当天收益列表
	public static String getDayIncomeListZDZ(String useId,String dayTime) {
		int sid =  sendObject(722,dayTime,useId);
		return ResultPoor.getResult(sid);
	}
	/**
	 * 返回钱包
	 * @return
	 */
	public static String findWallet(String useId) {
		int sid = sendObject(616, useId);
		return ResultPoor.getResult(sid);
	}

	public static HashMap<String, Object> putWallet(String partner_trade_no,String openid,String amount,String user_id,String withdrawals_id) throws  Exception{


		String appid = PropertiesConf.APP_ID.trim();
		String mch_id = PropertiesConf.MINI_MCH_ID.trim();
		String nonce_str = StringUtil.getRandomStringByLength(32);

		//证书文件不能放在web服务器虚拟目录，应放在有访问权限控制的目录中，防止被他人下载
		String certPath = PropertiesConf.merchantCertificatePath.trim();
//		String certPath = "D:/CenterWorkSpace/uranus/apiclient_cert.p12";
		CloseableHttpClient closeableHttpClient = null;
		try {
			closeableHttpClient = initCert(mch_id, certPath);
		} catch (Exception e) {
			e.printStackTrace();
		}

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("mch_appid",appid);
		map.put("mchid",mch_id);
		map.put("openid",openid);
		map.put("amount",amount);
		map.put("nonce_str",nonce_str);
		map.put("partner_trade_no",partner_trade_no);
		map.put("check_name","NO_CHECK");
		map.put("desc","提现到微信");
		map.put("spbill_create_ip","10.1.1.85");
		map.put("sign",MD5Util.sign(StringUtil.createLinkString(map),"&key=" + PropertiesConf.MINI_APP_KEY.trim(), "UTF-8").toUpperCase());

		String xml = StringUtil.mapToXML(map);
		System.out.println(xml);
		WechatRequest wchatHttpsRequest = new WechatRequest();
		//String responseString = wchatHttpsRequest.httpRequest(WechatRequest.WITHDRAW_CASH_URL,"POST", xml);
		HttpPost httpPost = new HttpPost(WechatRequest.WITHDRAW_CASH_URL);
		// 得指明使用UTF-8编码，否则到API服务器XML的中文不能被成功识别
		StringEntity postEntity = new StringEntity(xml, "UTF-8");
		httpPost.addHeader("Content-Type", "text/xml");
		httpPost.setEntity(postEntity);
		CloseableHttpResponse execute = closeableHttpClient.execute(httpPost);
		HttpEntity entity = execute.getEntity();
		String result = EntityUtils.toString(entity, "UTF-8");
		System.out.println("xml返回数据=========="+result);
		Map<String, Object> resultMap = StringUtil.xml2Map(result);
		if (!"SUCCESS".equals(resultMap.get("result_code"))) {
			//失败恢复钱包金额

			StringBuffer sql = new StringBuffer();
			if (withdrawals_id != null && !withdrawals_id.equals("")) {
				sql.append(" and w.id = ").append(withdrawals_id);
				int sid = sendObjectBase(711, sql.toString());
				String result1 = ResultPoor.getResult(sid);
				JSONObject res = JSONObject.parseObject(result1);

				String money = res.getJSONObject("result").getJSONArray("rs").getJSONObject(0).getString("money");
				int intAmount = Integer.valueOf(amount);
				int intMoney = Integer.valueOf(money)+intAmount;
				sendObjectCreate(715,intMoney,user_id);
				//获取错误信息 修改失败状态
				String err_code_des = resultMap.get("err_code_des").toString();
				int err_code =  sendObjectCreate(717,err_code_des,"6",withdrawals_id);
				String result2 = ResultPoor.getResult(err_code);
				JSONObject res2 = JSONObject.parseObject(result2);
			}

		}else{
			sendObjectCreate(716,"5",withdrawals_id);
		}
		// 组装返回参数
		map.clear();
		map.put("mch_appid", resultMap.get("mch_appid"));
		map.put("mchid", resultMap.get("mchid"));
		map.put("device_info", resultMap.get("device_info"));
		map.put("nonce_str", resultMap.get("nonce_str"));
		map.put("result_code", resultMap.get("result_code"));
		System.out.println("返回参数"+map);
		return map;
	}
	private static CloseableHttpClient initCert(String mchId, String certPath) throws Exception {
		// 证书密码，默认为商户ID
		String key = mchId;
		// 证书的路径
		String path = certPath;
		// 指定读取证书格式为PKCS12
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		// 读取本机存放的PKCS12证书文件
		FileInputStream instream = new FileInputStream(new File(path));
		try {
			// 指定PKCS12的密码(商户ID)
			keyStore.load(instream, key.toCharArray());
		} finally {
			instream.close();
		}
		SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, key.toCharArray()).build();
		SSLConnectionSocketFactory sslsf =
				new SSLConnectionSocketFactory(sslcontext, new String[] {"TLSv1"}, null,
						SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
		return HttpClients.custom().setSSLSocketFactory(sslsf).build();
	}

	//保存提现信息
	public static String reviewWithdraw(String userId,String jsCode,int amount,String phone,String smsCode) {

		String verificationCode = RedisClient.hget("phone_verification_code", phone, "verification_code");
		if(verificationCode!=null && verificationCode.equals(smsCode)) {
			//生成当前时间
			String create_time = BaseCache.getTIME();
			//生成提现订单
			String random = StringUtil.getRandomStringByLength(4);
			String partner_trade_no = create_time + random;

			//查询钱包金额
			int sid = sendObject(616, userId);
			String result = ResultPoor.getResult(sid);
			JSONObject res = JSONObject.parseObject(result);
			String money = res.getJSONObject("result").getJSONArray("rs").getJSONObject(0).getString("money");

			int intAmount = Integer.valueOf(amount);
			int intMoney = Integer.valueOf(money);
			if ( intAmount > 0 && intAmount <= intMoney){
				int i1 = sendObjectCreate(715, intMoney - intAmount, userId);

				String result1 = ResultPoor.getResult(i1);
				JSONObject result_json = JSONObject.parseObject(result1);
				String success = result_json.getString("success");
				if ( "1".equals(success)){
					//保存提现信息
					Map<String, Object> map = WechatPayService.findOpenid(jsCode);
					String openid = "";
					if(map != null){
						openid = map.get("openid").toString();

					}
					int i =  sendObjectCreate(712,partner_trade_no,userId,openid,amount,"1","1",create_time);
					String str = ResultPoor.getResult(i);
					return str;
				}
				return creatResult(2, "提现金额有误", null).toString();

			}else{
				return creatResult(2, "提现金额有误", null).toString();
			}
		}else {
			return creatResult(2, "短信验证码错误", null).toString();
		}

	}

	//查询通过审核的提现
	public static String agreeWithdraw(String status){
		System.out.println("***你的小可爱来扫描审核同意的提现了***");
		int sid =  sendObject(713,status);
		String result = ResultPoor.getResult(sid);
		System.out.println("============"+result);
		JSONObject res = JSONObject.parseObject(result);
		JSONArray jsonArray = res.getJSONObject("result").getJSONArray("rs");
		for (int i=0;i<jsonArray.size();i++){
			String openID = jsonArray.getJSONObject(i).getString("jsCode");
			System.out.println("openID==========="+openID);
			String txamount = jsonArray.getJSONObject(i).getString("amount");
			String partner_trade_no = jsonArray.getJSONObject(i).getString("withdrawals_on");
			String user_id = jsonArray.getJSONObject(i).getString("user_id");
			String withdrawals_id = jsonArray.getJSONObject(i).getString("id");
			//扫描后，先修改状态
			String statu = jsonArray.getJSONObject(i).getString("status");
			if ("2".equals(statu)){
				int i1 = sendObjectCreate(716, "7", withdrawals_id);
				String resu = ResultPoor.getResult(i1);
			}

			HashMap<String, Object> map1 = null;
			try {
				map1 = putWallet(partner_trade_no, openID, txamount, user_id, withdrawals_id);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}
	public static JSONObject creatResults(int success, String errorMessage, HashMap<String, Object> map) {
		JSONObject jo = new JSONObject();
		if (map != null) {
			JSONObject jors = new JSONObject();
			jors.put("total", map.size());

			JSONArray ja = new JSONArray();
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				System.out.println("key =" + key + " value = " + value);

				JSONObject joo = new JSONObject();
				joo.put(key, value);
				ja.add(joo);
			}
			jors.put("rs", ja);
			jo.put("result",jors);
		}
		jo.put("success", success);
		jo.put("errorMessage", errorMessage);
		return jo;
	}

	//查询提现记录
	public static String getWithdrawRecord(String userId) {
		Integer sid = sendObject(720,userId);
		return  ResultPoor.getResult(sid);
	}

	//查询掌达赚入账记录
	public static String getAccountRecordZDZ(String userId){

		Integer sid = sendObject(723,userId);
		return  ResultPoor.getResult(sid);
	}

	//查询用户会员等级
	public static String getMemberLevel(String useId) {

		int sid =  sendObject(742,useId);
		return ResultPoor.getResult(sid);
	}

	public static String getMyFans(String useId,String memberLevel) {
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE t.id IN ( SELECT id FROM uranus.t_user WHERE parent_user_id = ").append(useId);
		sql.append("  UNION SELECT id FROM uranus.t_user WHERE parent_user_id IN ( SELECT t1.id FROM uranus.t_user AS t1 LEFT JOIN uranus.t_user AS t2 ON t1.parent_user_id = t2.id WHERE t1.parent_user_id = ").append(useId).append("  AND t2.member_level = 3 ) ) ");;
		if(memberLevel != null &&  !memberLevel.equals("")){
			if(memberLevel.equals("0") || memberLevel.equals("1")){
				sql.append(" and t.member_level in (0,1) GROUP BY i.beInvite_date desc");
			}else if(memberLevel.equals("2")){
				sql.append(" and t.member_level = 2 GROUP BY i.beInvite_date desc");
			}else if(memberLevel.equals("3")){
				sql.append(" and t.member_level = 3 GROUP BY i.beInvite_date desc");
			}
		}else{
			sql.append(" GROUP BY i.beInvite_date desc");
		}
		int i = sendObjectBase(744,sql.toString());
		return ResultPoor.getResult(i);

	}

	public static String getLevelCount(String useId) {

		int sid =  sendObject(743,useId,useId);
		return ResultPoor.getResult(sid);
	}

	/**
	 *
	 * @param memberLevel
	 * @return
	 */
	public static String getMemberMessagesInfo(String memberLevel,String messageSign){
		int sid = sendObject(471, PropertiesConf.HESTIA_URL, memberLevel, messageSign);
		String res = ResultPoor.getResult(sid, 10, 50);
		return res;
	}


	public static int addApplyLevel(String userId,String applyLevel){
		String inviteDate = BaseCache.getTIME();
		int state = 0;
		int i = sendObjectCreate(655,userId,applyLevel,state,inviteDate);
		String result = ResultPoor.getResult(i);
		JSONObject result_json = JSONObject.parseObject(result);
		int ids = result_json.getInteger("success");
		return ids;
	}

	/**
	 *校验用户等级资格
	 * @param userId
	 * @return
	 */
	public static String checkCondition(String userId){
		int i = sendObject(658, userId);
		String result = ResultPoor.getResult(i);
		JSONObject jsonObject = JSONObject.parseObject(result);
		JSONArray jsonArray = jsonObject.getJSONObject("result").getJSONArray("rs");
		String number = jsonArray.getJSONObject(0).getString("number");
		if(Integer.valueOf(number) <5){
			return creatResult(2, "亲，您的拉粉数量不足5个", null).toString();
		}
		int i1 = sendObject(659, userId, userId);
		String results = ResultPoor.getResult(i1);
		JSONObject jsonObjects = JSONObject.parseObject(results);
		JSONArray jsonArrays = jsonObjects.getJSONObject("result").getJSONArray("rs");
		String psum = jsonArrays.getJSONObject(0).getString("psum");
		String zsum = jsonArrays.getJSONObject(0).getString("zsum");
		if (Integer.valueOf(psum) <1 || Integer.valueOf(zsum) < 1) {
			return creatResult(3, "亲，请先完成一条订单在申请", null).toString();
		}
		return null;
	}

	/**
	 *
	 * @param memberLevel
	 * @return
	 */
	public static String messageInfoList(String memberLevel){
		int sid = sendObject(473, memberLevel);
		String res = ResultPoor.getResult(sid);
		return res;
	}
}
