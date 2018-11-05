package action.service;

import cache.AioTcpCache;
import cache.ResultPoor;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import common.PropertiesConf;
import exception.SystemException;
import model.Category;

import java.util.ArrayList;
import java.util.List;

public class ClassifyService extends BaseService {
	 
	/**
	 * 商品搜索   默认搜索按照销量排序
	 * @param keySkuName
	 * @param saleMark	        1:降序 
	 * @param priceMark 0:升序	1:降序
	 * @return
	 */
	public static String searchGoods(String keySkuName,String saleMark,String priceMark,Integer beginNum,Integer endNum) {  
    	String sql = "";
    	if(saleMark != null){
    		sql = " order by k.sales_volume desc";
    	}else if(priceMark != null){
    		Integer priceMarkNum = Integer.valueOf(priceMark);
    		if(priceMarkNum == 0){
    			sql = " order by k.market_price asc";
    		}else{
    			sql = " order by k.market_price desc";
    		}
    	}else{
    		//客户不选择，默认上架商品排序    create_time
    		sql = " order by p.create_time desc";   		
    	}   	
         
		int searchGoodsInt =  sendObject(AioTcpCache.ctc,181,sql,PropertiesConf.HESTIA_URL,keySkuName,beginNum,endNum);

		return ResultPoor.getResult(searchGoodsInt);
   	
    }  
    
	/**
	 * 商品分类列表
	 * @return 
	 * @return
	 * @throws SystemException 
	 */
	public static  List<Category> classifyGoods() {  
    	
        //查询商品一级分类    	    	    	
		int firstCatalogInt =  sendObject(182,PropertiesConf.HESTIA_URL);
		String firstCatalogStr = ResultPoor.getResult(firstCatalogInt);
		JSONArray firstCatalogJson =  JSONObject.parseObject(firstCatalogStr).getJSONObject("result").getJSONArray("rs");
		String firstJs= JSONArray.toJSONString(firstCatalogJson);
		List<Category>  firstCatalogList = JSONObject.parseArray(firstJs, Category.class);
		
		//查询商品二级目录
		int secondCatalogInt =  sendObject(183,PropertiesConf.HESTIA_URL);
		String secondCatalogStr = ResultPoor.getResult(secondCatalogInt);
		JSONArray secondCatalogJson =  JSONObject.parseObject(secondCatalogStr).getJSONObject("result").getJSONArray("rs");
		String secondJs= JSONArray.toJSONString(secondCatalogJson);
		List<Category>  secondCatalogList = JSONObject.parseArray(secondJs, Category.class);
		
		//查询商品三级目录
		int thirdCatalogInt =  sendObject(184,PropertiesConf.HESTIA_URL);
		String thirtCatalogStr = ResultPoor.getResult(thirdCatalogInt);
		JSONArray thirdCatalogJson =  JSONObject.parseObject(thirtCatalogStr).getJSONObject("result").getJSONArray("rs");
		String thirdJs= JSONArray.toJSONString(thirdCatalogJson);
		List<Category>  thirdCatalogList = JSONObject.parseArray(thirdJs, Category.class);
		
		//过滤集合数据为空的目录
		for (Category category : firstCatalogList) {
			if(category == null){
				firstCatalogList.remove(category);
			}
		}
		for (Category category : secondCatalogList) {
			if(category == null){
				secondCatalogList.remove(category);
			}
		}
		for (Category category : thirdCatalogList) {
			if(category == null){
				thirdCatalogList.remove(category);
			}
		}
		
	
		
		//第一次挂载	挂载三级目录挂到二级目录
		for (Category seccategory : secondCatalogList) {			
			List<Category>  firstMountSet = new ArrayList<>();
			for (Category thicategory : thirdCatalogList) {
				if(seccategory.getDict_data_value().substring(0, 6).equals(thicategory.getDict_data_value().substring(0, 6))){
					firstMountSet.add(thicategory);
				}
			}
			seccategory.setNext_data(firstMountSet);
		}
		
		//第二次挂载	挂载二级目录挂到一级目录
		for (Category fircategory : firstCatalogList) {		
			List<Category>  secondMountSet = new ArrayList<>();
			for (Category seccategory : secondCatalogList) {
				if(fircategory.getDict_data_value().substring(0,3).equals(seccategory.getDict_data_value().substring(0,3))){
					//如果
					secondMountSet.add(seccategory);
				}
			}
			fircategory.setNext_data(secondMountSet);				
		}								
		
		return firstCatalogList; 	
    }  
    
    /**
	 * 根据商品分类查询商品列表
	 * @param dictDataValue     category_id
	 */
	public static String getGoodsListByCategoryId(String dictDataValue,Integer beginNum,Integer endNum){  		
		String sql = " ORDER BY p.create_time desc ";
		int goodsListId = sendObject(AioTcpCache.ctc,185,sql,PropertiesConf.HESTIA_URL,dictDataValue,beginNum,endNum);	
		return ResultPoor.getResult(goodsListId);
	}

	public static String getZhangDZGoodsList(){
		int sid = sendObject(607);
		return ResultPoor.getResult(sid);
	}

	public static String getPddGoods(int begin, int end){
		int sid = sendObject(629,begin,end);
		return ResultPoor.getResult(sid);
	}

	public static String findPddMenu(){
        int sid = sendObject(632);
        return ResultPoor.getResult(sid);
    }

    public static String findPddGoodsByMenu(int begin,int end,String menuId){
        int sid = sendObject(633,menuId,begin,end);
        return ResultPoor.getResult(sid);
    }


    public static String searchPddGoods(String goodsName,String saleMark,String priceMark,int begin,int end){
		StringBuffer sql = new StringBuffer() ;
		if(null != goodsName || !"".equals(goodsName)){
			sql.append(" where goods_name like '%"+goodsName+"%' ");
		}
		sql.append(" group by goods_id");
		if(saleMark != null){
			sql.append(" order by sold_quantity desc");
		}else if(priceMark != null) {
			Integer priceMarkNum = Integer.valueOf(priceMark);
			if (priceMarkNum == 0) {
				sql.append(" order by min_group_price asc");
			} else {
				sql.append(" order by min_group_price desc");
			}
		}

		int searchGoodsInt =  sendObject(AioTcpCache.ctc,635,sql.toString(),begin,end);
		return ResultPoor.getResult(searchGoodsInt);
	}
}