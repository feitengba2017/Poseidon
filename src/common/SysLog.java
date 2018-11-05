package common;

import action.service.BaseService;
import cache.ResultPoor;

public class SysLog{
	public static void creatServiceLog(String logCode,String serviceCode,String inParameters,String outParameters,String errorMsg,String memo,String log_time) {
		
		int sid = BaseService.sendObjectCreate(760, logCode, serviceCode, inParameters, outParameters, errorMsg, memo,log_time);
		ResultPoor.getResult(sid);
		
		
	}
}