package com.tencent.service;

import org.slf4j.LoggerFactory;

import com.tencent.common.Configure;
import com.tencent.common.Log;
import com.tencent.common.Signature;
import com.tencent.common.Util;
import com.tencent.protocol.pay_query_protocol.ScanPayQueryReqData;
import com.tencent.protocol.pay_query_protocol.ScanPayQueryResData;

/**
 * User: rizenguo
 * Date: 2014/10/29
 * Time: 16:04
 */
public class ScanPayQueryService extends BaseService{
    //打log用
    private static Log log = new Log(LoggerFactory.getLogger(ScanPayQueryService.class));

    public ScanPayQueryService() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        super(Configure.PAY_QUERY_API);
    }

    /**
     * 请求支付查询服务
     * @param scanPayQueryReqData 这个数据对象里面包含了API要求提交的各种数据字段
     * @return API返回的XML数据
     * @throws Exception
     */
    public String request(ScanPayQueryReqData scanPayQueryReqData) throws Exception {

        //--------------------------------------------------------------------
        //发送HTTPS的Post请求到API地址
        //--------------------------------------------------------------------
        String responseString = sendPost(scanPayQueryReqData);

        return responseString;
    }
    
    /**
     * 请求支付查询，返回Notify对象
     * @param transactionId 微信定单id
     * @param outTradeNo 应用定单号
     * @return
     * @throws Exception
     */
    public ScanPayQueryResData requestQuery(String transactionId, String outTradeNo) throws Exception {
        ScanPayQueryResData queryResData = null;
        ScanPayQueryReqData scanPayQueryReqData = new ScanPayQueryReqData(transactionId, outTradeNo);
        //--------------------------------------------------------------------
        //发送HTTPS的Post请求到API地址
        //--------------------------------------------------------------------
        String responseString = null; 

        long costTimeStart = System.currentTimeMillis();

        log.i("查询API返回的数据如下：");
        responseString = new ScanPayQueryService().request(scanPayQueryReqData);
        long costTimeEnd = System.currentTimeMillis();
        long totalTimeCost = costTimeEnd - costTimeStart;
        log.i("api请求总耗时：" + totalTimeCost + "ms");
        //打印回包数据
        log.i(responseString);

        log.i("微信支付查询数据开始.");

        //将从API返回的XML数据映射到Java对象
        queryResData = (ScanPayQueryResData) Util.getObjectFromXML(responseString, ScanPayQueryResData.class);

        if (queryResData == null || queryResData.getReturn_code() == null) {
            log.e("【查询错误】支付宝查询参数错误，xml:" + responseString);
            return queryResData;
        }
        
        if (queryResData.getReturn_code().equals("FAIL")) {
            //注意：一般这里返回FAIL是出现系统级参数错误，请检测Post给API的数据是否规范合法
            log.e("【查询错误】支付宝查询返回失败, msg:" + queryResData.getReturn_msg() + ", xml:" + responseString);
            return queryResData;
        } else {
            log.i("支付宝查询系统成功返回数据");
            //--------------------------------------------------------------------
            //收到API的返回数据的时候得先验证一下数据有没有被第三方篡改，确保安全
            //--------------------------------------------------------------------
            if (!Signature.checkIsSignValidFromResponseString(responseString)) {
                log.e("【查询错误】支付宝查询返回的数据签名验证失败，有可能数据被篡改了， xml:" + responseString);
                return queryResData;
            }
        }
        return queryResData;
    }


}
