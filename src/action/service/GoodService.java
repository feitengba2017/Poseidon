package action.service;

import cache.ResultPoor;

public class GoodService extends BaseService {
	
	/**  
	* @Title: getGoodsBySkuId  
	* @Description: TODO(根据sku_id取商品信息)  
	* @param @return    参数  
	* @return String    返回类型  
	* @throws  
	*/
	public static String getGoodsBySkuId(String skuId) {
		int sid = sendObject(171, skuId);
		String result_str = ResultPoor.getResult(sid);
		return result_str;		
	}
	
	/**  
	* @Title: descGoodsBySkuId  
	* @Description: TODO(根据sku_id减商品库存)  
	* @param @return    参数  
	* @return String    返回类型  
	* @throws  
	*/
	public static String descGoodsBySkuId(String skuId) {
		int sid = sendObject(171, skuId);
		String stock = ResultPoor.getResult(sid);
		return stock;		
	}
	
	/**  
	* @Title: descGoodsBySkuId  
	* @Description: TODO(根据sku_id减商品库存)  
	* @param @return    参数  
	* @return String    返回类型  
	* @throws  
	*/
	public static String userPurchase(String useId,String skuId) {
		int sid = sendObject(194,useId,skuId);
		String stock = ResultPoor.getResult(sid);
		return stock;		
	}
}