package xd.fw.action;

import com.alipay.api.internal.util.XmlUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xd.fw.AliPayUtil;
import xd.fw.WxUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xd Lv on 10/25/2016.
 */
public abstract class PayNotifyBaseAction extends BaseAction {
    Logger logger = LoggerFactory.getLogger(getClass());
    protected String out_trade_no, trade_no, trade_status, total_fee, seller_id;
    protected boolean isWx = false;

    @Action("wxNotify")
    public String wxNotify() throws Exception {
        isWx = true;
        Element rootElement = XmlUtils.getRootElementFromStream(
                ServletActionContext.getRequest().getInputStream());
        NodeList childNodes = rootElement.getChildNodes();
        Node node;
        Map<String, String> params = new HashMap<>(childNodes.getLength());
        for (int i = 0; i < childNodes.getLength(); i++) {
            node = childNodes.item(i);
            if (node.getNodeType() == Node.TEXT_NODE) {
                continue;
            }
            params.put(node.getNodeName(), XmlUtils.getElementValue(rootElement, node.getNodeName()));
        }
        String out_trade_no = params.get("out_trade_no");
        boolean verification = WxUtil.verify(params, wxKey(out_trade_no));

        if (verification) {
            boolean success = SUCCESS_FLAG.equals(params.get("return_code"));
            total_fee = params.get("total_fee");
            verification = processOrder(out_trade_no, params.get("transaction_id"), success);
        } else {
            logger.warn("wx verification failed:" + params);
        }

        return retXml("<xml>" +
                "  <return_code><![CDATA[" + (verification ? SUCCESS_FLAG : "FAIL") + "]]></return_code>" +
                "  <return_msg><![CDATA[OK]]></return_msg>" +
                "</xml>");
    }

    protected String retXml(String xml) {
        setRequestAttribute("xml", xml);
        return XML;
    }

    protected abstract String wxKey(String out_trade_no);


    protected abstract boolean processOrder(String out_trade_no, String transaction_id, boolean success);


    @Action("aliNotify")
    public String aliNotify() throws Exception {
        return processAliOrder() ? retXml("success") : retXml("fail");
    }

    private boolean processAliOrder() throws Exception {
        if ("WAIT_BUYER_PAY".equals(trade_status)){
            logger.info("wait user to pay.");
            return true;
        }
        String pid = pid(out_trade_no);
        String publicKey = aliPublicKey();
        if (AliPayUtil.verify(ServletActionContext.getRequest().getParameterMap(),publicKey, pid)) {
            boolean tradeSuccess = trade_status.equals("TRADE_SUCCESS");
            return processOrder(out_trade_no, trade_no, tradeSuccess);
        }
        logger.warn("fail to ali verify");
        return false;
    }

    protected abstract String aliPublicKey();

    protected abstract String pid(String out_trade_no);

    @Action("aliReturn")
    public String aliReturn() throws Exception {
        processAliOrder();
        return aliReturnHook(out_trade_no);
    }

    protected abstract String aliReturnHook(String out_trade_no);

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public void setTrade_no(String trade_no) {
        this.trade_no = trade_no;
    }

    public void setTrade_status(String trade_status) {
        this.trade_status = trade_status;
    }

    public void setTotal_fee(String total_fee) {
        this.total_fee = total_fee;
    }

    public void setSeller_id(String seller_id) {
        this.seller_id = seller_id;
    }

    public float totalFee(){
        if (StringUtils.isBlank(total_fee)){
            return 0f;
        }
        if (isWx){
            return Integer.parseInt(total_fee)/100.0f;
        }
        return Float.parseFloat(total_fee);
    }
}
