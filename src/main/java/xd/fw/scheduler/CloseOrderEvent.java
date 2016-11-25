package xd.fw.scheduler;

import org.springframework.context.ApplicationEvent;
import xd.fw.service.IConst;

public class CloseOrderEvent extends ApplicationEvent implements IConst {

    byte payType;

    String outTradeNo;

    String appId;
    String mchId;
    String wxKey;

    String rsaKey;

    public static CloseOrderEvent wxClose(String outTradeNo, String appId, String mchId, String wxKey) {
        CloseOrderEvent event = new CloseOrderEvent(outTradeNo);
        event.payType = PAY_WX;
        event.outTradeNo = outTradeNo;
        event.appId = appId;
        event.mchId = mchId;
        event.wxKey = wxKey;
        return event;
    }

    public static CloseOrderEvent aliClose(String outTradeNo,String appId, String rsaKey) {
        CloseOrderEvent event = new CloseOrderEvent(outTradeNo);
        event.payType = PAY_ALI;
        event.outTradeNo = outTradeNo;
        event.appId = appId;
        event.rsaKey = rsaKey;
        return event;
    }

    public CloseOrderEvent(String outTradeNo) {
        super(outTradeNo);
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public String getAppId() {
        return appId;
    }

    public String getMchId() {
        return mchId;
    }

    public String getRsaKey() {
        return rsaKey;
    }

    public byte getPayType() {
        return payType;
    }

    public String getWxKey() {
        return wxKey;
    }
}
