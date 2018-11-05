
package common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesConf{
	
	public static Integer SOCKETPORT;
	
	public static String PHANTOM;
	
	public static String REDIS_IP;
	
	public static Integer REDIS_PORT;
	
	public static String APP_ID;
	
	public static String APP_SECRET;
	
	public static String WETCAT_URL;
	
	public static String MINI_MCH_ID;
	
	public static String MINI_APP_KEY;
	
	public static String MINI_NOTIFY_URL;
	
	public static String HESTIA_URL;

	public static String HESTIA_URL_CE;

	public static String LOGISTIC_NORMAL;
	// 正式环境物流请求接口路径
	public static String LOGISTIC_KEY;
	// 申请正式物流KEY
	public static String CUSTOMERID;
	// 申请企业版本获得
	public static String uploadBackOrderImagePath;
	// 回填单上传图片地址
	public static String merchantCertificatePath;
	// 商户证书地址

	public static String clientId;
	public static String clientSecret;

	//拼多多 client_id
	public static String CLIENT_ID;
	//拼多多client_secret
	public static String CLIENT_SECRET;
	//拼多多请求地址
	public static String PDD_FORMAL_URL;

	public void init(){
		Properties prop = new Properties();
		InputStream in = getClass().getResourceAsStream("/properties/BaseConnect.properties");
		try {
			prop.load(in);
			
			SOCKETPORT = Integer.valueOf((String) prop.get("socketPort"));
			
			PHANTOM = (String) prop.get("phantom");
			
			String redisPath = prop.getProperty("redis");
			REDIS_IP = redisPath.split(":")[0];
			REDIS_PORT = Integer.valueOf(redisPath.split(":")[1]);
			
			APP_ID = (String) prop.get("app_id");
			APP_SECRET = (String) prop.get("app_key");
			WETCAT_URL = (String) prop.get("wetcat_url");
			
			MINI_MCH_ID = (String) prop.get("mini_mch_id");
			MINI_APP_KEY = (String) prop.get("mini_app_key");
			MINI_NOTIFY_URL = (String) prop.get("mini_notify_url");
			
			HESTIA_URL = (String) prop.get("hestia");

			HESTIA_URL_CE = (String) prop.get("hestia_ce");

			// 物流信息
			LOGISTIC_NORMAL = (String) prop.get("logistic_normal");
			LOGISTIC_KEY = (String) prop.get("logistic_key");
			CUSTOMERID = (String) prop.get("customerId");

			uploadBackOrderImagePath = (String) prop.get("uploadBackOrderImagePath");

			merchantCertificatePath = (String) prop.get("merchantCertificatePath");

			//百度云图像识别参数
			clientId = (String) prop.get("clientId");
			clientSecret = (String) prop.get("clientSecret");

			CLIENT_ID = (String) prop.get("client_id");
			CLIENT_SECRET = (String) prop.get("client_secret");
			PDD_FORMAL_URL = (String) prop.get("pdd_formal_url");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}