package xd.fw.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import xd.fw.bean.Event;
import xd.fw.service.FwService;
import xd.fw.service.IConst;

import java.util.Arrays;
import java.util.List;

/**
 * Created by xd lv on 10/26/2016.
 */
@Service
public abstract class EventJob implements IConst{

    Logger logger = LoggerFactory.getLogger(Event.class);
    @Autowired
    protected FwService fwService;

    @Scheduled(cron="0/10 * * * * ?")
    public void execute() throws Exception {
        List<Event> events = getEvent();
        if (events == null || events.size() < 1){
            return;
        }
        byte[] processType = processType();
        logger.info("start to process event:count: {}", Arrays.toString(processType), processType.length);
        byte eventStatus;
        for (Event event : events){
            try{
                eventStatus = process(event);
            } catch (Throwable e){
                logger.error("",e);
                eventStatus = STATUS_FAIL;
            }
            if (eventStatus == STATUS_FAIL){
                processFailure(event);
            } else {
                //finish the event
                processSuccess(event);
            }
        }
        logger.info("end to process event");
    }

    protected void processFailure(Event event){
        updateEvent(event, STATUS_FAIL);
    }
    protected void processSuccess(Event event){
        updateEvent(event, STATUS_DONE);
    }
    private void updateEvent(Event event, byte status){
        event.setEventStatus(status);
        event.setTryCount((byte)(event.getTryCount() + 1));
        fwService.update(event);
    }

    protected abstract byte process(Event event) throws Exception;

    protected abstract byte[] processType();

    protected List<Event> getEvent() {
        return fwService.getTriggeringEvent(processType(), maxTry(), maxLimit());
    }

    protected int maxTry(){
        return 1;
    }

    protected int maxLimit(){
        return 100;
    }
}
