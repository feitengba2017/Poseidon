package action;

import action.service.ClassifyService;
import com.alibaba.fastjson.JSONObject;
import common.RedisClient;
import model.Category;
import servlet.BaseServlet;
import utils.QRCodeUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;

@WebServlet(name = "Classify", urlPatterns = "/classify")
public class ClassifyAction extends BaseServlet{
		private static final long serialVersionUID = 1L;
		

		/**
		 * 商品搜索
		 * @param keySkuName
		 * @param saleMark	0:升序          1:降序 
		 * @param priceMark 0:升序	1:降序
		 * @return
		 */
		public static String searchGoods(String keySkuName,String saleMark,String priceMark,String begin,String end) { 
			//mysql limit的起始索引为0,便于前台理解，所以用此方式
			Integer beginNum = Integer.valueOf(begin)-1;
			Integer endNum = Integer.valueOf(end);					
			String searchGoodsStr = ClassifyService.searchGoods(keySkuName, saleMark, priceMark,beginNum,endNum);
			JSONObject searchGoods = JSONObject.parseObject(searchGoodsStr);
			HashMap<String,Object> resMap = new HashMap<String,Object>();
			resMap.put("searchGoods", searchGoods);
			return creatResult(1, "亲,数据包回来了哦...",resMap ).toString();
		}
		
		/**
		 * 商品分类
		 */
		public static String classifyGoods(){   	
			//判断redis里是否有缓存，如果有直接取,否则走数据查询，完了再存到redis		
			String classifyGoodsDatacache = RedisClient.hget("service_datacache","classifyGoods","classifyGoods_datacache");
			if(classifyGoodsDatacache != null){
				return classifyGoodsDatacache;
			}
			List<Category> classifyGoods = ClassifyService.classifyGoods();	
			String classifyGoodsResult = JSONObject.toJSONString(classifyGoods);
			RedisClient.hset("service_datacache","classifyGoods","classifyGoods_datacache",classifyGoodsResult,300);		
			return classifyGoodsResult;
		} 
		
		/**
		 * 根据商品分类查询商品列表
		 * @param dictDataValue     category_id
		 */
		public static String getGoodsListByCategoryId(String dictDataValue,String begin,String end){
			//mysql limit的起始索引为0,便于前台理解，所以用此方式
			Integer beginNum = Integer.valueOf(begin)-1;
			Integer endNum = Integer.valueOf(end);
			String  getGoodsListstr = ClassifyService.getGoodsListByCategoryId(dictDataValue,beginNum,endNum);
			JSONObject getGoodsList = JSONObject.parseObject(getGoodsListstr);
			
			HashMap<String,Object> resMap = new HashMap<String,Object>();
			resMap.put("getGoodsList", getGoodsList);
			return creatResult(1, "",resMap).toString();
		}

	/**
	 * 获取商品的小程序码
	 * access_token 小程序access_token
	 * scene 小程序码参数
	 * path 页面路径
	 * width 图片大小
	 */
	public static void getGoodsQR(String access_token, String scene, String path, String width, HttpServletResponse response){
		 QRCodeUtil.getQRCode(access_token, scene, path, Integer.valueOf(width),response);
	}

	/**
	 * 获取掌达赚的商品列表
	 * @return
	 */
	public static String getZhangDZGoods(){
		String zhangDZGoodsList = ClassifyService.getZhangDZGoodsList();
		JSONObject zhangDZGoods = JSONObject.parseObject(zhangDZGoodsList);
		HashMap<String,Object> resMap = new HashMap<String,Object>();
		resMap.put("zhangDZGoods", zhangDZGoods);
		return creatResult(1, "亲,数据包回来了哦...",resMap ).toString();
	}

	/**
	 * 获取拼多多商品列表  (暂时不用)
	 */
	public static String getPddGoods(String page, String limit){
		int pageI = Integer.valueOf(page);
		int limitI = Integer.valueOf(100);
        String pddGoods = ClassifyService.getPddGoods(pageI, limitI);
        JSONObject pddGoodsMap = JSONObject.parseObject(pddGoods);
        HashMap<String,Object> resMap = new HashMap<>();
        resMap.put("pddGoods",pddGoodsMap);
        return creatResult(1, "亲,数据包回来了哦...",resMap ).toString();
    }

    /*public static HashMap<String, Object> classification(String goods){
		JSONObject pddGoodsMap = JSONObject.parseObject(goods);
		JSONArray goodsArray = JSONObject.parseObject(pddGoodsMap.get("result").toString()).getJSONArray("rs");
		Map<String,Object> map = new HashMap<>();
		for (Object goodsJson:goodsArray) {
			JSONObject jsonObject = JSONObject.parseObject(goodsJson.toString());
			String opt_id = jsonObject.getString("opt_id");
			if("1".equals(opt_id)){
				map.put("1",goodsJson);
			}else if("4".equals(opt_id)){
				map.put("4",goodsJson);
			}
		}
		JSONObject jsonObject = JSONObject.parseObject(map.toString());
		HashMap<String,Object> resMap = new HashMap<>();
		resMap.put("pddGoods",map);
		return resMap;
	}*/

	/**
	 * 获取拼多多商品列表  (正在使用中)
	 */
    public static String getPddMenu(String page, String limit){
		int pageI = Integer.valueOf(page);
		int limitI = Integer.valueOf(limit);
		String pddMenu = ClassifyService.findPddMenu();
		JSONObject jsonMenu = JSONObject.parseObject(pddMenu);
		String pddGoodsByMenu = ClassifyService.findPddGoodsByMenu(pageI, limitI, "1");
		JSONObject jsonGoods = JSONObject.parseObject(pddGoodsByMenu);
		HashMap<String,Object> map = new HashMap<>();
		map.put("pddMenu",jsonMenu);
		map.put("pddGoods",jsonGoods);
		return creatResult(1,"亲,数据包回来了哦...",map).toString();
	}

	public static String getPddGoodsByMenu(String page, String limit,String menuId){
		int pageI = Integer.valueOf(page);
		int limitI = Integer.valueOf(limit);
		String pddGoodsByMenu = ClassifyService.findPddGoodsByMenu(pageI, limitI, menuId);
		JSONObject jsonGoods = JSONObject.parseObject(pddGoodsByMenu);
		HashMap<String,Object> map = new HashMap<>();
		map.put("pddGoods",jsonGoods);
		return creatResult(1,"亲,数据包回来了哦...",map).toString();
	}

	public static String searchPddGoods(String goodsName,String saleMark,String priceMark,String begin,String end){
		Integer beginNum = Integer.valueOf(begin)-1;
		Integer endNum = Integer.valueOf(end);
		String searchPddGoodsStr = ClassifyService.searchPddGoods(goodsName,saleMark,priceMark,beginNum,endNum);
		JSONObject searchPddGoods = JSONObject.parseObject(searchPddGoodsStr);
		String searchGoodsStr = ClassifyService.searchGoods(goodsName, saleMark, priceMark,beginNum,endNum);
		JSONObject searchGoods = JSONObject.parseObject(searchGoodsStr);
		HashMap<String,Object> resMap = new HashMap<String,Object>();
		resMap.put("searchPddGoods",searchPddGoods);
		resMap.put("searchGoods", searchGoods);
		return creatResult(1, "亲,数据包回来了哦...",resMap ).toString();
	}
}