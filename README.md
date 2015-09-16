# 微信支付客户端

## 介绍 

 **微信支付客户端**是基于微信官方提供的刷卡支付SDK基础上二次开发出来的一个用于集成调用微信支付API的客户端工具。 目的是为了提高我们对微信支付的开发效率。集成了包括服务端对微信支付下单、回调、查询、退款等所有API接口的调用，支持包括刷卡、公众号、扫码及APP四种支付方式。支付增加代理设置及修复有些情况UTF-8编码报验证失败的bug。

## 变更记录 

### Ver 1.0 (2015-6-2)
 * 增加代理设置；
 * 修复必须要提交微信支付证书验证的bug；
 * 增加公众号、扫码、及APP支付方式的API调用；
 * 修复有些情况UTF-8编码报验证失败的bug；


## 客户端结构 

![](http://i.imgur.com/ItGmuuS.png)

## 快速入门 

### 第一步：引入微信支付客户端及其依赖包 

在maven配置文件中加入以下代码

 ```
   <dependency>
       <groupId>com.tencent</groupId>
       <artifactId>wxpay</artifactId>
       <version>1.0</version>
   </dependency>
   <dependency>
       <groupId>commons-logging</groupId>
       <artifactId>commons-logging</artifactId>
       <version>1.1.3</version>
   </dependency>
   <dependency>
       <groupId>org.apache.httpcomponents</groupId>
       <artifactId>fluent-hc</artifactId>
       <version>4.3.5</version>
   </dependency>
   <dependency>
       <groupId>org.apache.httpcomponents</groupId>
       <artifactId>httpclient</artifactId>
       <version>4.3.5</version>
   </dependency>
   <dependency>
       <groupId>org.apache.httpcomponents</groupId>
       <artifactId>httpclient-cache</artifactId>
       <version>4.3.5</version>
   </dependency>
   <dependency>
       <groupId>org.apache.httpcomponents</groupId>
       <artifactId>httpcore</artifactId>
       <version>4.3.2</version>
   </dependency>
   <dependency>
       <groupId>org.apache.httpcomponents</groupId>
       <artifactId>httpmime</artifactId>
       <version>4.3.5</version>
   </dependency>
   <dependency>
       <groupId>org.slf4j</groupId>
       <artifactId>slf4j-simple</artifactId>
       <version>1.7.7</version>
       <scope>provided</scope>
   </dependency>
   <dependency>
       <groupId>xmlpull</groupId>
       <artifactId>xmlpull</artifactId>
       <version>1.1.3.1</version>
   </dependency>
   <dependency>
       <groupId>xpp3</groupId>
       <artifactId>xpp3</artifactId>
       <version>1.1.4c</version>
   </dependency>``
   <dependency>
       <groupId>com.thoughtworks.xstream</groupId>
       <artifactId>xstream</artifactId>
       <version>1.4.7</version>
   </dependency>
```

### 第二步：调用 

统一下单示例

![](http://i.imgur.com/KKrUEFZ.png)

## SDK层级详解：

### 1）通用层

![img](https://raw.githubusercontent.com/grz/wxpay_scanpay_java_sdk_proj/master/docs/asset/common_layer.png "common_layer") 
这里封装了很多非常基础的组件，供上层服务调用，其中包括以下组件：  

1.  基础配置组件（Configure）
该组件用来从wxpay.properties里面获取到跟商户相关的几个关键配置信息。
2.  HTTPS请求器（HttpsRequest）
发HTTPS请求的底层封装。
3.  随机数生成器（RandomStringGenerator）
用来生成指定长度的随机数。
4.  MD5加密算法（MD5）
5.  XMLParser（XML解析器）
由于API返回的数据是XML格式，所以SDK这里也提供了对返回的XML数据进行解析成Java对象的能力，方便大家可以快速处理API返回的数据。
6.  签名（Signature）
为了防止数据在传输过程中被篡改，所以这里要对字段做签名运算。
7.  基础工具（Util）
开发过程中用到的一些基础工具类函数。
8.  日志上报
这里会将SDK里面的模块调用情况，程序执行流程给打好详细日志，Log系统用的是SLF4J这套通用解决方案，方便对接商户系统自己的日志逻辑。
9.  性能上报
这里用的是微信支付统一的API性能上报接口进行上报，可以实现将每次调用支付API的耗时、返回码等相关数据进行上报。
（以上讲了这么多，只要使用了这个SDK，这些东西都帮大家解决掉了^_^，这就是我们为啥需要有一个SDK~）

### 2）协议层

![img](https://raw.githubusercontent.com/grz/wxpay_scanpay_java_sdk_proj/master/docs/asset/protocol_layer.png "protocol_layer")
这里跟API文档定义的字段进行一一对应，协议层这里分为两部分：  

*   第一部分是“请求数据”，这里定义了每一个API请求时需要传过去的具体数据字段；  
*   第二部分是“返回数据”，这里定义了每一个API返回时会传的具体数据字段；

以上协议在“服务层”提供的各种服务里面已经帮忙封装好，直接使用服务即可。

### 3）服务层

![img](https://raw.githubusercontent.com/grz/wxpay_scanpay_java_sdk_proj/master/docs/asset/service_layer.png "service_layer")
这里已经根据API文档封装好具体服务，供开发者直接调用。  
例如，以下代码直接调用PayService.request提交支付请求，商户只需要从自己的系统生成该服务提交协议里面要求的数据项即可：  

```java
payServiceResponseString = PayService.request(
               authCode,//auth_code:这个是扫码终端设备从用户手机上扫取到的支付授权号，这个号是跟用户用来支付的银行卡绑定的，有效期是1分钟
               body,//body:要支付的商品的描述信息，用户会在支付成功页面里看到这个信息
               attach, //attach:支付订单里面可以填的附加数据，API会将提交的这个附加数据原样返回，有助于商户自己可以注明该笔消费的具体内容，方便后续的运营和记录
               outTradeNo,//out_trade_no:商户系统内部的订单号,32个字符内可包含字母, [确保在商户系统唯一]
               totalFee,//total_fee:订单总金额，单位为“分”，只能整数
               deviceInfo,//device_info:商户自己定义的扫码支付终端设备号，方便追溯这笔交易发生在哪台终端设备上
               spBillCreateIP,//spBillCreateIP:订单生成的机器IP
               timeStart,//time_start:订单生成时间
               timeEnd,//time_end:订单失效时间
               goodsTag//goods_tag:商品标记，微信平台配置的商品标记，用于优惠券或者满减使用
       );
```

### 4）业务层
![img](https://raw.githubusercontent.com/grz/wxpay_scanpay_java_sdk_proj/master/docs/asset/business_layer.png "business_layer")  
业务层是比服务更加高级的封装。业务层通过服务层向API提交请求，拿到API的返回数据之后会对返回数据做一些数据解析、签名校验、出错判断等操作。  
对于像“被扫支付”这种比较复杂和常用的业务，这里特别封装了官方建议的最佳实践流程。里面涵盖了“支付”、“支付查询”、“撤销”等几个服务和建议的流程、轮询次数、轮询间隔等。商户开发可以直接使用，也可以通过修改里面的配置来自定义自己的流程。

