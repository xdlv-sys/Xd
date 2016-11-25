package xd.fw.scheduler;

import org.springframework.context.ApplicationEvent;
import xd.fw.service.IConst;

public class RefundEvent extends ApplicationEvent implements IConst {

    String id = "001";

    byte payType;

    String outTradeNo;
    float totalFee;

    String appId;
    String mchId;
    String wxKey;

    String rsaKey;

    public static RefundEvent wxRefund(String outTradeNo, float totalFee
            , String appId, String mchId, String wxKey) {
        RefundEvent event = new RefundEvent(outTradeNo);
        event.payType = PAY_WX;
        event.outTradeNo = outTradeNo;
        event.totalFee = totalFee;
        event.appId = appId;
        event.mchId = mchId;
        event.wxKey = wxKey;
        return event;
    }

    public static RefundEvent aliRefund(String outTradeNo, float totalFee
            , String appId, String rsaKey) {
        RefundEvent event = new RefundEvent(outTradeNo);
        event.payType = PAY_ALI;
        event.outTradeNo = outTradeNo;
        event.totalFee = totalFee;
        event.appId = appId;
        event.rsaKey = rsaKey;
        return event;
    }

    public RefundEvent(String outTradeNo) {
        super(outTradeNo);
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public float getTotalFee() {
        return totalFee;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte getPayType() {
        return payType;
    }

    public String getWxKey() {
        return wxKey;
    }
}
