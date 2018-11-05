   package action.service;

   import cache.BaseCache;
   import cache.ResultPoor;

   import java.text.SimpleDateFormat;
   import java.util.ArrayList;
   import java.util.Date;
   import java.util.Random;

   public class FinanceService extends BaseService{

       /**
        * 获取商户订单流水编号
        * @return
        */
       public static String getTradeNo(String useId) {
            SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("yyMMddhhmmss");
             Date date = new Date();
             String str = simpleDateFormat.format(date);
             Random random = new Random();
             int rannum = (int) (random.nextDouble() * (99999 - 10000 + 1)) + Integer.valueOf(useId);// 获取四位随机数
             return  str+String.valueOf(rannum);// 当前时间
       }

       /**
        *财务表插入流水数据
        * @return
        */
       public static String createFinance(String useId,String transactionNo,String transactionBody, String orderNo,String totalPrice,String paymentWayKey) {
           //生成当前时间
           String createData = BaseCache.getTIME();
           //默认币种符号（默认CNY）
           String currencyCode = "CNY";
           //交易状态 未交易 201
           String state = "201";
           int sid = sendObjectCreate(173,transactionNo,orderNo,paymentWayKey,totalPrice,currencyCode,transactionBody,useId,state,createData);
           return ResultPoor.getResult(sid);
       }

       /**
        *查询财务表交易状态
        * @return
        */
       public static String financeStatusFind(String transaction_no) {
           int sid = sendObject(174,transaction_no);
           String states = ResultPoor.getResult(sid);
           return states;
       }

       /**
        *根据订单号查询订单
        * @return
        */
       public static String findOrderByOrderNo(String orderNo) {
           int sid = sendObject(187, orderNo);
           String rsTest = ResultPoor.getResult(sid);
           return rsTest;
       }



       /**
        * 微信回调确认交易   	需要做得三件是工作:1，改变订单状态;2，退回库存;3，修改交易表状态
        * @return
        */
       public static String confirmedFinance(String transactionNo) {
           //生成当前时间
           String nowTime = BaseCache.getTIME();
           //待收货(后台待备货)
           String orderState = "102";
           //完成交易
           String tradeState = "203";

           String sql1 = "UPDATE uranus.b_order SET STATUS = ?, pay_date = ? WHERE order_no = (SELECT order_no FROM uranus.b_transactions WHERE transaction_no = ?)";
           //备注 由于下单已经判断,所以这里减库存的时候不用考虑库存为负数的情况
           String sql2 = "UPDATE uranus.b_sku a,(select sku_id,sku_num from uranus.b_order where order_no = (select order_no from uranus.b_transactions WHERE transaction_no = ? )) AS b SET a.stock = a.stock - b.sku_num WHERE a.id = b.sku_id";
           String sql3 = "UPDATE uranus.b_transactions  SET state = ?,closed_date = ? WHERE transaction_no = ?";
           //事物操作，同时执行多张表 	110
           ArrayList<Object> ay = new ArrayList<Object>();
           //1，改变订单状态
           ay = addTransJa(ay,sql1,orderState,nowTime,transactionNo);
           //2，退回库存;
           ay = addTransJa(ay, sql2, transactionNo);
           //3，修改交易表状态，可选值：201（未交易）202（交易取消）203（交易完成）204（业务关闭）
           ay = addTransJa(ay, sql3, tradeState,nowTime,transactionNo);
           int sid = sendTransJa(ay);
           return ResultPoor.getResult(sid);
       }


   }
