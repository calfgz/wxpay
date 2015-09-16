package com.tencent.service;

import org.slf4j.LoggerFactory;

import com.tencent.common.Configure;
import com.tencent.common.Log;
import com.tencent.common.Signature;
import com.tencent.common.Util;
import com.tencent.protocol.unified_protocol.UnifiedOutData;
import com.tencent.protocol.unified_protocol.UnifiedReqData;
import com.tencent.protocol.unified_protocol.UnifiedResData;

/**
 * User: zhongwm
 * Date: 2015/4/24
 * Time: 1:03
 */
public class UnifiedService extends BaseService{
    //打log用
    private static Log log = new Log(LoggerFactory.getLogger(UnifiedService.class));

    public UnifiedService() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        super(Configure.UNIFIED_API);
    }

    /**
     * 请求请求统一下单支付服务
     * @param unifiedReqData 这个数据对象里面包含了API要求提交的各种数据字段
     * @return API返回的数据
     * @throws Exception
     */
    public String request(UnifiedReqData unifiedReqData) throws Exception {

        //--------------------------------------------------------------------
        //发送HTTPS的Post请求到API地址
        //--------------------------------------------------------------------
        String responseString = sendPost(unifiedReqData);

        return responseString;
    }
    
    /**
     * APP统一下单方法
     * @param body
     * @param outTradeNo
     * @param totalFee
     * @param spBillCreateIP
     * @param timeStart
     * @param timeExpire
     * @param notifyUrl
     * @param productId
     * @return 
     * @throws Exception
     */
    public UnifiedOutData appUnified(String body,String outTradeNo,int totalFee,String spBillCreateIP,
            String timeStart,String timeExpire,String notifyUrl, String productId) throws Exception {
        UnifiedOutData unifiedOutData = null;
        
        UnifiedReqData unifiedReqData = new UnifiedReqData(body, "", "", outTradeNo, totalFee, "",
                spBillCreateIP, timeStart, timeExpire, "", notifyUrl, "APP", productId, ""); 

        //接受API返回
        String unifiedResponseString;

        long costTimeStart = System.currentTimeMillis();

        log.i("下单API返回的数据如下：");
        unifiedResponseString = request(unifiedReqData);
        System.err.println("unifiedResponseString : " + unifiedResponseString);
        long costTimeEnd = System.currentTimeMillis();
        long totalTimeCost = costTimeEnd - costTimeStart;
        log.i("api请求总耗时：" + totalTimeCost + "ms");
        //打印回包数据
        log.i(unifiedResponseString);

        //将从API返回的XML数据映射到Java对象
        UnifiedResData unifiedResData = (UnifiedResData) Util.getObjectFromXML(unifiedResponseString, UnifiedResData.class);

        if (unifiedResData == null || unifiedResData.getReturn_code() == null) {
            log.e("【下单失败】下单请求逻辑错误，请仔细检测传过去的每一个参数是否合法，或是看API能否被正常访问");
            return unifiedOutData;
            //return "{\"return_code\":\"FAIL\"}";
            //throw new Exception("下单失败");
        }

        if (unifiedResData.getReturn_code().equals("FAIL")) {
            //注意：一般这里返回FAIL是出现系统级参数错误，请检测Post给API的数据是否规范合法
            log.e("【下单失败】下单API系统返回失败，请检测Post给API的数据是否规范合法, err_code:" + unifiedResData.getErr_code() + ", err_des:" + unifiedResData.getErr_code_des());
            return unifiedOutData;
            //return "{\"return_code\":\"FAIL\"}";
            //throw new Exception("下单失败");
        } else {
            log.i("下单API系统成功返回数据");
            //--------------------------------------------------------------------
            //收到API的返回数据的时候得先验证一下数据有没有被第三方篡改，确保安全
            //--------------------------------------------------------------------
            if (!Signature.checkIsSignValidFromResponseString(unifiedResponseString)) {
                log.e("【下单失败】下单请求API返回的数据签名验证失败，有可能数据被篡改了");
                return unifiedOutData;
                //return "{\"return_code\":\"FAIL\"}";
                //throw new Exception("下单失败");
            }

            //获取错误码
            String errorCode = unifiedResData.getErr_code();
            //获取错误描述
            String errorCodeDes = unifiedResData.getErr_code_des();

            if (unifiedResData.getResult_code().equals("SUCCESS")) {
                //--------------------------------------------------------------------
                //1)直接下单成功
                //--------------------------------------------------------------------
                log.i("【一次性下单成功】");
                
                unifiedOutData = new UnifiedOutData(unifiedResData);
                log.i("out_json:" + unifiedOutData.toJson());
                return unifiedOutData;
                //return unifiedOutData.toJson();
            }else{
                //出现业务错误
                log.i("业务返回失败");
                log.i("err_code:" + errorCode);
                log.i("err_code_des:" + errorCodeDes);
                log.w("【下单明确失败】原因是：" + errorCodeDes);
                return unifiedOutData;
                //return "{\"return_code\":\"FAIL\", \"err_code\":\"" + errorCode + "\"}";
                //throw new Exception("下单失败");
            }
        }
    }
}
