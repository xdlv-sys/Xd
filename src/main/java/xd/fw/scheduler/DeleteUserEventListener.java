package xd.fw.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Async
public class DeleteUserEventListener implements ApplicationListener<DeleteUserEvent> {
    Logger logger = LoggerFactory.getLogger(DeleteUserEventListener.class);

    @Override
    public void onApplicationEvent(DeleteUserEvent deleteUserEvent) {
        logger.info("receive event: {} in thread:{}"
                , deleteUserEvent.getSource(), Thread.currentThread().getName());
    }
}
