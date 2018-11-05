package model;

/**
 * @author cuiw
 */
public class ColumnInfoVO {
    /**
     * 栏目ID
     */
    private String planGroup;
    /**
     * 栏目展示图标
     */
    private String imagePath;
    /**
     * 提示
     */
    private String message;
    /**
     * 秒杀开始时间
     */
    private String sekillStartTime;
    /**
     * 秒杀结束时间
     */
    private String sekillEndTime;

    /**
     * 商品开关
     */
    private String productLinkFlag;

    /**
     * 栏目外链接地址ID
     */
    private int clinkId;

    /**
     * 栏目外链接url
     */
    private String urlLink;

    /**
     * 提示前台信息
     */
    private String Info;
    /**
     * 秒杀时段ID
     */
    private String seckillid;

    public ColumnInfoVO(){

    }

    public void setSeckillid(String seckillid) {
        this.seckillid = seckillid;
    }

    public String getSeckillid() {
        return seckillid;
    }

    public void setPlanGroup(String planGroup) {
        this.planGroup = planGroup;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSekillStartTime(String sekillStartTime) {
        this.sekillStartTime = sekillStartTime;
    }

    public void setSekillEndTime(String sekillEndTime) {
        this.sekillEndTime = sekillEndTime;
    }

    public void setProductLinkFlag(String productLinkFlag) {
        this.productLinkFlag = productLinkFlag;
    }

    public void setClinkId(int clinkId) {
        this.clinkId = clinkId;
    }

    public void setUrlLink(String urlLink) {
        this.urlLink = urlLink;
    }

    public String getPlanGroup() {
        return planGroup;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getMessage() {
        return message;
    }

    public String getSekillStartTime() {
        return sekillStartTime;
    }

    public String getSekillEndTime() {
        return sekillEndTime;
    }

    public String getProductLinkFlag() {
        return productLinkFlag;
    }

    public int getClinkId() {
        return clinkId;
    }

    public String getUrlLink() {
        return urlLink;
    }

    public void setInfo(String info) {
        Info = info;
    }

    public String getInfo() {
        return Info;
    }

    @Override
    public String toString() {
        return "ColumnInfoVO{" +
                "planGroup='" + planGroup + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", message='" + message + '\'' +
                ", sekillStartTime='" + sekillStartTime + '\'' +
                ", sekillEndTime='" + sekillEndTime + '\'' +
                ", productLinkFlag='" + productLinkFlag + '\'' +
                ", clinkId=" + clinkId +
                ", urlLink='" + urlLink + '\'' +
                ", Info='" + Info + '\'' +
                ", seckillId='" + seckillid + '\'' +
                '}';
    }
}
