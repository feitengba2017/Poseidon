package action;

import action.service.MyselfService;
import action.service.UserService;
import action.service.WalletService;
import cache.BaseCache;
import com.alibaba.fastjson.JSONObject;
import servlet.BaseServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@WebServlet(name = "Member", urlPatterns = "/member")
public class MemberAction extends BaseServlet{
	private static final long serialVersionUID = 1L;

	public static String getMember(String token,HttpServletRequest s) {
		String userId = UserService.getUserIdByToken(token);
		
		String balanceText = WalletService.getBalance(userId);
		Integer balance = (Integer) WalletService.getFieldValue(balanceText, "balance", Integer.class);
		
		String selfCommissionText = WalletService.getSelfCommission(userId);
		Integer selfCommission = (Integer) WalletService.getFieldValue(selfCommissionText, "m", Integer.class);
		
		String commissionText = WalletService.getCommission(userId);
		Integer commission = (Integer) WalletService.getFieldValue(commissionText, "m", Integer.class);
		
		String today = BaseCache.getTIME().substring(0, 6);
		
		String todayCommissionText = WalletService.getTodayCommission(userId,today);
		Integer todayCommission = (Integer) WalletService.getFieldValue(todayCommissionText, "m", Integer.class);
		
		HashMap<String,Object> resMap = new HashMap<String,Object>();
		resMap.put("balance", balance==null?0:balance);
		resMap.put("selfCommission", selfCommission==null?0:selfCommission);
		resMap.put("commission", commission==null?0:commission);
		resMap.put("todayCommission", todayCommission==null?0:todayCommission);
		
		return creatResult(1, "", resMap).toString();
	}
	
	public static String getMemberProfitList(String token,String startDate,String endDate,String orderno,String begin,String end) {

		String userId = UserService.getUserIdByToken(token);
		if(userId==null){
			return creatResult(0, "亲，未登录....", null).toString();
		}
		//mysql limit的起始索引为0,便于前台理解，所以用此方式
		Integer beginNum = Integer.valueOf(begin)-1;
		Integer endNum = Integer.valueOf(end);	
		
		String memberProfitLisStr = WalletService.getMemberProfitList(userId,startDate,endDate,orderno,beginNum,endNum);
		JSONObject memberProfitList = JSONObject.parseObject(memberProfitLisStr);
		HashMap<String,Object> resMap = new HashMap<String,Object>();
		resMap.put("memberProfitList", memberProfitList);	
		
		return creatResult(1, "", resMap).toString();
	}

	//微信审核提现
	public static String reviewWithdraw(String token,String jsCode,String amount,String phone,String smsCode){


		String userId = UserService.getUserIdByToken(token);
		if(userId==null){
			return creatResult(0, "亲，未登录....", null).toString();
		}
		int intAmount = Integer.valueOf(amount)*100;
		String res = MyselfService.reviewWithdraw(userId, jsCode, intAmount,phone,smsCode);
		JSONObject addressMsg = JSONObject.parseObject(res);
		return addressMsg.toJSONString();
	}

	//查询提现记录
	public static String getWithdrawRecord(String token){
		String userId = UserService.getUserIdByToken(token);
		if(userId==null){
			return creatResult(0, "亲，未登录....", null).toString();
		}
//		String withdrawRecord = MyselfService.getWithdrawRecord(userId,month_time);
		String withdrawRecord = MyselfService.getWithdrawRecord(userId);
		JSONObject jObject = JSONObject.parseObject(withdrawRecord);
		return jObject.toJSONString();
	}

	/**
	 *
	 * @param memberLevel
	 * @return
	 */
	public String getMemberMessagesInfo(String memberLevel,String messageSign){
		String res =  MyselfService.getMemberMessagesInfo(memberLevel,messageSign);
		return res;
	}

	/**
	 *
	 * @param memberLevel
	 * @return
	 */
	public String messageInfoList(String memberLevel){
		String res = MyselfService.messageInfoList(memberLevel);
		return res;
	}

}