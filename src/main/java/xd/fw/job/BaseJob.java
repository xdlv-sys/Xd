package xd.fw.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xd.fw.service.IConst;

@Service
public abstract class BaseJob implements IConst{

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private boolean destroyed = false;

    protected void destroy(){
        destroyed = true;
    }

    public final void execute() throws Exception{
        if (!destroyed){
            doExecute();
        }
    }

    public abstract void doExecute() throws Exception;
}
