package common;

import cache.AioTcpCache;
import cache.BaseCache;
import cache.ResultPoor;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.io.UnsupportedEncodingException;

/**
 * @ClassName: LoadInvoker
 * @Description: TODO(工程启动加载类)
 * @author jiangshidi
 * @date 2016年9月14日 下午5:42:32
 * 
 */

@WebServlet(loadOnStartup = 1, urlPatterns = { "/dwr-invoker" })
public class LoadInvoker extends HttpServlet {
	/**
	 * @Fields serialVersionUID : TODO(序列化)
	 */
	private static final long serialVersionUID = 1L;

	public void init() {

		// 初始化项目参数
		PropertiesConf pcf = new PropertiesConf();
		pcf.init();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		new Thread() {
			public void run() {
				// 初始化redis连接池token_user
				RedisClient.initialPool(PropertiesConf.REDIS_IP, PropertiesConf.REDIS_PORT, "user_token",0);
				RedisClient.initialPool(PropertiesConf.REDIS_IP, PropertiesConf.REDIS_PORT, "token_user",1);
				RedisClient.initialPool(PropertiesConf.REDIS_IP, PropertiesConf.REDIS_PORT, "phone_verification_code",2);
				RedisClient.initialPool(PropertiesConf.REDIS_IP, PropertiesConf.REDIS_PORT, "service_datacache",3);
				RedisClient.initialPool(PropertiesConf.REDIS_IP, PropertiesConf.REDIS_PORT, "img_code",4);
			};
		}.start();
		
		
		aioclient.AioTcpClient atc = new aioclient.AioTcpClient() {
			@Override
			public void disconnect() {
				status = false;
				while(!reconnect("phantom")) {
					try {
						
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				status = true;
			}
		};
		new Thread() {
			public void run() {
				try {
					String[] uri = PropertiesConf.PHANTOM.split(":");

					atc.start(uri[0], Integer.valueOf(uri[1]), Integer.valueOf(uri[2]));
					// ----链接chaos服务
					int sid1 = ResultPoor.sid.incrementAndGet();

					JSONObject mjo1 = new JSONObject();
					mjo1.put("id", "chaos");
					mjo1.put("sid", sid1);
					mjo1.put("mid", 2);
					atc.send(mjo1.toString());
					Thread.sleep(1000);

					JSONObject jo = JSON.parseObject(ResultPoor.getResult(sid1,2000,1000));
					JSONArray ja = jo.getJSONArray("result");

					for (int i = 0; i < ja.size(); i++) {
						String urix = ja.getString(i);
						System.out.println(urix);
						String[] uri3 = urix.split(":");
						aioclient.AioTcpClient atc1 = new aioclient.AioTcpClient() {
							@Override
							public void disconnect() {
								while(!reconnect("chaos")) {
									try {
										System.out.println("尝试重连chaos："+ AioTcpCache.ctc.monitorMap.get("chaos"));
										Thread.sleep(5000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}
						};
						if(atc1.start(uri3[0], Integer.valueOf(uri3[1]), 10)) {
							System.out.println("开始创建ctc:"+ uri3[0]);
							AioTcpCache.ctc = atc1;
							AioTcpCache.ctcs.add(atc1);
						}
					}
					// ----链接chaos结束

					// 链接gaia服务

					int sid2 = ResultPoor.sid.incrementAndGet();

					JSONObject mjo2 = new JSONObject();
					mjo2.put("id", "gaia");
					mjo2.put("sid", sid2);
					mjo2.put("mid", 2);
					atc.send(mjo2.toString());

					Thread.sleep(500);

					JSONObject jo2 = JSON.parseObject(ResultPoor.getResult(sid2,2000,1000));
					JSONArray ja2 = jo2.getJSONArray("result");

					for (int i = 0; i < ja2.size(); i++) {
						String urix = ja2.getString(i);
						System.out.println(urix);
						String[] uri3 = urix.split(":");
						aioclient.AioTcpClient atc1 = new aioclient.AioTcpClient() {
							@Override
							public void disconnect() {
								while(!reconnect("gaia")) {
									try {
										System.out.println("尝试重连gaia："+ AioTcpCache.gtc.monitorMap.get("gaia"));
										Thread.sleep(5000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}
						};
						if(atc1.start(uri3[0], Integer.valueOf(uri3[1]), 5)) {
							System.out.println("开始创建gtc:"+ uri3[0]);
							AioTcpCache.gtc = atc1;
							break;
						}
					}
					// 链接gaia结束

					while(true) {
						if(!atc.status) {
							Thread.sleep(2000);
							continue;
						}
						
						try {
							int phantomSid = ResultPoor.sid.incrementAndGet();
							int chaosSid = ResultPoor.sid.incrementAndGet();
							int gaiaSid = ResultPoor.sid.incrementAndGet();
							
							JSONObject phantomJo = new JSONObject();
							phantomJo.put("id", "phantom");
							phantomJo.put("sid", phantomSid);
							phantomJo.put("mid", 2);
							atc.send(phantomJo.toString());
							Thread.sleep(2000);
							JSONObject phantomJox = JSON.parseObject(ResultPoor.getResult(phantomSid,2000,1000));
							JSONArray phantomJa = phantomJox.getJSONArray("result");
							atc.monitorMap.put("phantom", phantomJa);
							
							JSONObject chaosJo = new JSONObject();
							chaosJo.put("id", "chaos");
							chaosJo.put("sid", chaosSid);
							chaosJo.put("mid", 2);
							atc.send(chaosJo.toString());
							Thread.sleep(2000);
							JSONObject chaosJox = JSON.parseObject(ResultPoor.getResult(chaosSid,2000,1000));
							JSONArray chaosJa = chaosJox.getJSONArray("result");
							AioTcpCache.ctc.monitorMap.put("chaos", chaosJa);
							
							JSONObject gaiaJo = new JSONObject();
							gaiaJo.put("id", "gaia");
							gaiaJo.put("sid", gaiaSid);
							gaiaJo.put("mid", 2);
							atc.send(gaiaJo.toString());
							Thread.sleep(2000);
							JSONObject gaiaJox = JSON.parseObject(ResultPoor.getResult(gaiaSid,2000,1000));
							JSONArray gaiaJa = gaiaJox.getJSONArray("result");
							AioTcpCache.gtc.monitorMap.put("gaia", gaiaJa);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							Thread.sleep(10000);
						}
					}
					
					
					
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};
		}.start();

		
		new Thread() {
			public void run() {
				BaseCache.timeLoop();
			};
		}.start();
		Thread t1 = new Thread() {
			public void run() {
				while(true) {
		            try {
		                Thread.sleep(480000);
//						MyselfService.agreeWithdraw("2");
		            } catch (InterruptedException e) {
		                e.printStackTrace();
		            }
		        }
			}
		};
		t1.setPriority(1);
		t1.start();


	}
}