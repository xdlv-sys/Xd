package xd.fw.service;
import xd.fw.bean.Event;
import xd.fw.bean.Role;
import xd.fw.bean.User;

import java.util.List;

public interface FwService extends BaseService{

    User userLogin(User user) throws Exception;

    void saveOrUpdateUser(User user) throws Exception;

    void saveOrUpdateRole(Role role);

    List<Event> getTriggeringEvent(byte[] bytes, int maxTry, int limit);

    void triggerEvent(Event event);

}
