package action;

import action.service.RecommenService;
import cache.BaseCache;
import common.RedisClient;
import servlet.BaseServlet;

import javax.servlet.annotation.WebServlet;

/**
 * @author cuiw
 */
@WebServlet(name = "Recommen", urlPatterns = "/recommen")
public class RecommonAction extends BaseServlet {
    /**
     * 查询出每个栏目上方 展示图片
     * 其中秒杀专区 传送 秒杀最近时段
     * @return
     */
    public String getColumnListInfo(){
//        String getColumnListInfo = RedisClient.hget("service_datacache","getColumnListInfo","columnListInfo_datacache");
//        if (null != getColumnListInfo) {
//            return getColumnListInfo;
//        }
        String res = RecommenService.getColumnListInfo();
//        RedisClient.hset("service_datacache","getColumnListInfo","columnListInfo_datacache",res,3);
        return res;
    }

    /**
     * 查询秒杀列表页时刻表
     * @return
     */
    public String getSeckillTimeList(){
        String currentTime =  BaseCache.getTIME();
        String res =  RecommenService.getSeckillTimeList();
        return res;
    }

    /**
     *
     * @param seckillId
     * @return
     */
    public String getSeckillGoodsList(String seckillId){
        String getSeckillGoodsList = RedisClient.hget("service_datacache","getSeckillGoodsList"+seckillId,"getSeckillGoodsList_datacache_"+seckillId);
        if (null != getSeckillGoodsList) {
            return getSeckillGoodsList;
        }
        if (null == seckillId) {
            seckillId = "";
        }
        getSeckillGoodsList =  RecommenService.getSeckillGoodsList(seckillId);
        RedisClient.hset("service_datacache","getSeckillGoodsList"+seckillId,"getSeckillGoodsList_datacache_"+seckillId,getSeckillGoodsList,5);
        return getSeckillGoodsList;
    }
}
