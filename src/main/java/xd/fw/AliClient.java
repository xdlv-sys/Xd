package xd.fw;

import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public abstract class AliClient {
    AlipayClient alipayClient;
    @Value("${ali_app_id}")
    String appId;

    @Value("${rsa_key}")
    String rsaKey;
    @Value("${ali_public_key}")
    String aliPublicKey;

    @PostConstruct
    public void init() {
        alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do"
                , getAppId(), getRsaKey(), "json", AlipayConstants.CHARSET_UTF8, aliPublicKey);
    }

    public String getAppId() {
        return appId;
    }

    public String getRsaKey() {
        return rsaKey;
    }

    public String getAliPublicKey(){
        return aliPublicKey;
    }

    public AlipayClient getAlipayClient() {
        return alipayClient;
    }

}
