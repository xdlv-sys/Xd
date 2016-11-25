package xd.fw.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.transaction.annotation.Transactional;
import xd.fw.FwUtil;
import xd.fw.bean.PrimaryKey;
import xd.fw.service.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional(readOnly = true)
public class HibernateServiceImpl implements BaseService{
    static Map<String, int[]> primaryKeyMap = new HashMap<String, int[]>();
    final static int PRE_OCCUPY_MAX = 10;

    @Autowired(required = false)
    protected HibernateTemplate htpl;

    @Override
    @Transactional
    public <T> T runInSession(SessionProcessor<T> processor) {
        return processor.process(htpl);
    }

    @Override
    @Transactional
    public void runSessionCommit(SessionCommit sessionCommit) {
        sessionCommit.process(htpl);
    }

    public int getPrimaryKey(Class<?> cls){
        int[] value;
        String tableName = cls.getSimpleName();
        synchronized (primaryKeyMap) {
            value = primaryKeyMap.get(tableName);
            if (value == null || value[0] == value[1]) {
                PrimaryKey primaryKey = get(PrimaryKey.class, tableName);
                if (primaryKey == null){
                    primaryKey = new PrimaryKey();
                    primaryKey.setCurrentId(PRE_OCCUPY_MAX);
                    primaryKey.setTableName(tableName);
                    save(primaryKey);
                    value = new int[]{1, PRE_OCCUPY_MAX};
                } else {
                    value = new int[]{primaryKey.getCurrentId() + 1, primaryKey.getCurrentId() + PRE_OCCUPY_MAX};
                    primaryKey.setCurrentId(value[1]);
                    update(primaryKey);
                }
                primaryKeyMap.put(tableName, value);
            } else {
                value[0] += 1;
            }
        }
        return value[0];
    }

    @Override
    @Transactional
    public void saveOrUpdate(Object entity) {
        htpl.saveOrUpdate(entity);
    }

    private <T> String constructHsql(String prefix, Class<T> cls, T params, String orderBy){
        StringBuffer hsql = new StringBuffer(prefix).append("from ").append(cls.getSimpleName());
        if (params != null){
            final boolean[] appendWhere = new boolean[]{false};
            try {
                FwUtil.invokeBeanFields(params, (f, o) -> {
                    if (o != null && StringUtils.isNotBlank(o.toString())) {
                        if (!appendWhere[0]) {
                            hsql.append(" where ");
                            appendWhere[0] = true;
                        } else {
                            hsql.append(" and ");
                        }
                        if (o instanceof String){
                            hsql.append(f.getName()).append(" like :").append(f.getName());
                        } else {
                            hsql.append(f.getName()).append("=:").append(f.getName());
                        }
                    }
                });
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        if (StringUtils.isNotBlank(orderBy)){
            hsql.append(" order by ").append(orderBy);
        }
        return hsql.toString();
    }

    public <T> int getAllCount(Class<T> cls) {
        return getAllCount(cls,null);
    }

    public <T> int getAllCount(Class<T> cls, T params){
        List<Object> list = this.getList(constructHsql("select count(*) ",cls, params, null), params, -1,0);
        return ((Long) list.get(0)).intValue();
    }

    public <T> List<T> getList(Class<T> cls, String orderBy, int start, int limit){
        return getList(cls,null,orderBy,start,limit);
    }

    public <T> List<T> getList(Class<T> cls, T param, String orderBy, int start, int limit) {
        return this.getList(constructHsql("",cls,param,orderBy), param, start, limit);
    }

    public <T> List<T> getList(String hsql, T params, int start, int limit){
        return htpl.execute(new HibernateCallback<List<T>>() {
            @Override
            public List<T> doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(hsql);
                if (params != null){
                    try {
                        FwUtil.invokeBeanFields(params, (f, o) -> {
                            if (o != null && StringUtils.isNotBlank(o.toString())) {
                                if (o instanceof String){
                                    query.setParameter(f.getName(),"%" + o + "%");
                                } else {
                                    query.setParameter(f.getName(),o);
                                }
                            }
                        });
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (start > -1){
                    query.setFirstResult(start);
                }
                if (limit > 0){
                    query.setMaxResults(limit);
                }
                return query.list();
            }
        });
    }

    public <T> List<T> getLists(String hsql,T param, SetParameters setParameters, ConstructHql<T> constructHql, int start, int limit){
        return htpl.execute(new HibernateCallback<List<T>>() {
            @Override
            public List<T> doInHibernate(Session session) throws HibernateException {
                String hsql2 = hsql + constructHql.process(param);
                Query query = session.createQuery(hsql2);
                setParameters.process(query);
                if (start > -1){
                    query.setFirstResult(start);
                }
                if (limit > 0){
                    query.setMaxResults(limit);
                }
                return query.list();
            }
        });
    }

    @Override
    public <T> List<T> getLists(String hsql, SetParameters setParameters) {
        return htpl.execute(new HibernateCallback<List<T>>() {
            @Override
            public List<T> doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(hsql);
                if (setParameters != null){
                    setParameters.process(query);
                }
                return query.list();
            }
        });
    }

    public <T> int getAllCount(T param, SetParameters setParameters, ConstructHql<T> constructHql){
        return htpl.execute(new HibernateCallback<List<Long>>() {
            @Override
            public List<Long> doInHibernate(Session session) throws HibernateException {
                String hsql2 = "select count(*) " + constructHql.process(param);
                Query query = session.createQuery(hsql2);
                setParameters.process(query);
                return query.list();
            }
        }).get(0).intValue();
    }

    public int update(String sql, SetParameters setParameters, boolean original){
        return htpl.execute(new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(Session session) throws HibernateException {
                Query query;
                if (original){
                    query = session.createSQLQuery(sql);
                } else {
                    query = session.createQuery(sql);
                }

                if (setParameters != null){
                    setParameters.process(query);
                }
                return query.executeUpdate();
            }
        });
    }
    public <T> T getOne(String where, Class<T> cls, SetParameters setParameters){
        String hsql = "from " + cls.getSimpleName() + " where " + where;
        List<Object> lists = getLists(hsql, setParameters);
        if (lists == null || lists.size() < 1){
            return null;
        }
        return (T)lists.get(0);
    }

    public <T> T get(Class<T> tClass, Serializable id) {
        return htpl.get(tClass, id);
    }


    protected <T> T load(Class<T> tClass, Serializable id) {
        return htpl.load(tClass, id);
    }

    @Override
    @Transactional
    public void update(Object entity) {
        htpl.update(entity);
    }

    @Override
    @Transactional
    public void save(Object entity) {
        htpl.save(entity);
    }

    @Override
    @Transactional
    public Object merge(Object entity) {
        return htpl.merge(entity);
    }

    @Override
    @Transactional
    public void delete(Object entity) {
        htpl.delete(entity);
    }

    @Override
    @Transactional
    public void delete(Class<?> cls, Serializable id) {
        Object record = htpl.get(cls,id);
        htpl.delete(record);
    }
}