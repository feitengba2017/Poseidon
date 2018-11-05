package action;

import action.service.LoginService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import common.RedisClient;
import servlet.BaseServlet;
import servlet.HasLog;

import javax.servlet.annotation.WebServlet;
import java.util.HashMap;

/**
 * poseidon
 * Created by 18330 on 2018/10/13.
 */
@WebServlet(name = "Login", urlPatterns = "/login")
public class LoginAction extends BaseServlet {
    private static final long serialVersionUID = 1L;
    //用户登录
    @HasLog(logCode=100001)
    public static String userLogin(String loginName, String pwd) {
        String res = LoginService.userLogin(loginName, pwd);
        return res;
    }
    //用户注册
    public String addNewUserOptimize(String phone,String code,String pwd,String source,String inviteCode) {
        String verificationCode = RedisClient.hget("phone_verification_code", phone, "verification_code");
        if(verificationCode!=null && verificationCode.equals(code)) {
            if(!pwd.equals("")){
                pwd = pwd.substring(pwd.length()-6,pwd.length());
            }

            //如果填写了邀请人，但是邀请人不存在的情况下，直接返回
            if(!inviteCode.equals("")){
                //确认上级邀请人时需要判断,邀请码里的邀请人是是否存在
                Integer hasId = LoginService.getUsersgByCode(inviteCode);
                if(hasId == null) {
                    return creatResult(3, "邀请人不存在", null).toString();
                }
            }

            String has = LoginService.getNewUsersgByPhone(phone);
            JSONArray ja = JSONObject.parseObject(has).getJSONObject("result").getJSONArray("rs");

            HashMap<String, Object> map = new HashMap<String, Object>();
            Integer gaiaId = null;
            String sixCode = "";
            Integer userId = null;

            //判断用户是否存在
            if(ja.size() != 0) {
                return creatResult(3, "用户已存在", null).toString();
            }else{
                gaiaId = LoginService.addNewGiaiUser(phone, pwd, source);
                sixCode = LoginService.getRandomCode();
                userId = LoginService.addPoseidonUser(phone, phone, gaiaId, source,sixCode);
                LoginService.confirmNewSupmember(String.valueOf(userId),inviteCode);
//                loginByWetCat =  loginNewByWetCat(phone);
            }
            map.put("gaiaId", gaiaId);
            map.put("sixCode", sixCode);
            map.put("userId", userId);
            RedisClient.hdel("phone_verification_code", phone);
            return creatResult(1, "", map).toString();
        }else {
            return creatResult(2, "短信验证码错误", null).toString();
        }
    }

    public String loginNewByWetCat(String phone) {
        if(phone.equals("")){
            return creatResult(2, "手机号不为空", null).toString();
        }
        String res = LoginService.loginNewByWetCat(phone);
        return res;
    }
    //修改密码
    public String resetNewPWDOptimize(String phone,String pwd,String code) {
        String verificationCode = RedisClient.hget("phone_verification_code", phone, "verification_code");
        if(verificationCode!=null && verificationCode.equals(code)) {
            //Integer hasId = UserService.hasGiaiUser(phone);
            Integer hasId = LoginService.getUserByPhone(phone);
            if(hasId!=null) {
                RedisClient.hdel("phone_verification_code", phone);
                Integer num = LoginService.resetNewPWD(phone, pwd);
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
    //手机验证码
    public String getNewVerificationCodeOptimize(String phone) {
        String verificationCodeR = RedisClient.hget("phone_verification_code", phone, "verification_code");
        if(verificationCodeR!=null) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            Long tll = RedisClient.tll("phone_verification_code", phone);
            map.put("tll", tll);
            return creatResult(2, "验证码已发送"+(tll/60+1)+"分后再试!", map).toString();
        }
        String verificationCode = LoginService.getNewVerificationCode(phone);
        if(verificationCode.equals("000000")) {
            return creatResult(3, "短信发送失败", null).toString();
        }
        RedisClient.hset("phone_verification_code",phone,"verification_code",verificationCode,300);
        return creatResult(1, "", null).toString();

    }
    public String testLogin(String loginName, String pwd){

        String res = LoginService.userLogin(loginName, pwd);
        return res;
    }

}
