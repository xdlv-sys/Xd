package xd.fw.scheduler;

import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.XmlUtils;
import com.alipay.api.request.AlipayTradeCancelRequest;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.response.AlipayTradeCancelResponse;
import com.alipay.api.response.AlipayTradeCloseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import xd.fw.HttpClientTpl;
import xd.fw.WxUtil;
import xd.fw.service.IConst;

import java.util.ArrayList;
import java.util.List;

@Service
//@Async
public abstract class CloseOrderListener implements ApplicationListener<CloseOrderEvent>, IConst {
    Logger logger = LoggerFactory.getLogger(CloseOrderListener.class);

    @Override
    public void onApplicationEvent(CloseOrderEvent event) {
        logger.info("start to close {} ", event.getOutTradeNo());
        boolean success = false;
        if (event.getPayType() == PAY_WX) {
            List<String> paramList = new ArrayList<>();
            StringBuffer xml = new StringBuffer("<xml>");

            construct(paramList, xml, "appid", event.getAppId());
            construct(paramList, xml, "mch_id", event.getMchId());
            construct(paramList, xml, "nonce_str", WxUtil.getRandomStringByLength(32));
            construct(paramList, xml, "out_trade_no", event.getOutTradeNo());

            String sign = WxUtil.getSign(paramList, event.getWxKey());
            construct(paramList, xml, "sign", sign);
            xml.append("</xml>");

            try {
                logger.info("close request xml:" + xml);
                String retXml = wxHttp(xml.toString());
                logger.info("close response xml:" + retXml);
                Element rootEle = XmlUtils.getRootElementFromString(retXml);
                String returnCode = XmlUtils.getElementValue(rootEle, "return_code");
                String resultCode = XmlUtils.getElementValue(rootEle, "result_code");
                success = SUCCESS_FLAG.equals(returnCode) && SUCCESS_FLAG.equals(resultCode);
            } catch (Exception e) {
                logger.error("", e);
            }
        } else {
            //ali refund
            try {
                AlipayClient alipayClient = alipayClient(event);
                AlipayTradeCancelRequest request = new AlipayTradeCancelRequest();

                request.setBizContent(String.format("{" +
                        "    \"out_trade_no\":\"%s\"" +
                        "  }", event.getOutTradeNo()));
                AlipayTradeCancelResponse response = alipayClient.execute(request);
                success = response.isSuccess();
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        logger.info("close {} for {}", success, event.getOutTradeNo());

        processCloseStatus(event.getOutTradeNo(), success);
    }

    protected abstract void processCloseStatus(String outTradeNo, boolean success);

    void construct(List<String> paramList, StringBuffer xml, String key, String value) {
        paramList.add(key + "=" + value + "&");
        xml.append("<").append(key).append(">").append(value).append("</").append(key).append(">");
    }

    String wxHttp(String xml) throws Exception {
        return HttpClientTpl.postXml("https://api.mch.weixin.qq.com/pay/closeorder", xml);
    }

    protected AlipayClient alipayClient(CloseOrderEvent event) {
        return new DefaultAlipayClient("https://openapi.alipay.com/gateway.do"
                , event.getAppId(), event.getRsaKey(), "json", AlipayConstants.CHARSET_UTF8, null);

    }
}
