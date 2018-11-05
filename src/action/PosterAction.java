package action;

import action.service.PosterService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import servlet.BaseServlet;
import javax.servlet.annotation.WebServlet;
import java.util.HashMap;

@WebServlet(name = "Poster", urlPatterns = "/poster")
public class PosterAction extends BaseServlet {
    /**
     *
     * @param page
     * @param limit
     * @return
     */
    public String getPosterLists(String page,String limit){
        int pageI = Integer.valueOf(page);
        int limitI = Integer.valueOf(limit);
        HashMap<String,Object> resMap = new HashMap<String,Object>();
        String categoryText = PosterService.getPosterCategory();
        JSONObject categoryJo = JSONObject.parseObject(categoryText);
        resMap.put("posterCategory",categoryJo.getJSONObject("result").getJSONArray("rs"));

        String pictureText = PosterService.getPosterPictures((pageI-1)*limitI,limitI);
        JSONObject picturesJO = JSON.parseObject(pictureText);
        resMap.put("posterPictures",picturesJO.getJSONObject("result").getJSONArray("rs"));

        return creatResult(1, "", resMap).toString();
    }

    /**
     *
     * @param categoryId
     * @param page
     * @param limit
     * @return
     */
    public String selectPosterCategory(String categoryId,String page,String limit){
        int pageI = Integer.valueOf(page);
        int limitI = Integer.valueOf(limit);
        String res = PosterService.selectPosterCategory(categoryId,(pageI -1)*limitI,limitI);
        return res;

    }
}
