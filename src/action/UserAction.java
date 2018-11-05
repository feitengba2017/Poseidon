package action;

import action.service.MyselfService;
import action.service.UserService;
import action.service.WechatPayService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import common.RedisClient;
import servlet.BaseServlet;
import servlet.HasLog;
import servlet.HasToken;
import utils.CodeUtil;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "User", urlPatterns = "/user")
public class UserAction extends BaseServlet{
	
	private static final long serialVersionUID = 1L;
	@HasLog(logCode=100001)
	public String getUser(String loginName,String pwd) {
		String res = UserService.login(loginName, pwd);
		return res;
	}
	@HasLog(logCode=100002)
	public String getUserByToken(String token) {
		String res = UserService.getUserByToken(token);
		return res;
	}
	

	public String getVerificationCode(String phone,String key,String imgCode) {
		String redisImgCode = RedisClient.hget("img_code", key, "img_code");
		if(redisImgCode!=null && redisImgCode.equals(imgCode)) {
			String verificationCodeR = RedisClient.hget("phone_verification_code", phone, "verification_code");
			if(verificationCodeR!=null) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				Long tll = RedisClient.tll("phone_verification_code", phone);
				map.put("tll", tll);
				return creatResult(2, "验证码已发送"+tll/60+"分后再试!", map).toString();
			}
			String verificationCode = UserService.getVerificationCode(phone);
			if(verificationCode.equals("000000")) {
				return creatResult(3, "短信发送失败", null).toString();
			}
			RedisClient.hset("phone_verification_code",phone,"verification_code",verificationCode,300);
			RedisClient.hset("phone_verification_code",phone,"img_code",imgCode,300);
			RedisClient.hdel("img_code", key);
			return creatResult(1, "", null).toString();
		}else {
			RedisClient.hdel("img_code", key);
			return creatResult(4, "图片验证码错误", null).toString();
		}
	}
	
	public String addUser(String phone,String code,String pwd,String source,String key,String imgCode,String wxCode) {
		String redisImgCode = RedisClient.hget("img_code", key, "img_code");
		String vImgCode = RedisClient.hget("phone_verification_code",phone,"img_code");
		if(redisImgCode != null && !imgCode.equals(redisImgCode)) {
			return creatResult(2, "图片验证码错误", null).toString();
		}else if(redisImgCode == null && vImgCode!=null){
			if(!vImgCode.equals(imgCode)) {
				RedisClient.hset("phone_verification_code",phone,"img_code",null,300);
				return creatResult(2, "图片验证码错误", null).toString();
			}
		}		
		String verificationCode = RedisClient.hget("phone_verification_code", phone, "verification_code");
		if(verificationCode!=null && verificationCode.equals(code)) {
			
			Integer hasId = UserService.hasGiaiUser(phone);
			if(hasId!=null) {
				return creatResult(2, "用户已存在", null).toString();
			}				
			RedisClient.hdel("phone_verification_code", phone);
			
			String openid = "";
			Map<String, Object> resultMap =  WechatPayService.findOpenid(wxCode);
			if(resultMap != null){
				openid = resultMap.get("openid").toString();
			}
			
			Integer gaiaId = UserService.addGiaiUser(phone, pwd, source);
			UserService.addUranusUser(phone, openid, gaiaId, source);
			return creatResult(1, "亲,注册成功哦", null).toString();
		}else {
			return creatResult(2, "短信验证码错误", null).toString();
		}
	}
	
	@HasToken
	public String resetPWD(String phone,String code,String pwd,String key,String imgCode) {
		
		String redisImgCode = RedisClient.hget("img_code", key, "img_code");
		String vImgCode = RedisClient.hget("phone_verification_code",phone,"img_code");
		if(redisImgCode != null && !imgCode.equals(redisImgCode)) {
			return creatResult(2, "图片验证码错误", null).toString();
		}else if(redisImgCode == null && vImgCode!=null){
			if(!vImgCode.equals(imgCode)) {
				RedisClient.hset("phone_verification_code",phone,"img_code",null,300);
				return creatResult(2, "图片验证码错误", null).toString();
			}
		}				
		String verificationCode = RedisClient.hget("phone_verification_code", phone, "verification_code");
		if(verificationCode!=null && verificationCode.equals(code)) {
			
			Integer hasId = UserService.hasGiaiUser(phone);
			if(hasId!=null) {
				RedisClient.hdel("phone_verification_code", phone);
				Integer num = UserService.resetPWD(phone, pwd);
				if(num>0)
					return creatResult(1, "重置成功", null).toString();
				else
					return creatResult(2, "重置失败", null).toString();
			}else {
				return creatResult(2, "用户不存在", null).toString();
			}
		}else {
			return creatResult(2, "验证码错误", null).toString();
		}
	}
	
	/**
	 * 获取图片验证码	
	 * @param key
	 * @param resp
	 */
	public void getImgCode(String key,HttpServletResponse resp) {
		Map<String, Object> codeMap = CodeUtil.generateCodeAndPic();

		RedisClient.hset("img_code",key,"img_code",codeMap.get("code").toString(),300);
		
        // 禁止图像缓存。
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setDateHeader("Expires", -1);

        resp.setContentType("image/jpeg");

        // 将图像输出到Servlet输出流中。
        ServletOutputStream sos;
        try {
            sos = resp.getOutputStream();
            ImageIO.write((RenderedImage) codeMap.get("codePic"), "jpeg", sos);
            sos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	} 
	
	
	/**
	 * 2018-05-30	libai
	 * 注册修改密码第一步校验：1,防止机刷验证码; 2,手机号是否注册
	 */
	public String getCheckCodeAndPhone(String phone,String key,String imgCode) {
		String redisImgCode = RedisClient.hget("img_code", key, "img_code");
		if(redisImgCode!=null && redisImgCode.equals(imgCode)) {		
			//Integer hasId = UserService.hasGiaiUser(phone);
			Integer hasId = UserService.hasUserByPhone(phone);
			if(hasId!=null) {
				return creatResult(3, "用户已存在", null).toString();
			}	
			return creatResult(1, "用户不存在", null).toString();
		}else {
			RedisClient.hdel("img_code", key);
			return creatResult(4, "图片验证码错误", null).toString();
		}
	}
	
	/**
	 * 2018-05-30	libai
	 * 获取验证码
	 */
	public String getVerificationCodeOptimize(String phone) {
			String verificationCodeR = RedisClient.hget("phone_verification_code", phone, "verification_code");
			if(verificationCodeR!=null) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				Long tll = RedisClient.tll("phone_verification_code", phone);
				map.put("tll", tll);
				return creatResult(2, "验证码已发送"+(tll/60+1)+"分后再试!", map).toString();
			}
			String verificationCode = UserService.getVerificationCode(phone);
			if(verificationCode.equals("000000")) {
				return creatResult(3, "短信发送失败", null).toString();
			}
			RedisClient.hset("phone_verification_code",phone,"verification_code",verificationCode,300);
			return creatResult(1, "", null).toString();
	
	}
	
	/**
	 * 2018-05-30	libai
	 * 优化版注册
	 */
	public String addUserOptimize(String phone,String code,String pwd,String source,String wxCode,String inviteCode) {
		    String verificationCode = RedisClient.hget("phone_verification_code", phone, "verification_code");
	        if(verificationCode!=null && verificationCode.equals(code)) {
	        	//String openid =  WechatPayService.findOpenid(wxCode);
//				if(openid.equals("")){
//					return creatResult(1, "获取open_id失败", null).toString();
//				}
				if(!pwd.equals("")){
					pwd = pwd.substring(pwd.length()-6,pwd.length());
				}	
				
				//如果填写了邀请人，但是邀请人不存在的情况下，直接返回
				if(!inviteCode.equals("")){
					//确认上级邀请人时需要判断,邀请码里的邀请人是是否存在
					Integer hasId = UserService.hasUserByPhone(inviteCode);
					if(hasId == null) {
						return creatResult(3, "邀请人不存在", null).toString();							
					}
				}
				
				String has = MyselfService.getUsersgByPhone(phone);
				JSONArray ja = JSONObject.parseObject(has).getJSONObject("result").getJSONArray("rs");
		
				HashMap<String, String> map = new HashMap<String, String>();
				String inviteMsg = "";
				String loginByWetCat = "";							
				
				//判断用户是否存在
				if(ja.size() != 0) {
					JSONObject jsonObject = (JSONObject) ja.get(0);
					//微信绑定关系错误或者需要修改
					if(!jsonObject.get("wx_openid").equals(phone)) {	
						  //绑定微信关系
						  UserService.grouByWetCat(phone,jsonObject.getString("id"));
					 }
					//绑定邀请人上级
                    MyselfService.confirmSupmember(jsonObject.get("id").toString(),inviteCode);
					loginByWetCat =  loginByWetCat(phone);
				}else{	
					Integer gaiaId = UserService.addGiaiUser(phone, pwd, source);
					Integer userId = UserService.addUranusUser(phone, phone, gaiaId, source);
					MyselfService.confirmSupmember(String.valueOf(userId),inviteCode);
					loginByWetCat = loginByWetCat(phone);
				}	
				
				map.put("loginByWetCat", loginByWetCat);
				map.put("inviteMsg", inviteMsg);
				RedisClient.hdel("phone_verification_code", phone);		
				return loginByWetCat;
			}else {
				return creatResult(2, "短信验证码错误", null).toString();
			}	    
	}
	
	/**
	 * 2018-05-30	libai
	 * 优化版修改密码
	 */
	public String resetPWDOptimize(String phone,String pwd,String code) {					
		String verificationCode = RedisClient.hget("phone_verification_code", phone, "verification_code");
		if(verificationCode!=null && verificationCode.equals(code)) {			
			//Integer hasId = UserService.hasGiaiUser(phone);
			Integer hasId = UserService.hasUserByPhone(phone);
			if(hasId!=null) {
				RedisClient.hdel("phone_verification_code", phone);
				Integer num = UserService.resetPWD(phone, pwd);
				if(num>0)
					return creatResult(1, "重置成功", null).toString();
				else
					return creatResult(3, "重置失败", null).toString();
			}else {
				return creatResult(4, "用户不存在", null).toString();
			}
		}else {
			return creatResult(2, "短信验证码错误", null).toString();
		}
	}
	
	/**
	 * 2018-05-30	libai
	 * 登录情况下，绑定关系
	 */
	public String grouByWetCat(String wxCode,String token) {
		String useId = UserService.getUserIdByToken(token);
		if(useId==null){
			return creatResult(0, "亲，未登录....", null).toString();
		}
		String openid = "";
		Map<String, Object> resultMap =  WechatPayService.findOpenid(wxCode);
		if(resultMap != null){
			openid = resultMap.get("openid").toString();
		}		
		
		String res = UserService.grouByWetCat(useId, openid);
		return res;
	}
	
	/**
	 * 2018-05-30	libai
	 * 微信登录,不需要用户登录
	 */
	public String loginByWetCat(String phone) {
		//String openid =  WechatPayService.findOpenid(wxCode);
		if(phone.equals("")){
			return creatResult(2, "手机号不为空", null).toString();
		}
		String res = UserService.loginByWetCat(phone);
		return res;
	}
	
	/**
	 * 2018-06-05	libai
	 * 判断手机号是否注册
	 */
	public String getCheckPhoneWetCat(String phone) {
		String has = MyselfService.getUsersgByPhone(phone);
		JSONArray ja = JSONObject.parseObject(has).getJSONObject("result").getJSONArray("rs");
		//HashMap<String, Object> map = new HashMap<String, Object>();
		if(ja.size()!=0){
			JSONObject jsonObject = (JSONObject) ja.get(0);
			if(jsonObject.get("wx_openid").equals(phone)) {
				String loginByWetCat = UserService.loginByWetCat(phone);
				//map.put("result", loginByWetCat);
				return  loginByWetCat;
			}
		}		
		return creatResult(2, "用户不存在", null).toString();
	}
	
}