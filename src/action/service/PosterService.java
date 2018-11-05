package action.service;

import cache.ResultPoor;
import common.PropertiesConf;

public class PosterService extends BaseService {

    /**
     * 点击海报返回图片分类
     * @return
     */
    public static String getPosterCategory(){
        int sid = sendObject(481);
        String res = ResultPoor.getResult(sid);
        return res;
    }

    /**
     *
     * @return
     */
    public static String getPosterPictures(int begin,int end){
        int sid = sendObject(492, PropertiesConf.HESTIA_URL,begin,end);
        String res = ResultPoor.getResult(sid);
        return res;
    }

    /**
     * 点击图片分类 查看图片集合
     * @param categoryId
     * @return
     */
    public static String selectPosterCategory(String categoryId,int begin,int end){
        int sid= sendObject(491,PropertiesConf.HESTIA_URL, categoryId,begin,end);
        String res = ResultPoor.getResult(sid);
        return res;
    }

}
