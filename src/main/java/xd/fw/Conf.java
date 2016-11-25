package xd.fw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xd.fw.bean.DynamicConf;
import xd.fw.service.FwService;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Service
public class Conf {
    @Value("${dynamic_config_load}")
    boolean loadFromDb;

    static Logger logger = LoggerFactory.getLogger(Conf.class);

    static List<PropertyChangeListener> propertyChangeListeners = new ArrayList<>();
    public static void addPropertyChangeListener(PropertyChangeListener l){
        synchronized (propertyChangeListeners){
            propertyChangeListeners.add(l);
        }
    }
    public static void triggerChangeListeners(String configName){
        synchronized (propertyChangeListeners){
            for (PropertyChangeListener l: propertyChangeListeners){
                l.process(configName);
            }
        }
    }
    interface PropertyChangeListener {
        void process(String configName);
    }

    @Autowired
    FwService fwService;

    @PostConstruct
    public void readFromDb() {
        if (!loadFromDb){
            return;
        }
        List<DynamicConf> dynamicConfigs = fwService.getList(DynamicConf.class, null, 0, -1);
        Field[] fields = this.getClass().getDeclaredFields();
        FwUtil.safeEach(dynamicConfigs, (config) -> {
            String name = config.getConfName();
            Object value;
            for (Field f : fields) {
                if (name.equals(f.getName())) {
                    f.setAccessible(true);
                    value = null;
                    if (f.getType().equals(String.class)) {
                        value = config.getConfValue();
                    } else if (f.getType().equals(byte.class)) {
                        value = Byte.parseByte(config.getConfValue());
                    } else if (f.getType().equals(short.class)) {
                        value = Short.parseShort(config.getConfValue());
                    } else if (f.getType().equals(int.class)) {
                        value = Integer.parseInt(config.getConfValue());
                    } else if (f.getType().equals(boolean.class)) {
                        value = Boolean.parseBoolean(config.getConfValue());
                    } else if (f.getType().equals(long.class)) {
                        value = Long.parseLong(config.getConfValue());
                    } else if (f.getType().equals(short.class)) {
                        value = Short.parseShort(config.getConfValue());
                    }else if (f.getType().equals(float.class)) {
                        value = Float.parseFloat(config.getConfValue());
                    }else if (f.getType().equals(double.class)) {
                        value = Double.parseDouble(config.getConfValue());
                    } else {
                        value = type(f, config);
                    }
                    if (value != null) {
                        try {
                            f.set(this, value);
                            logger.info("CONFIG: " + name + "=" + value);
                        } catch (IllegalAccessException e) {
                            logger.error("", e);
                        }
                    }
                    break;
                }
            }
        });
    }

    //use to extend other data type
    private Object type(Field f, DynamicConf conf) {
        return null;
    }

    public static boolean appUpdating;
}
