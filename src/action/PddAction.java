package action;

import com.alibaba.fastjson.JSONObject;
import common.PropertiesConf;
import servlet.BaseServlet;
import utils.MD5Util;
import utils.StringUtil;

import javax.servlet.annotation.WebServlet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by 18330 on 2018/8/13.
 */
@WebServlet(name = "Pdd", urlPatterns = "/pdd")
public class PddAction extends BaseServlet {

    private static final long serialVersionUID = 1L;

    public String getPid(){
        String pddFormalUrl = PropertiesConf.PDD_FORMAL_URL;
        String clientId = PropertiesConf.CLIENT_ID;
        String clientSecret = PropertiesConf.CLIENT_SECRET;
//        String timestamp = String.valueOf(System.currentTimeMillis());

        JSONObject pddObj = new JSONObject();
        pddObj.put("client_id",clientId);
        pddObj.put("data_type","JSON");
        pddObj.put("number",1);
        pddObj.put("timestamp",String.valueOf(System.currentTimeMillis()));
        pddObj.put("type","pdd.ddk.goods.pid.generate");
        pddObj.put("sign", MD5Util.sign(StringUtil.createLinkString(pddObj,clientSecret),"UTF-8").toUpperCase());
        StringBuffer json = new StringBuffer();
        PrintWriter out = null;
        try {
            URL url = new URL(pddFormalUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // 发送POST请求必须设置如下两行
            connection.setDoOutput(true);
            connection.setDoInput(true);

            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("POST"); // 设置请求方式
            connection.setRequestProperty("Content-Type", "application/json"); // 设置接收数据的格式
            connection.connect();

            out = new PrintWriter(connection.getOutputStream());
            out.print(pddObj.toString());
            // flush输出流的缓冲
            out.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(),"utf-8"));

            String inputLine = null;
            while ( (inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            System.out.println("url:"+json);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject result = JSONObject.parseObject(json.toString());
        System.out.println("result:"+result);
        String p_id = result.getJSONObject("p_id_generate_response").getJSONArray("p_id_list").getJSONObject(0).getString("p_id");
        System.out.println("p_id:"+p_id);
        return p_id;
    }

    public String getUrlGenerate(String goods_id_list,String phone){

        String p_id = getPid();

        String pddFormalUrl = PropertiesConf.PDD_FORMAL_URL;
        String clientId = PropertiesConf.CLIENT_ID;
        String clientSecret = PropertiesConf.CLIENT_SECRET;

        JSONObject pddObj = new JSONObject();
        pddObj.put("client_id",clientId);
        pddObj.put("custom_parameters",phone);
        pddObj.put("data_type","JSON");
        pddObj.put("generate_short_url",true);
        pddObj.put("generate_we_app",true);
        pddObj.put("goods_id_list",goods_id_list);
        pddObj.put("p_id",p_id);
        pddObj.put("timestamp",String.valueOf(System.currentTimeMillis()));
        pddObj.put("type","pdd.ddk.goods.promotion.url.generate");
        pddObj.put("sign", MD5Util.sign(StringUtil.createLinkString(pddObj,clientSecret),"UTF-8").toUpperCase());
        StringBuffer json = new StringBuffer();
        PrintWriter out = null;
        try {
            URL url = new URL(pddFormalUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // 发送POST请求必须设置如下两行
            connection.setDoOutput(true);
            connection.setDoInput(true);

            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("POST"); // 设置请求方式
            connection.setRequestProperty("Content-Type", "application/json"); // 设置接收数据的格式
            connection.connect();

            out = new PrintWriter(connection.getOutputStream());
            out.print(pddObj.toString());
            // flush输出流的缓冲
            out.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(),"utf-8"));

            String inputLine = null;
            while ( (inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            System.out.println("url:"+json);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject result = JSONObject.parseObject(json.toString());
        HashMap<String, Object> resMap = new HashMap<String, Object>();
        resMap.put("result", result);
        return creatResult(1, "亲,数据包回来了哦...", resMap).toString();
    }

}
