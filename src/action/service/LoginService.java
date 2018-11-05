package action.service;

import cache.AioTcpCache;
import cache.BaseCache;
import cache.ResultPoor;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import common.RedisClient;
import utils.MD5Util;
import utils.SmsUtil;
import utils.StringUtil;

import java.util.*;

/**
 * poseidon
 * Created by 18330 on 2018/10/13.
 */
public class LoginService extends BaseService {

    //用户登录
    public static String userLogin(String loginName, String pwd) {
//        int sid = sendObject(AioTcpCache.gtc, 0, "", loginName, pwd);
        int sid = sendObject(762,loginName, MD5Util.MD5(pwd));
        Integer gaiaId = (Integer) getFieldValue(ResultPoor.getResult(sid), "id", Integer.class);
        if(gaiaId == null) {
            return creatResult(3, "用户名密码错误", null).toString();
        }

        //如果没有用户，需要处理
        int userSid = sendObject(751, gaiaId);
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

    public static Integer getUsersgByCode(String inviteCode) {
        int sid = sendObject(752, inviteCode);
        String resText = ResultPoor.getResult(sid);
        return (Integer) getFieldValue(resText, "id", Integer.class);
    }

    public static String getNewUsersgByPhone(String phone) {
        int getUserMsg = sendObject(754,phone);
        return ResultPoor.getResult(getUserMsg);
    }
    public static Integer getUserByPhone(String phone) {
        int sid = sendObject(754, phone);
        String resText = ResultPoor.getResult(sid);
        return (Integer) getFieldValue(resText, "id", Integer.class);
    }

    public static Integer resetNewPWD(String phone,String pwd) {
        int sid = sendObject(AioTcpCache.gtc, 3,"",pwd, phone);
        String resText = ResultPoor.getResult(sid);
        JSONObject ja = JSONObject.parseObject(resText).getJSONObject("result");
        return ja.getInteger("num");
    }

    public static String getNewVerificationCode(String phone) {
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


            int sid = sendObjectCreate(753, phone, "惠点科技", "SMS_128790134", "{\"code\":\""+ result +"\"}", phone, BaseCache.getTIME());
            ResultPoor.getResult(sid);

        } catch (ClientException e) {
            e.printStackTrace();
            return "000000";
        }

        return result;
    }

    /**
     * 确然上级和加入被邀请人列表
     */
    public static String confirmNewSupmember(String useId,String phone) {
        //更新我的上级
        sendObjectCreate(755,phone,useId);
        //查询邀请人信息
        int insertBeInvite =  sendObject(756,useId);
        //生成当前时间  插入邀请表
        String inviteDate = BaseCache.getTIME();
        JSONObject beInvite = JSONObject.parseObject(ResultPoor.getResult(insertBeInvite));
        String phoneInvite = beInvite.getJSONObject("result").getJSONArray("rs").getJSONObject(0).getString("phone");
        String nickNameInvite = beInvite.getJSONObject("result").getJSONArray("rs").getJSONObject(0).getString("nick_name");
        int insertBeInvites =  sendObjectCreate(757,useId,phoneInvite,nickNameInvite,inviteDate);
        return ResultPoor.getResult(insertBeInvites);
    }

    public static Integer addNewGiaiUser(String phone,String pwd,String source) {
        int sid = sendObject(AioTcpCache.gtc, 2, "", phone, pwd,"","","",phone,1,Integer.valueOf(source));
        String resText = ResultPoor.getResult(sid);
        JSONArray ja = JSONObject.parseObject(resText).getJSONObject("result").getJSONArray("ids");
        if(ja.size() == 0) {
            return null;
        }else {
            return ja.getInteger(0);
        }
    }

    public static Integer addPoseidonUser(String phone, String openid, Integer gaiaId, String source,String sixCode) {
        int sid = sendObjectCreate(758, openid, phone, gaiaId, BaseCache.getTIME(), source, 1, 0, 1,"",phone,sixCode);
        String resText = ResultPoor.getResult(sid);
        JSONArray ja = JSONObject.parseObject(resText).getJSONObject("result").getJSONArray("ids");
        return ja.getInteger(0);
    }

    public static String loginNewByWetCat(String phone) {
        int sid = sendObject(759,phone);
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

    public static String getRandomCode(){
        String sixCode = null;
        int sign = 1;
        while(sign!=0){
            sixCode = StringUtil.randomCode();
            int sid = sendObject(761, sixCode);
            String result = ResultPoor.getResult(sid);
            JSONObject ja = JSONObject.parseObject(result);
            int size = ja.getJSONObject("result").getJSONArray("rs").size();
            if (size == 0){
                sign = size;
            }
        }
        return sixCode;
    }

}
