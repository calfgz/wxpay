package com.tencent.protocol.unified_protocol;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.tencent.common.Configure;
import com.tencent.common.RandomStringGenerator;
import com.tencent.common.Signature;

/**
 * User: rizenguo
 * Date: 2014/10/22
 * Time: 16:42
 */

/**
 * 请求统一下单提交Post数据给到API之后，API会返回XML格式的数据，这个类用来装这些数据
 */
public class UnifiedOutData {
    private String appid = "";
    private String partnerid = "";
    private String prepayid = "";
    private String noncestr = "";
    private String sign = "";
    private String timestamp = "";
    //private String package = "";
    
    public UnifiedOutData(UnifiedResData payResData) {
        setAppid(Configure.getAppid());
        setPartnerid(Configure.getMchid());
        setPrepayid(payResData.getPrepay_id());
        setNoncestr(RandomStringGenerator.getRandomStringByLength(32));
        setTimestamp(String.valueOf(System.currentTimeMillis()/1000));
        String sign = Signature.getSign(toMap());
        setSign(sign);
    }

    /**
     * @return the partnerid
     */
    public String getPartnerid() {
        return partnerid;
    }

    /**
     * @param partnerid the partnerid to set
     */
    public void setPartnerid(String partnerid) {
        this.partnerid = partnerid;
    }

    /**
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    /**
     * @return the noncestr
     */
    public String getNoncestr() {
        return noncestr;
    }

    /**
     * @param noncestr the noncestr to set
     */
    public void setNoncestr(String noncestr) {
        this.noncestr = noncestr;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
    
    /**
     * @return the prepayid
     */
    public String getPrepayid() {
        return prepayid;
    }

    /**
     * @param prepayid the prepayid to set
     */
    public void setPrepayid(String prepayid) {
        this.prepayid = prepayid;
    }

    public String toJson() {
        StringBuffer buf = new StringBuffer("{");
        buf.append("\"return_code\":\"SUCCESS\"");
        buf.append(",").append("\"appid\":\"" + appid +"\"");
        buf.append(",").append("\"partnerid\":\"" + partnerid + "\"");
        buf.append(",").append("\"prepayid\":\"" + prepayid + "\"");
        buf.append(",").append("\"package\":\"Sign=WXPay\"");
        buf.append(",").append("\"noncestr\":\"" + noncestr + "\"");
        buf.append(",").append("\"timestamp\":\"" + timestamp +"\"");
        //String sign = Signature.getSign(toMap());
        buf.append(",").append("\"sign\":\"" + sign +"\"");
        buf.append("}");
        return buf.toString();
    }

    public Map<String,Object> toMap(){
        Map<String,Object> map = new HashMap<String, Object>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            Object obj;
            try {
                obj = field.get(this);
                if(obj!=null){
                    map.put(field.getName(), obj);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        map.put("package", "Sign=WXPay");
        return map;
    }

}
