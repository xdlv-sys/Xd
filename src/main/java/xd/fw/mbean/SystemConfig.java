package xd.fw.mbean;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;
import xd.fw.I18n;

/**
 * Created by exiglvv on 6/1/2016.
 */
@Service
@ManagedResource(objectName = "xapp:name=SystemConfig", description = "System configuration")
public class SystemConfig {

    @ManagedOperation(description = "Modify i18n file in run time")
    public void modifyI18n(String key, String value){
        I18n.setI18n(key, value);
    }
}
