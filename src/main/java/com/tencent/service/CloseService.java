package com.tencent.service;

import org.slf4j.LoggerFactory;

import com.tencent.common.Configure;
import com.tencent.common.Log;
import com.tencent.common.Signature;
import com.tencent.common.Util;
import com.tencent.protocol.close_protocol.CloseReqData;
import com.tencent.protocol.close_protocol.CloseResData;

/**
 * User: zhongwm
 */
public class CloseService extends BaseService{
    private static Log log = new Log(LoggerFactory.getLogger(CloseService.class));

    public CloseService() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        super(Configure.CLOSE_API);
    }

    /**
     * 请求撤销服务
     * @param reverseReqData 这个数据对象里面包含了API要求提交的各种数据字段
     * @return API返回的XML数据
     * @throws Exception
     */
    public String request(CloseReqData closeReqData) throws Exception {

        //--------------------------------------------------------------------
        //发送HTTPS的Post请求到API地址
        //--------------------------------------------------------------------
        String responseString = sendPost(closeReqData);

        return responseString;
    }
    
    /**
     * 请求关闭定单
     * @param outTradeNo
     * @return
     * @throws Exception
     */
    public CloseResData close(String outTradeNo) throws Exception {
        CloseResData closeResData = null;
        CloseReqData reqData = new CloseReqData(outTradeNo);

        long costTimeStart = System.currentTimeMillis();

        log.i("关闭定单API返回的数据如下：");        
        String responseString = sendPost(reqData);
        System.err.println("responseString : " + responseString);
        long costTimeEnd = System.currentTimeMillis();
        long totalTimeCost = costTimeEnd - costTimeStart;
        log.i("api请求总耗时：" + totalTimeCost + "ms");
        //打印回包数据
        log.i(responseString);

        //将从API返回的XML数据映射到Java对象
        closeResData = (CloseResData) Util.getObjectFromXML(responseString, CloseResData.class);

        if (closeResData == null || closeResData.getReturn_code() == null) {
            log.e("【关闭定单失败】关闭定单请求逻辑错误，请仔细检测传过去的每一个参数是否合法，或是看API能否被正常访问");
            return closeResData;
            //return "{\"return_code\":\"FAIL\"}";
            //throw new Exception("下单失败");
        }

        if (closeResData.getReturn_code().equals("FAIL")) {
            //注意：一般这里返回FAIL是出现系统级参数错误，请检测Post给API的数据是否规范合法
            log.e("【关闭定单失败】关闭定单API系统返回失败，请检测Post给API的数据是否规范合法, err_code:" + closeResData.getErr_code() + ", err_des:" + closeResData.getErr_code_des());
            return closeResData;
            //return "{\"return_code\":\"FAIL\"}";
            //throw new Exception("下单失败");
        } else {
            log.i("关闭定单API系统成功返回数据");
            //--------------------------------------------------------------------
            //收到API的返回数据的时候得先验证一下数据有没有被第三方篡改，确保安全
            //--------------------------------------------------------------------
            if (!Signature.checkIsSignValidFromResponseString(responseString)) {
                log.e("【关闭定单失败】关闭定单请求API返回的数据签名验证失败，有可能数据被篡改了");
                return closeResData;
                //return "{\"return_code\":\"FAIL\"}";
                //throw new Exception("下单失败");
            }

            //获取错误码
            String errorCode = closeResData.getErr_code();
            //获取错误描述
            String errorCodeDes = closeResData.getErr_code_des();

            if (closeResData.getResult_code().equals("SUCCESS")) {
                //--------------------------------------------------------------------
                //1)关闭定单成功
                //--------------------------------------------------------------------
                log.i("【关闭定单成功】");
                return closeResData;
            }else{
                //出现业务错误
                log.i("业务返回失败");
                log.i("err_code:" + errorCode);
                log.i("err_code_des:" + errorCodeDes);
                log.e("【关闭定单明确失败】原因是：" + errorCodeDes);
                return closeResData;
                //return "{\"return_code\":\"FAIL\", \"err_code\":\"" + errorCode + "\"}";
                //throw new Exception("下单失败");
            }
        }
    }

}
