package xd.fw.action;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;

public class WeixinAction extends BaseAction {

	private String echostr;
	private String nonce;
	private String timestamp;
    private String signature;

	public String entry() {
		if (StringUtils.isNotBlank(echostr)) {
			returnXml(echostr);
			return XML;
		}
		return XML;
	}

	private void returnXml(String xml) {
		ServletActionContext.getRequest().setAttribute("xml", xml);
	}

	public void setEchostr(String echostr) {
		this.echostr = echostr;
	}

	public String getEchostr() {
		return echostr;
	}

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}