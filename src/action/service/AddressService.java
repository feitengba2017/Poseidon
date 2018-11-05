package action.service;

import cache.ResultPoor;

public class AddressService  extends BaseService {
	public static String getAddress(String userId) {
		Integer sid = sendObject(207,userId);
		return  ResultPoor.getResult(sid); 
	}
	//INSERT INTO `uranus`.`b_address` ( `delivery_address`, `consignee`, `consignee_tel`, `address_name`, `district_code`, `is_default`, `user_id`) VALUES ( ?, ?, ?, ?, ?, ?, ?)
	public static String addAddress(String deliveryAddress,String deliveryAddressSec, String consignee, String consigneeTel, String addressName, String districtCode, String isDefault, String userId) {
		//如果用户选择默认的情况下添加新住址,需要重置其他的默认状态
		Integer isDefaultInt = Integer.valueOf(isDefault);
		if(isDefaultInt == 1){
			sendObjectCreate(197,userId);
		}
		Integer sid = sendObjectCreate(208,deliveryAddress,deliveryAddressSec,consignee,consigneeTel,addressName,districtCode,isDefault,userId);
		return  ResultPoor.getResult(sid); 
	}
	//UPDATE `uranus`.`b_address` SET `deliveryAddress` = ?, `consignee` =?, `consigneeTel` = ?, `addressName` =?, `districtCode` =?, `isDefault` =? WHERE `id` = ? 
	public static String editAddress(String deliveryAddress,String deliveryAddressSec,String consignee,String consigneeTel,String addressName,String districtCode,String isDefault,String id) {
		Integer sid = sendObjectCreate(209,deliveryAddress,deliveryAddressSec,consignee,consigneeTel,addressName,districtCode,isDefault,id);
		return  ResultPoor.getResult(sid); 
	}
	
	public static String delAddress(String addressId) {
		Integer sid = sendObjectCreate(210,addressId);
		return  ResultPoor.getResult(sid); 
	}
	
	public static String serDefault(String addressId) {
		Integer udef = sendObjectCreate(212,addressId);
		ResultPoor.getResult(udef); 
		Integer sdef = sendObjectCreate(211,addressId);
		return  ResultPoor.getResult(sdef); 
	}
}