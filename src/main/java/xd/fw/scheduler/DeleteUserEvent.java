package xd.fw.scheduler;

import org.springframework.context.ApplicationEvent;
import xd.fw.bean.User;

public class DeleteUserEvent extends ApplicationEvent {

    public DeleteUserEvent(User source) {
        super(source);
    }
}
