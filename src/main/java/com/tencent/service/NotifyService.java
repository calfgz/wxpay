/**
 * Copyright (c) 2015 pconline.com.cn . All rights reserved.
 *
 * Description: wxpay-scanpay-java-sdk
 * 
 * Modified log:
 * ------------------------------------------------------
 * Ver.		Date		Author			Description
 * ------------------------------------------------------
 * 1.0		2015-4-27	pconline		created.
 */
package com.tencent.service;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.tencent.common.Log;
import com.tencent.common.Signature;
import com.tencent.common.Util;
import com.tencent.protocol.notify_protocol.NotifyResData;

/**
 * @author zhongwm
 *
 */
public class NotifyService {

    //打log用
    private static Log log = new Log(LoggerFactory.getLogger(NotifyService.class));
    
    /**
     * 支付宝回调处理
     * @param request
     * @param response
     * @return 返回回调信息
     * @throws SAXException 
     * @throws IOException 
     * @throws ParserConfigurationException 
     */
    public NotifyResData donotify(HttpServletRequest request, HttpServletResponse response) throws ParserConfigurationException, IOException, SAXException {
        NotifyResData notifyResData = null;
        log.i("微信支付回调数据开始.");
        String inputLine;
        String notityXml = "";
        
        try {
            while ((inputLine = request.getReader().readLine()) != null) {
                notityXml += inputLine;
            }
            request.getReader().close();
        } catch (Exception e) {
            e.printStackTrace();
        }        
        log.i("接收到的报文：" + notityXml);

        //将从API返回的XML数据映射到Java对象
        notifyResData = (NotifyResData) Util.getObjectFromXML(notityXml, NotifyResData.class);

        if (notifyResData == null || notifyResData.getReturn_code() == null) {
            log.e("【回调错误】支付宝回调参数错误，xml:" + notityXml);
            notifyResData.setCode("FAIL");
            notifyResData.setMsg("参数错误");
            return notifyResData;
        }
        
        if (notifyResData.getReturn_code().equals("FAIL")) {
            //注意：一般这里返回FAIL是出现系统级参数错误，请检测Post给API的数据是否规范合法
            log.e("【回调错误】支付宝回调返回失败, msg:" + notifyResData.getReturn_msg() + ", xml:" + notityXml);
            notifyResData.setCode("FAIL");
            notifyResData.setMsg("回调失败");
            return notifyResData;
        } else {
            log.i("支付宝回调系统成功返回数据");
            //--------------------------------------------------------------------
            //收到API的返回数据的时候得先验证一下数据有没有被第三方篡改，确保安全
            //--------------------------------------------------------------------
            if (!Signature.checkIsSignValidFromResponseString(notityXml)) {
                log.e("【回调错误】支付宝回调返回的数据签名验证失败，有可能数据被篡改了， xml:" + notityXml);
                notifyResData.setCode("FAIL");
                notifyResData.setMsg("签名失败");
                return notifyResData;
            } else {
                notifyResData.setCode("SUCCESS");
            }
        }
        return notifyResData;
    }
    
    /**
     * 支付宝回调处理
     * @param notityXml 回调参数
     * @return 返回对象
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public NotifyResData donotify(String notityXml) throws ParserConfigurationException, IOException, SAXException {
        NotifyResData notifyResData = null;
        log.i("微信支付回调数据开始.");

        //将从API返回的XML数据映射到Java对象
        notifyResData = (NotifyResData) Util.getObjectFromXML(notityXml, NotifyResData.class);

        if (notifyResData == null || notifyResData.getReturn_code() == null) {
            log.e("【回调错误】支付宝回调参数错误，xml:" + notityXml);
            notifyResData.setCode("FAIL");
            notifyResData.setMsg("参数错误");
            return notifyResData;
        }
        
        if (notifyResData.getReturn_code().equals("FAIL")) {
            //注意：一般这里返回FAIL是出现系统级参数错误，请检测Post给API的数据是否规范合法
            log.e("【回调错误】支付宝回调返回失败, msg:" + notifyResData.getReturn_msg() + ", xml:" + notityXml);
            notifyResData.setCode("FAIL");
            notifyResData.setMsg("回调失败");
            return notifyResData;
        } else {
            log.i("支付宝回调系统成功返回数据");
            //--------------------------------------------------------------------
            //收到API的返回数据的时候得先验证一下数据有没有被第三方篡改，确保安全
            //--------------------------------------------------------------------
            if (!Signature.checkIsSignValidFromResponseString(notityXml)) {
                log.e("【回调错误】支付宝回调返回的数据签名验证失败，有可能数据被篡改了， xml:" + notityXml);
                notifyResData.setCode("FAIL");
                notifyResData.setMsg("签名失败");
                return notifyResData;
            } else {
                notifyResData.setCode("SUCCESS");
                notifyResData.setMsg("OK");
            }
        }
        return notifyResData;
    }

}
