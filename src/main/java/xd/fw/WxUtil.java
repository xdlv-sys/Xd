package xd.fw;

import com.alipay.api.internal.util.XmlUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * Created by xd on 2016/6/29.
 */
public class WxUtil {
    final static String data = "abcdefghijklmnopqrstuvwxyz0123456789";

    public static String getRandomStringByLength(int length) {
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(data.length());
            sb.append(data.charAt(number));
        }
        return sb.toString();
    }

    public static String getSign(Object o, String key) throws IllegalAccessException {
        List<String> list = new ArrayList<String>();
        invokeBeanFields(o,(f,v)->{
            if (v != null && StringUtils.isNotBlank(v.toString())) {
                list.add(f.getName() + "=" + v + "&");
            }
        });
        return getSign(list, key);
    }

    public static boolean verify(Element rootElement, String key){
        NodeList childNodes = rootElement.getChildNodes();
        Node node;
        Map<String, String> params = new HashMap<>(childNodes.getLength());
        for (int i =0 ;i < childNodes.getLength();i++){
            node = childNodes.item(i);
            if (node.getNodeType() == Node.TEXT_NODE){
                continue;
            }
            params.put(node.getNodeName(), XmlUtils.getElementValue(rootElement, node.getNodeName()));
        }
        return verify(params,key);
    }

    public static boolean verify(Map<String, String> params, String key){
        List<String> list = new ArrayList<String>();
        String sign = null;
        for (Map.Entry<String,String> entry : params.entrySet()){
            if (entry.getKey().equals("sign")){
                sign = entry.getValue();
                continue;
            }
            if (StringUtils.isBlank(entry.getValue())){
                continue;
            }
            list.add(entry.getKey() + "=" + entry.getValue()  + "&");
        }
        return getSign(list, key).equals(sign);
    }

    public static String getSign(List<String> params, String key){
        int size = params.size();
        String[] arrayToSort = params.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(arrayToSort[i]);
        }
        sb.append("key=").append(key);
        return MD5.MD5Encode(sb.toString()).toUpperCase();
    }

    public static String constructUnifiedOrderXml(Object object) throws IllegalAccessException {
        StringBuffer buffer = new StringBuffer(256);
        buffer.append("<xml>");

        invokeBeanFields(object,(f,v)->{
            if (v != null && StringUtils.isNotBlank(v.toString())) {
                buffer.append("<").append(f.getName()).append(">"
                ).append(v).append("</").append(f.getName()).append(">");
            }
        });

        buffer.append("</xml>");
        return buffer.toString();
    }

    public static void invokeBeanFields(Object o, FwUtil.BeanFieldProcess p){
        try {
            FwUtil.invokeBeanFields(o,p);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
