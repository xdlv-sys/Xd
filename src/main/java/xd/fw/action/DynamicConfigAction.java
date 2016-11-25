package xd.fw.action;

import org.springframework.beans.factory.annotation.Autowired;
import xd.fw.Conf;
import xd.fw.bean.DynamicConf;
import xd.fw.bean.Mod;
import xd.fw.service.FwService;

import java.util.List;

public class DynamicConfigAction extends BaseAction{
    @Autowired
    FwService fwService;
    List<DynamicConf> dynamicConfigs;
    DynamicConf dynamicConfig;

    public String obtainDynamicConfigs() throws Exception {
        total = fwService.getAllCount(DynamicConf.class);
        dynamicConfigs = fwService.getList(DynamicConf.class,null,start, limit);
        return SUCCESS;
    }

    public String saveDynamicConfig(){
        fwService.saveOrUpdate(dynamicConfig);

        Conf.triggerChangeListeners(dynamicConfig.getConfName());
        return SUCCESS;
    }

    public DynamicConf getDynamicConfig() {
        return dynamicConfig;
    }

    public void setDynamicConfig(DynamicConf dynamicConfig) {
        this.dynamicConfig = dynamicConfig;
    }

    public List<DynamicConf> getDynamicConfigs() {
        return dynamicConfigs;
    }
}
