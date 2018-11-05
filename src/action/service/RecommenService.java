package action.service;

import cache.BaseCache;
import cache.ResultPoor;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import common.PropertiesConf;
import model.ColumnInfoVO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cuiw
 */
public class RecommenService extends BaseService {
    /**
     * 栏目商品列表
     *
     * @return
     */
    public static String getColumnListInfo() {
        String seckillId = "";
        String planGroup = "";
        String imagePath = "";
        String productLinkFlag = "";
        String urlLink = "";
        int clinkId = 0;

        ColumnInfoVO ci = null;
        HashMap<String, Object> resMap = null;

        /**
         * 传递 栏目ID 栏目展示图片
         */
        int cid = sendObject(450, PropertiesConf.HESTIA_URL, 0, 2);
        String columnInfoResult = ResultPoor.getResult(cid);
        JSONObject result = JSONObject.parseObject(columnInfoResult).getJSONObject("result");
        JSONObject resultObject = JSONObject.parseObject(result.toJSONString());
        Object tmp = resultObject.get("total");
        int total = (int) tmp;
        if (total == 0) {
            return creatResult(2, "total is 0 ", null).toString();
        } else {
            resMap = new HashMap<String, Object>();
            for (int i = 0; i < total; i++) {
                ci = new ColumnInfoVO();
                planGroup = (String) RecommenService.getFieldEachValue(columnInfoResult, "plan_group", String.class, i);
                imagePath = (String) RecommenService.getFieldEachValue(columnInfoResult, "image", String.class, i);
                productLinkFlag = (String) RecommenService.getFieldEachValue(columnInfoResult, "productLinkFlag", String.class, i);
                clinkId = (Integer) RecommenService.getFieldEachValue(columnInfoResult, "clinkId", Integer.class, i);
                urlLink = (String) RecommenService.getFieldEachValue(columnInfoResult, "urlLink", String.class, i);
                ci.setSeckillid(seckillId);
                ci.setPlanGroup(planGroup);
                ci.setImagePath(imagePath);
                ci.setSekillStartTime("");
                ci.setSekillEndTime("");
                ci.setMessage("");
                ci.setProductLinkFlag(productLinkFlag);
                ci.setClinkId(clinkId);
                ci.setUrlLink(urlLink);
                ci.setInfo("");
                if ("1".equals(planGroup)) {
                    /**
                     * 秒杀专区 需要传送 秒杀时间段
                     */
                    String currTime = BaseCache.getTIME();
                    int sid = sendObject(451, currTime);
                    String res = ResultPoor.getResult(sid);
                    String seckillStartTimeZONE = (String) RecommenService.getFieldValue(res, "start_time", String.class);
                    /**
                     * 为空说明 今天秒杀时刻已经完事
                     */
                    String sekillStartTime = "";
                    String sekillEndTime = "";
                    if (seckillStartTimeZONE == null) {
                    /**
                     * 只有当查询完今天00:00-24:00 确实都没有设置场次 才能message=3
                     */
                        LocalDateTime arrivalDate = LocalDateTime.now();
                        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyMMdd");
                        String yearMonthDay = arrivalDate.format(format);
                        String startFilterTime = yearMonthDay + "000000";
                        String endFilterTime = yearMonthDay + "240000";
                        String todaySeckillTimeManagerResult =  ResultPoor.getResult(sendObject(455,startFilterTime,endFilterTime));
                        String seckillTimeId = (String) RecommenService.getFieldValue(todaySeckillTimeManagerResult, "id", String.class);
                        if (null == seckillTimeId) {
                            /**
                             * 说明今天的场次没设置
                             */
                            ci.setSekillStartTime("333333333333");
                            ci.setSeckillid("0");
                            ci.setSekillEndTime("333333333333");
                            ci.setMessage("3");
                            ci.setInfo("运营后台没有设置今天的秒杀场次");
                        }else{
                            ci.setSekillStartTime("222222222222");
                            ci.setSeckillid("0");
                            ci.setSekillEndTime("222222222222");
                            ci.setMessage("2");
                            ci.setInfo("今天的秒杀场次已经结束,请明天再来");
                        }
                    } else {
                        String seckillEndTimeZONE = (String) RecommenService.getFieldValue(res, "end_time", String.class);
                        seckillId = (String) RecommenService.getFieldEachValue(res, "id", String.class, i);
                        ci.setSeckillid(seckillId);
                        ci.setSekillStartTime(seckillStartTimeZONE);
                        ci.setSekillEndTime(seckillEndTimeZONE);
                        ci.setMessage("1");
                        ci.setInfo("-"+ci.getSekillStartTime()+"-");
                    }
                }
                Object ciJson = JSON.toJSON(ci);
                System.out.println("  ciJSON   " + ciJson);
                resMap.put("planGroup"+i, ciJson);
            }
        }
        return creatResultJSON(1, "success", resMap).toString();
    }

    /**
     * 重写专用
     *
     * @param success
     * @param errorMessage
     * @param map
     * @return
     */
    public static JSONObject creatResultJSON(int success, String errorMessage, HashMap<String, Object> map) {
        JSONObject jo = new JSONObject();
        jo.put("success", success);
        jo.put("errorMessage", errorMessage);

        if (map != null) {
            JSONObject jors = new JSONObject();
            jors.put("total", map.size());
            JSONArray ja = new JSONArray();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                System.out.println("key =" + key + " value = " + value);
                JSONObject joo = new JSONObject();
                joo.put(key, value);
                ja.add(joo);
            }

            jors.put("rs", ja);
            jo.put("result", jors);
        }
        return jo;
    }

    /**
     * 查找当天秒杀时段
     * @return
     */
    public static String getSeckillTimeList(){
        LocalDateTime arrivalDate = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyMMdd");
        String yearMonthDay = arrivalDate.format(format);
        String startFilterTime = yearMonthDay + "000000";
        String endFilterTime = yearMonthDay + "240000";
        String currentTime = BaseCache.getTIME();
        String todaySeckillTimeManagerResult =  ResultPoor.getResult(sendObject(453,currentTime,currentTime,currentTime,startFilterTime,endFilterTime));
        return todaySeckillTimeManagerResult;
    }

    /**
     * 查询秒杀单场商品
     * @param seckillId
     * @return
     */
    public static String getSeckillGoodsList(String seckillId){
        int sid = sendObject(454,PropertiesConf.HESTIA_URL, seckillId);
        String res =  ResultPoor.getResult(sid);
        return res;
    }
}
