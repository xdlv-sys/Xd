package xd.fw.service.impl;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xd.fw.FwUtil;
import xd.fw.bean.Event;
import xd.fw.bean.Mod;
import xd.fw.bean.Role;
import xd.fw.bean.User;
import xd.fw.service.FwService;
import xd.fw.service.IConst;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Service
public class FwServiceImpl extends HibernateServiceImpl implements FwService, IConst {

    @Override
    public User userLogin(User user) throws Exception {
        List<User> users = htpl.execute(new HibernateCallback<List<User>>() {
            @Override
            public List<User> doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("from User where name=:name");
                query.setParameter("name",user.getName());
                return query.list();
            }
        });
        User ret;
        if (users.size() > 0
                && (ret = users.get(0)).getPassword().equals(FwUtil.md5(user.getPassword()))){
            return ret;
        }
        return null;
    }

    @Override
    @Transactional
    public void saveOrUpdateUser(User user) throws Exception {
        User record;
        if (user.getId() != null) {
            record = get(User.class, user.getId());
            if (!record.getPassword().equals(user.getPassword())){
                user.setPassword(FwUtil.md5(user.getPassword()));
            }
            //merge(user);
        } else {
            user.setId(getPrimaryKey(User.class));
            user.setPassword(FwUtil.md5(user.getPassword()));
            //save(user);
        }
        for (Role role : user.getRolesL()){
            role = load(Role.class,role.getId());
        }
        user.setRoles(new HashSet<>(user.getRolesL()));
        merge(user);
    }

    @Override
    @Transactional
    public void saveOrUpdateRole(Role role) {
        if (role.getId() == null){
            role.setId(getPrimaryKey(Role.class));
        }
        for (Mod mod : role.getModsL()){
            //check the existences of mod
            load(Mod.class, mod.getId());
        }
        role.setMods(new HashSet<>(role.getModsL()));

        saveOrUpdate(role);
    }

    @Override
    public List<Event> getTriggeringEvent(byte[] eventType, int maxTry, int limit) {
        String events = Arrays.toString(eventType);
        events = events.substring(1, events.length() -1);

        String hsql = String.format("from Event where eventStatus in (%d,%d) " +
                "and tryCount < %d and eventType in (%s)" +
                " and (triggerDate is null or triggerDate < now()) order by eventId"
                , STATUS_INI, STATUS_FAIL, maxTry, events);

        return getList(hsql, null, 0, limit);
    }

    @Override
    @Transactional
    public void triggerEvent(Event event) {
        event.setEventStatus(STATUS_INI);
        event.setTryCount((byte) 0);
        if (event.getTriggerDate() == null) {
            event.setTriggerDate(new Timestamp(System.currentTimeMillis()));
        }
        save(event);
    }
}
