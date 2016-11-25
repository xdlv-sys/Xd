package xd.fw;

import com.alipay.api.AlipayConstants;
import com.alipay.api.internal.util.AlipaySignature;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by exiglvv on 7/5/2016.
 */
public class AliPayUtil {

    static Logger logger = LoggerFactory.getLogger(AliPayUtil.class);
    static final String HTTPS_VERIFY_URL = "https://mapi.alipay.com/gateway.do?service=notify_verify";
    public static String aliPayPublicKey  = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";


    public static boolean verify(Map requestParams, String publicKey, String partner) throws Exception {
        Map<String,String> params = new HashMap<>();
        Iterator iterator = requestParams.keySet().iterator();
        StringBuilder value = new StringBuilder();
        String tmpValue;
        while (iterator.hasNext()){
            String name = (String) iterator.next();
            String[] values = (String[]) requestParams.get(name);
            for (int i = 0; i < values.length; i++) {
                value.append(values[i]);
                if (i < values.length -1){
                    value.append(",");
                }
            }
            tmpValue = value.toString();
            //tmpValue = new String(tmpValue.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, tmpValue);
            value.setLength(0);
        }

        String notifyId = params.get("notify_id");
        if (StringUtils.isBlank(notifyId)){
            logger.warn("failed to verify : no notify_id");
            return false;
        }
        String verifyUrl = HTTPS_VERIFY_URL + "&partner=" + partner + "&notify_id=" + notifyId;
        String response = HttpClientTpl.get(verifyUrl);
        if (!"true".equals(response)){
            logger.warn("failed to verify : notify id invalidate");
            return false;
        }
        return AlipaySignature.rsaCheckV1(params,publicKey,AlipayConstants.CHARSET_UTF8);
    }

    public static String getSign(Object obj, String privateKey) throws Exception {
        Map<String, String> sortedParams = new TreeMap<String, String>();
        WxUtil.invokeBeanFields(obj, (f,v)->{
            if (f.getName().equals("sign")
                    || f.getName().equals("sign_type")
                    || v == null || StringUtils.isBlank(v.toString())){
                return;
            }
            sortedParams.put(f.getName(),String.valueOf(v));
        });
        String content = AlipaySignature.getSignContent(sortedParams);
        logger.debug(content);
        return AlipaySignature.rsaSign(content, privateKey, AlipayConstants.CHARSET_UTF8);
    }
}
