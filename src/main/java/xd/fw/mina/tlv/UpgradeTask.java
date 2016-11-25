package xd.fw.mina.tlv;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import xd.fw.FwUtil;
import xd.fw.I18n;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Callable;
@Service
public class UpgradeTask implements Callable<Integer>{

    static Logger logger = LoggerFactory.getLogger(UpgradeTask.class);
    @Value("${version}")
    int version;

    @Qualifier("executor")
    @Autowired()
    AsyncTaskExecutor taskExecutor;
    @Override
    public Integer call() throws Exception {
        File[] patches = I18n.getPatches(version);
        if (patches == null || patches.length < 1){
            return 0;
        }
        Arrays.sort(patches, File::compareTo);
        //prepare patch dest
        File tmp = new File(I18n.getPatchDir(), "tmp");
        FileUtils.deleteDirectory(tmp);

        if (!tmp.mkdirs()){
            throw new Exception("can not create tmp dir");
        }
        //unzip patches
        for (File patch : patches){
            FwUtil.unzip(patch, tmp);
        }
        logger.info("start to notify update proxy");
        //
        UpgradeProxyHook.upgrade(new File(tmp,"upgrade.bat").getCanonicalPath());
        logger.info("end to notify update proxy");
        return 0;
    }
}
