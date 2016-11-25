package xd.fw.bean;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "t_event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int eventId;
    private byte eventType;
    private int dbKey;
    private int dbInt;
    private String dbContent;
    private byte eventStatus;
    private byte tryCount;
    private Timestamp triggerDate;

    public Event(){}

    public Event(byte eventType, int dbKey, int dbInt) {
        this.eventType = eventType;
        this.dbKey = dbKey;
        this.dbInt = dbInt;
    }
    public Event(byte eventType, int dbKey, String dbContent) {
        this.eventType = eventType;
        this.dbKey = dbKey;
        this.dbContent = dbContent;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public byte getEventType() {
        return eventType;
    }

    public void setEventType(byte eventType) {
        this.eventType = eventType;
    }

    public int getDbKey() {
        return dbKey;
    }

    public void setDbKey(int dbKey) {
        this.dbKey = dbKey;
    }

    public int getDbInt() {
        return dbInt;
    }

    public void setDbInt(int dbInt) {
        this.dbInt = dbInt;
    }

    public String getDbContent() {
        return dbContent;
    }

    public void setDbContent(String dbContent) {
        this.dbContent = dbContent;
    }

    public byte getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(byte eventStatus) {
        this.eventStatus = eventStatus;
    }

    public byte getTryCount() {
        return tryCount;
    }

    public void setTryCount(byte tryCount) {
        this.tryCount = tryCount;
    }

    public Timestamp getTriggerDate() {
        return triggerDate;
    }

    public void setTriggerDate(Timestamp triggerDate) {
        this.triggerDate = triggerDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event jknEvent = (Event) o;

        if (eventId != jknEvent.eventId) return false;
        if (eventType != jknEvent.eventType) return false;
        if (dbKey != jknEvent.dbKey) return false;
        if (dbInt != jknEvent.dbInt) return false;
        if (eventStatus != jknEvent.eventStatus) return false;
        if (tryCount != jknEvent.tryCount) return false;
        if (dbContent != null ? !dbContent.equals(jknEvent.dbContent) : jknEvent.dbContent != null) return false;
        if (triggerDate != null ? !triggerDate.equals(jknEvent.triggerDate) : jknEvent.triggerDate != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = eventId;
        result = 31 * result + (int) eventType;
        result = 31 * result + dbKey;
        result = 31 * result + dbInt;
        result = 31 * result + (dbContent != null ? dbContent.hashCode() : 0);
        result = 31 * result + (int) eventStatus;
        result = 31 * result + (int) tryCount;
        result = 31 * result + (triggerDate != null ? triggerDate.hashCode() : 0);
        return result;
    }
}
