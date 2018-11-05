package utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import common.PropertiesConf;
import ocr.OcrAction;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadImageUtil {

    public static Map<String, String> uploadImg(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        String imagePath = "", fileName = null, orderNo = null, remark = null, token = null, goodsName = null, skuId = null;
        FileItem files = null;
        String payTime = null;
        InputStream in;
        OutputStream ops;
        String realFileName = "";
        // 重命名
        String extName = "";
        // 文件后缀
        //imagePath = "D:\\upload\\";
        imagePath = PropertiesConf.uploadBackOrderImagePath;
        File zz = new File(imagePath);
        if (!zz.exists()) {
            zz.mkdir();
        }
        try {
            request.setCharacterEncoding("utf-8");
            //2、获得磁盘文件条目工厂
            DiskFileItemFactory factory = new DiskFileItemFactory();
            //3、创建一个文件上传解析器
            ServletFileUpload upload = new ServletFileUpload(factory);
            //4、解决上传文件名的中文乱码
            upload.setHeaderEncoding("utf-8");
            //5、得到FileItem的集合items
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (isMultipart) {
                List<FileItem> fileItems = upload.parseRequest(request);
                //6、遍历items
                for (FileItem item : fileItems) {
                    String fieldName = item.getFieldName();
                    SimpleDateFormat sdf1 = new SimpleDateFormat("YYYYMMddHHmmss");
                    Date date = new Date();
                    // 上传文件命名
                    realFileName = sdf1.format(date);
                    fileName = item.getName();

                    System.out.println(" filedName: " + fieldName);
                    //若是一个一般的表单域，打印信息
                    if (item.isFormField()) {
                        String value = item.getString("utf-8");
                        if ("orderNo".equals(fieldName)) {
                            orderNo = value;
                        } else if ("remark".equals(fieldName)) {
                            remark = value;
                        } else if ("token".equals(fieldName)) {
                            token = value;
                        } else if ("goodsName".equals(fieldName)) {
                            goodsName = value;
                        } else if ("skuid".equals(fieldName)) {
                            skuId = value;
                        }
                    } else {
                        if ("file".equals(fieldName)) {
                            files = item;
                        }
                    }
                }
                //上传文件
                if (files == null) {

                } else {

                    try {
                        extName = fileName.substring(fileName.lastIndexOf("."));//.jpg
                        // 文件后缀名字
                        //imagePath = "D:\\upload\\" + realFileName + extName;
                        imagePath = PropertiesConf.uploadBackOrderImagePath + realFileName + extName;
                        ops = new FileOutputStream(imagePath);
                        in = files.getInputStream();
                        byte[] buff = new byte[1024];
                        int rc = 0;
                        while ((rc = in.read(buff)) > 0) {
                            ops.write(buff, 0, rc);
                        }

                        ops.close();
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //图片识别
                        try {
                            //JSONObject orc = OcrAction.orc("D:\\upload\\" + realFileName + extName);
                            JSONObject orc = OcrAction.orc(imagePath);
                            if (null != orc) {
                                Object words_result = orc.get("words_result");
                                JSONArray wordsArray = JSONArray.parseArray(words_result.toString());
                                /*if(goodsName == null || "".equals(goodsName)) {
                                    Object o1 = wordsArray.get(5);
                                    JSONObject goodsWord = JSONObject.parseObject(o1.toString());
                                    goodsName = goodsWord.get("words").toString();
                                }*/
                                for (int i=0;i<wordsArray.size(); i++) {
                                    JSONObject jsonWords = JSONObject.parseObject(wordsArray.get(i).toString());
                                    String words = jsonWords.get("words").toString();
                                    if(words.indexOf("￥") !=-1){
                                        if(goodsName == null || "".equals(goodsName)){
                                            if(words.length()<7){
                                                JSONObject jsonWords_1 = JSONObject.parseObject(wordsArray.get(i-1).toString());
                                                String words_1 = jsonWords_1.get("words").toString();
                                                goodsName = words_1.substring(6, words_1.length()-1);
                                            }else {
                                                goodsName = words.substring(6, words.indexOf("￥"));
                                            }
                                        }
                                    } else if(orderNo == null || "".equals(orderNo)){
                                        if (words.indexOf("订单编号") != -1) {
                                            orderNo = words.substring(words.indexOf(":") + 1, words.length());
                                        }
                                    } else if (words.indexOf("付款时间") != -1) {
                                        String payDateTime = words.substring(words.indexOf(":") + 1, words.length());
                                        DateFormat format1 = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                                        Date parse = format1.parse(payDateTime);
                                        SimpleDateFormat df = new SimpleDateFormat("YYMMddHHmmss");//设置日期格式
                                        payTime = df.format(parse);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("==========图片无法正确识别=========");
                            e.printStackTrace();
                        }
                }
                map.put("orderNo", orderNo);
                map.put("goodsName", goodsName);
                map.put("remark", remark);
                map.put("token", token);
                String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/hestia/backOrder/" + realFileName + extName;
                //String basePath = "http://10.1.1.96:8084/upload/"+realFileName + extName;
                map.put("imagePath", basePath);
                map.put("skuId", skuId);
                map.put("payTime", payTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
