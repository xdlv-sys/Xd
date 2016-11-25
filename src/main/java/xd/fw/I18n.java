package xd.fw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class I18n {

    static Logger logger = LoggerFactory.getLogger(I18n.class);
    static Properties properties;
    static File webInfoDir;

    static {
        properties = new Properties();
        String i18nFile = "/i18n.properties";
        try (InputStreamReader reader = new InputStreamReader(I18n.class.getResourceAsStream(
                i18nFile), FwUtil.UTF8)) {
            properties.load(reader);
            webInfoDir = new File(I18n.class.getResource(i18nFile).toURI().toURL().getFile()).getParentFile().getParentFile();
        } catch (Exception e) {
            logger.error("can not load i18n files", e);
        }
    }

    public static File getWebInfDir(){
        return webInfoDir;
    }
    public static File getPatchDir(){
        return new File(getWebInfDir(),"patch");
    }

    static Pattern patchPattern = Pattern.compile("v(\\d+)\\.zip");

    public static File[] getPatches(int version){
       return new File(I18n.getWebInfDir(), "patch").listFiles((f) -> {
           Matcher matcher = patchPattern.matcher(f.getName());
           return matcher.find() && Integer.parseInt(matcher.group(1)) > version;
       });
    }

    public static String getI18n(String key) {
        return properties.getProperty(key, key);
    }

    public static void setI18n(String key, String value){
        properties.setProperty(key, value);
    }
}
