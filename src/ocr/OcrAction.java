package ocr;

import com.alibaba.fastjson.JSONObject;
import common.RedisClient;
import utils.Base64Util;
import utils.FileUtil;
import utils.HttpUtil;

import java.net.URLEncoder;
import java.util.Map;

public class OcrAction {
    // 通用识别url(高精度版)
    static String otherHost = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic";
    /**
     * 重要提示代码中所需工具类
     * FileUtil,Base64Util,HttpUtil,GsonUtils请从
     * https://ai.baidu.com/file/658A35ABAB2D404FBF903F64D47C1F72
     * https://ai.baidu.com/file/C8D81F3301E24D2892968F09AE1AD6E2
     * https://ai.baidu.com/file/544D677F5D4E4F17B4122FBD60DB82B3
     * https://ai.baidu.com/file/470B3ACCA3FE43788B5A963BF0B625F3
     * 下载
     */
    public static JSONObject orc(String filePath) {
        try {
            byte[] imgData = FileUtil.readFileByBytes(filePath);
            String imgStr = Base64Util.encode(imgData);
            String params = URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(imgStr, "UTF-8");
            /**
             * 线上环境access_token有过期时间， 缓存进redis，30天过期后重新获取。
             */
            String accessToken = RedisClient.hget("service_datacache","orcAccess_token","orcAccessToken_datacache");
            if(null == accessToken) {
                Map<String, String> resultMap = AuthService.getAuth();
                RedisClient.hset("service_datacache", "orcAccess_token", "orcAccessToken_datacache", resultMap.get("access_token"), Integer.valueOf(resultMap.get("expires_in")));
                accessToken = resultMap.get("access_token");
            }
            System.out.println("++authtoken++"+accessToken);
            String result = HttpUtil.post(otherHost, accessToken, params);
            System.out.println("orcResult: "+result);
            return JSONObject.parseObject(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
