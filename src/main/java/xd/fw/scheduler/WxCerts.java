package xd.fw.scheduler;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xd.fw.I18n;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

@Service
public class WxCerts {
    Map<String, SSLConnectionSocketFactory> sslMap = new HashMap<>();

    Logger logger = LoggerFactory.getLogger(WxCerts.class);

    @PostConstruct
    public void loadCert() throws Exception {
        File[] files = new File(I18n.getWebInfDir(), "cert").listFiles(File::isDirectory);
        if (files == null) {
            return;
        }
        String fileName, id, pwd;
        int index;
        for (File file : files) {
            fileName = file.getName();
            index = fileName.indexOf("@");
            id = fileName.substring(0, index);
            pwd = fileName.substring(index + 1);

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            File p12File = new File(file, "apiclient_cert.p12");
            if (!p12File.exists() || !p12File.canRead()){
                logger.info("ignore p12 file for non-existence or no rights to read");
                continue;
            }
            try (FileInputStream stream = new FileInputStream(
                    p12File)) {
                keyStore.load(stream, pwd.toCharArray());
            }
            SSLContext sslcontext = SSLContexts.custom()
                    .loadKeyMaterial(keyStore, pwd.toCharArray())
                    .build();
            SSLConnectionSocketFactory ssf = new SSLConnectionSocketFactory(
                    sslcontext,
                    new String[]{"TLSv1"},
                    null,
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());
            sslMap.put(id, ssf);
            logger.info("loaded cert {}", id);
        }
    }

    public CloseableHttpClient getClientById(String id) {
        SSLConnectionSocketFactory ssf = sslMap.get(id);
        return HttpClients.custom()
                .setSSLSocketFactory(ssf)
                .build();
    }
}
