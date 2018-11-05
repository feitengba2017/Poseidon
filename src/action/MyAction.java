package action;

import action.service.AddressService;
import action.service.BaseService;
import action.service.UserService;
import cache.ResultPoor;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import servlet.BaseServlet;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "My", urlPatterns = "/my")
public class MyAction extends BaseServlet{
	//
	/**  
	* @Fields field:field:{todo}(用一句话描述这个变量表示什么)  
	*/
	private static final long serialVersionUID = 3277124556953877446L;

	public static String addAddress(String deliveryAddress,String deliveryAddressSec, String consignee, String consigneeTel, String addressName, String districtCode, String isDefault, String token) {
		String userId = UserService.getUserIdByToken(token);
		if(userId==null){
			return creatResult(0, "亲，未登录....", null).toString();
		}
		String addressResult = AddressService.addAddress(deliveryAddress,deliveryAddressSec, consignee, consigneeTel, addressName, districtCode, isDefault, userId);
		JSONObject addressMsg = JSONObject.parseObject(addressResult);
		
		return addressMsg.toJSONString();
	}
	
	public static String editAddress(String deliveryAddress,String deliveryAddressSec,String consignee,String consigneeTel,String addressName,String districtCode,String isDefault,String addressId, String token) {
		String userId = UserService.getUserIdByToken(token);
		if(userId==null){
			return creatResult(0, "亲，未登录....", null).toString();
		}
		String addressResult = AddressService.editAddress(deliveryAddress,deliveryAddressSec, consignee, consigneeTel, addressName, districtCode, isDefault, addressId);
		JSONObject addressMsg = JSONObject.parseObject(addressResult);
		return addressMsg.toJSONString();
	}
	
	public static String delAddress(String addressId, String token) {
		String userId = UserService.getUserIdByToken(token);
		if(userId==null){
			return creatResult(0, "亲，未登录....", null).toString();
		}
		String addressResult = AddressService.delAddress(addressId);
		JSONObject addressMsg = JSONObject.parseObject(addressResult);
		return addressMsg.toJSONString();
	}
	
	public static String serDefault(String addressId, String token) {
		String userId = UserService.getUserIdByToken(token);
		if(userId==null){
			return creatResult(0, "亲，未登录....", null).toString();
		}
		String addressResult = AddressService.serDefault(addressId);
		JSONObject addressMsg = JSONObject.parseObject(addressResult);
		return addressMsg.toJSONString();
	}
	
	public static String getAddress(String token) {
		String userId = UserService.getUserIdByToken(token);
		if(userId==null){
			return creatResult(0, "亲，未登录....", null).toString();
		}
		String addressResult = AddressService.getAddress(userId);
		JSONObject addressMsg = JSONObject.parseObject(addressResult);
		return addressMsg.toJSONString();
	}
	
	public static String deteleBadDate() {
		int[] ids = {13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,102,103,122,126,128,135,136,137,139,140,141,142,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,161,287,305,326,384,385,388,389,391,392,393,394,396,397,399,401,402,454,618,648,696,726,728,735,779,813,822,842,845,892,910,911,966,985,987,1003,1008,1020,1021,1042,1050,1229,1246,1247,1248,1290,1365,1395,1411,1429,1450,1472,1502,1539,1540,1541,1544,1548,1549,1550,1551,1552,1553,1568,1836,1906,2120,2171,2173,2300,2864,2865,2866,2867,2869,2870,2873,2874,2875,2876,2880,2881,2882,2884,2898,2899,3093,3724,3827,3980,4010,4018,4033,4059,4104,4232,4267,4288,4328,4347,4396,4403,4455,4486,4517,4586,4619,4681,4728,4772,4818,4825,4869,4957,5085,5105,5108,5117,5119,5129,5136,5200,5252,5279,5303,5328,5360,5417,5468,5677,5721,5723,5824,5889,5890,6014,6015,6025,6094,6113,6159,6185,6188,6291,6382,6384,6473,6518,6525,6592,6630,6651,6696,6897,7052};
        for(int i=0;i<ids.length;i++){
        	int sid = BaseService.sendObject(515,ids[i]);
        	String resText = ResultPoor.getResult(sid);
    		JSONArray ja = JSONObject.parseObject(resText).getJSONObject("result").getJSONArray("rs");
    		for(int x = 1;x< ja.size();x++){
    			JSONObject sJsonObject = (JSONObject) ja.get(x);
    			BaseService.sendObjectCreate(516,sJsonObject.get("id"));
    		}
        }	
		return "垃圾数据删除成功";
	}
	
}
	
