package ocr;
import com.alibaba.fastjson.JSONObject;
import common.PropertiesConf;
import org.apache.commons.collections.map.HashedMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class AuthService {

    static String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
    /**
     * 获取API访问token
     * 该token有一定的有效期，需要自行管理，当失效时需重新获取.
     * @return assess_token 示例：
     * "24.5ae0daed04c1001dd10cfcf2933ebcad.2592000.1535023791.282335-11579875"
     */
    public static Map<String,String> getAuth() {
        // 获取token地址
        String getAccessTokenUrl = authHost
                // 1. grant_type为固定参数
                + "grant_type=client_credentials"
                // 2. 官网获取的 API Key
                + "&client_id=" + PropertiesConf.clientId
                // 3. 官网获取的 Secret Key
                + "&client_secret=" + PropertiesConf.clientSecret;
        try {
            URL realUrl = new URL(getAccessTokenUrl);
            // 打开和URL之间的连接
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            // 获取所有响应头字段
            //Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            /*for (String key : map.keySet()) {
                System.err.println(key + "--->" + map.get(key));
            }*/
            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String result = "";
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            //百度云返回结果
            //System.err.println("result:" + result);
            JSONObject jsonObject = JSONObject.parseObject(result);
            Map<String,String> resultMap = new HashedMap();
            resultMap.put("access_token",jsonObject.getString("access_token"));
            resultMap.put("expires_in",jsonObject.getString("expires_in"));
            return resultMap;
        } catch (Exception e) {
            System.err.printf("获取token失败！");
            e.printStackTrace(System.err);
        }
        return null;
    }


}
