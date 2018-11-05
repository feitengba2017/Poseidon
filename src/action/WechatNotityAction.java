package action;

import action.service.FinanceService;
import action.service.UserService;
import action.service.WechatPayService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import common.PropertiesConf;
import utils.StringUtil;

import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;


/**
 * 微信小程序预支付回调接口
 * @param request
 * @param response
 */
@WebServlet(name = "WechatNotityAction", urlPatterns = "/wechatNotityAction")
public class WechatNotityAction extends HttpServlet{
	
	
	/**  
	* @Fields field:field:{todo}(用一句话描述这个变量表示什么)  
	*/
	private static final long serialVersionUID = 5965308352581194270L;

	@Override
	protected void service (HttpServletRequest request, HttpServletResponse response) {
		System.out.println("-----进入回调----");
        BufferedReader br = null;
        String line = null;  
        Map<String, Object> map = null;
        StringBuilder sb = new StringBuilder();   
        String resXml = "";  
        try {
        	br = new BufferedReader(new InputStreamReader((ServletInputStream)request.getInputStream()));
			while((line = br.readLine()) != null){  
			    sb.append(line);  
			}
			//sb为微信返回的xml  
			String notityXml = sb.toString();  
			System.out.println("接收到的报文：" + notityXml);  
			map = StringUtil.xml2Map(notityXml);
		} catch (Exception e) {
			e.printStackTrace();
		} 
      
        String returnCode = (String) map.get("return_code");  
        String app_key = PropertiesConf.MINI_APP_KEY.trim();
        if("SUCCESS".equals(returnCode)){  
            //验证签名是否正确  
			try {
				if (WechatPayService.checkIsSignValidFromResponseString(map, app_key)) {
					/**此处添加自己的业务逻辑代码start**/  
						//"微信异步结果通知:调用交易确认接口"
						String transactionNo = (String) map.get("out_trade_no");
						String statusStr = FinanceService.financeStatusFind(transactionNo);
						String  state = (String) getFieldValue(statusStr, "state", String.class); 	
						//交易状态，可选值：201（未交易）202（交易取消）203（交易完成）204（业务关闭）
						if(Integer.parseInt(state)==201){
							//1,修改订单状态;2,更新交易表数据;3,商品库存回退;
							FinanceService.confirmedFinance(transactionNo);
						}
						//前面支付成功的前提下，判断该商品订单是是否是非会员礼包操作，如果是做升级用户会员等级操作
						String  orderNo = (String) getFieldValue(statusStr, "order_no", String.class); 
						String rsTest = FinanceService.findOrderByOrderNo(orderNo);
						String orderType = (String) getFieldValue(rsTest, "order_type", String.class); 
						if(orderType.equals("LB")){
							//buyer_id 即为userId
							String userId = (String) getFieldValue(rsTest, "buyer_id", String.class);
							UserService.updataMemberLevel(userId);
						}
											
					/**此处添加自己的业务逻辑代码end**/  
					//通知微信服务器已经支付成功  
					resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"  
							+ "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";  
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
        }else{  
            resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"  
            + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";  
        }  
        System.out.println(resXml);  
        System.out.println("微信支付回调数据结束");  
  
  
        BufferedOutputStream out = null;
		try {
			out = new BufferedOutputStream(response.getOutputStream());
			out.write(resXml.getBytes());
			out.flush();  
			out.close(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
		
	}

	public static Object getFieldValue(String resText, String field,Class<?> c) {
		JSONArray ja = JSONObject.parseObject(resText).getJSONObject("result").getJSONArray("rs");
		if(ja.size() == 0) {
			return null;
		}else {
			return ja.getJSONObject(0).getObject(field,c);
		}
	} 
	
}