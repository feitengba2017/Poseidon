package action.service;


import cache.AioTcpCache;
import cache.ResultPoor;

public class WalletService extends BaseService{
	
	public static String getBalance(String userId) {
		Integer sid = sendObject(213,userId);
		return  ResultPoor.getResult(sid); 
	}
	
	public static String getSelfCommission(String userId) {
		Integer sid = sendObject(214,userId,userId);
		return  ResultPoor.getResult(sid); 
	}
	
	public static String getCommission(String userId) {
		Integer sid = sendObject(215,userId,userId);
		return  ResultPoor.getResult(sid); 
	}
	
	public static String getTodayCommission(String userId,String today) {
		Integer sid = sendObject(216,userId,today);
		return  ResultPoor.getResult(sid); 
	}
	
	public static String getMemberProfitList(String userId,String startDate,String endDate,String orderno,Integer beginNum,Integer endNum ) {
		int orderListId;
    	String sql = " ORDER BY o.pay_date desc";
    	// 分页查询sql
		if(orderno != null && !orderno.equals("")) {
			sql =  " and o.order_no like '%" +orderno+ "%'"+ sql  ;
		}
		if(startDate != null && !startDate.equals("")) {
			sql =  " and substr(o.pay_date,1,6) >= " +startDate+ " "+ sql  ;
		}
        if(endDate != null && !endDate.equals("")){
			sql =  " and substr(o.pay_date,1,6) <= " +endDate+ " "+ sql  ;
		}
		orderListId = sendObject(AioTcpCache.ctc,190,sql,userId,beginNum,endNum);
		
		return  ResultPoor.getResult(orderListId); 
	}
}