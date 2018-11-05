package action.service;

import cache.AioTcpCache;
import cache.BaseCache;
import cache.ResultPoor;
import common.PropertiesConf;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PlanService extends BaseService {

    /**
     * 查看当前最近一场秒杀场次seckillId
     *
     * @return
     */
    public static int getLastestSeckillId() {
        String currTime = BaseCache.getTIME();
        int sid = sendObject(451, currTime);
        String res = ResultPoor.getResult(sid);
        String seckillId = (String) RecommenService.getFieldValue(res, "id", String.class);
         if (seckillId == null || "0".equals(seckillId)) {
            LocalDateTime arrivalDate = LocalDateTime.now();
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyMMdd");
            String yearMonthDay = arrivalDate.format(format);
            String startFilterTime = yearMonthDay + "000000";
            String endFilterTime = yearMonthDay + "240000";
            String todaySeckillTimeManagerResult = ResultPoor.getResult(sendObject(455, startFilterTime, endFilterTime));
            String seckillTimeId = (String) RecommenService.getFieldValue(todaySeckillTimeManagerResult, "id", String.class);

            if (null == seckillTimeId) {
                return 0;
            } else {
                seckillId = seckillTimeId;
            }

        }
        System.out.println("----------------------- seckillId ---------------------" + seckillId);
        return Integer.parseInt(seckillId);
    }

    public static String getBanner(String districtId) {
        int bannerId;
        int seckillId = getLastestSeckillId();
        String sql = " order by plan_group asc,sort asc";
        if (districtId != null && !districtId.equals("")) {
            sql = " and plan.district_id = ? " + sql;
            bannerId = sendObject(AioTcpCache.getCtc(), 452, sql, PropertiesConf.HESTIA_URL, seckillId, districtId);
        } else {
            bannerId = sendObject(AioTcpCache.getCtc(), 452, sql, PropertiesConf.HESTIA_URL, seckillId);
        }
        return ResultPoor.getResult(bannerId);
    }

    public static String onLoadShowPicture() {
        int sid = sendObject(450, PropertiesConf.HESTIA_URL);
        String res = ResultPoor.getResult(sid);
        return res;
    }

    public static int getSpu(String spuId, String useId) {
        int resultInt = 0;
        if (useId == null) {
            resultInt = sendObject(204, spuId);
        } else {
            resultInt = sendObject(509, useId, spuId);
        }
        return resultInt;
    }

    public static int getSpuAttribute(String spuId) {
        return sendObject(205, spuId, spuId);
    }

    public static int getSkus(String spuId) {
        String currentTime = BaseCache.getTIME();
        return sendObject(462,currentTime,currentTime,currentTime, spuId);
    }

    /**
     * 返回栏目具有的商品信息
     * @param spuId
     * @return
     */
    public static int getPlanGroup(String spuId){
        String currentTime =  BaseCache.getTIME();
        int sid = sendObject(457,currentTime, currentTime, currentTime, spuId);
        return sid;
    }

    public static int getImgs(String imgIds) {
        imgIds = imgIds.equals("") ? "0" : imgIds;
        //if(image.http_path is not null,concat( \"http:\" ,image.http_path) ,concat( ""+ PropertiesConf.HESTIA_URL +"\",image.image_path))
        String sql = "SELECT id,if(http_path is not null,concat(\"http:\" ,http_path) ,concat(\" " + PropertiesConf.HESTIA_URL + "\",image_path))  as image_path FROM uranus.b_image where id in (" + imgIds + ")";
        return sendObject(AioTcpCache.getCtc(), 9999, sql);
    }

    public static String getAdvertBanner(String page_location) {
        String newTime = BaseCache.getTIME();
//		int bannerId;
//		String sql = " order by a.advert_num asc";
//		if (newTime != null && !newTime.equals("")){
//			sql = " and "+newTime+" between a.start_time and a.end_time " + sql;
//		}
//		if(position_id != null && !position_id.equals("")) {
//			sql = " and a.position_id = ? " + sql;
//			bannerId = sendObject(AioTcpCache.getCtc(),729,sql,PropertiesConf.HESTIA_URL,position_id);
//		}else {
//			bannerId = sendObject(AioTcpCache.getCtc(),729,sql,PropertiesConf.HESTIA_URL);
//		}
        int bannerId = sendObject(729, PropertiesConf.HESTIA_URL_CE, newTime, newTime, page_location);
        return ResultPoor.getResult(bannerId);
    }

}