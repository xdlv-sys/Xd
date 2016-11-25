package xd.fw.scheduler;

import org.springframework.context.ApplicationEvent;

public class UpgradeProxyEvent extends ApplicationEvent {

    String id;
    int version;

    public UpgradeProxyEvent(String id, int version) {
        super(id);
        this.id = id;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }
}
