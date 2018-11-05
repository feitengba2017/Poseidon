package cache;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseCache{
	private static String TIME;
	
	public static String getTIME() {
		return TIME;
	}
	
	public static void timeLoop() {
		SimpleDateFormat df = new SimpleDateFormat("YYMMddHHmmss");//设置日期格式
		while(true) {
			TIME = df.format(new Date());
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}