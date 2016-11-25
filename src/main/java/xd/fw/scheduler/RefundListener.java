package xd.fw.scheduler;

import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.XmlUtils;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import org.apache.http.Consts;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import xd.fw.WxUtil;
import xd.fw.service.IConst;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
//@Async
public abstract class RefundListener implements ApplicationListener<RefundEvent>, IConst {
    Logger logger = LoggerFactory.getLogger(RefundListener.class);

    @Autowired
    WxCerts wxCerts;

    @Override
    public void onApplicationEvent(RefundEvent event){
        logger.info("start to refund {} in {}", event.getOutTradeNo(), event.getId());
        boolean success = false;

        if (event.getPayType() == PAY_WX) {
            String fee = String.valueOf((int)(event.getTotalFee() * 100));
            List<String> paramList = new ArrayList<>();
            StringBuffer xml = new StringBuffer("<xml>");

            construct(paramList, xml, "appid", event.getAppId());
            construct(paramList, xml, "mch_id", event.getMchId());
            construct(paramList, xml, "nonce_str", WxUtil.getRandomStringByLength(32));
            construct(paramList, xml, "op_user_id", event.getMchId());
            construct(paramList, xml, "out_refund_no", "8" + event.getOutTradeNo());
            construct(paramList, xml, "out_trade_no", event.getOutTradeNo());
            construct(paramList, xml, "refund_fee", fee);
            construct(paramList, xml, "total_fee", fee);

            String sign = WxUtil.getSign(paramList, event.getWxKey());
            construct(paramList, xml, "sign", sign);
            xml.append("</xml>");

            try {
                logger.info("refund request xml:" + xml);
                String retXml = wxHttp(event.getId(), xml.toString());
                logger.info("refund response xml:" + retXml);
                Element rootEle = XmlUtils.getRootElementFromString(retXml);
                String returnCode = XmlUtils.getElementValue(rootEle, "return_code");
                String resultCode = XmlUtils.getElementValue(rootEle, "result_code");
                success = SUCCESS_FLAG.equals(returnCode) && SUCCESS_FLAG.equals(resultCode);
            } catch (Exception e) {
                logger.error("",e);
            }
        } else {
            //ali refund
            try {
                AlipayClient alipayClient = alipayClient(event);
                AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
                request.setBizContent(String.format("{" +
                        "\"out_trade_no\":\"%s\"," +
                        "\"refund_amount\":%.2f" +
                        "}", event.getOutTradeNo(), event.getTotalFee()));
                AlipayTradeRefundResponse response = alipayClient.execute(request);
                success = response.isSuccess();
            } catch (Exception e) {
                logger.error("",e);
            }
        }
        logger.info("refund {} for {}", success , event.getOutTradeNo());

        processRefundStatus(event.getOutTradeNo(), success);
    }

    protected abstract void processRefundStatus(String outTradeNo, boolean success);

    void construct(List<String> paramList, StringBuffer xml, String key, String value) {
        paramList.add(key + "=" + value + "&");
        xml.append("<").append(key).append(">").append(value).append("</").append(key).append(">");
    }

    String wxHttp(String id, String xml) throws IOException {
        try(CloseableHttpClient httpClient = wxCerts.getClientById(id)){
            HttpPost post = new HttpPost("https://api.mch.weixin.qq.com/secapi/pay/refund");

            StringEntity jsonEntity = new StringEntity(xml, Consts.UTF_8);
            jsonEntity.setContentType("text/xml");
            post.setEntity(jsonEntity);
            return EntityUtils.toString(httpClient.execute(post).getEntity(), Consts.UTF_8);
        }
    }
    protected AlipayClient alipayClient(RefundEvent event){
        return new DefaultAlipayClient("https://openapi.alipay.com/gateway.do"
                , event.getAppId(), event.getRsaKey(), "json", AlipayConstants.CHARSET_UTF8, null);

    }
}
