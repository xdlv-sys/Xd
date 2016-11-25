package xd.fw.service;

import java.io.Serializable;
import java.util.List;

public interface BaseService extends IConst{

    <T> int getAllCount(Class<T> cls);

    <T> int getAllCount(Class<T> cls, T params);

    <T> List<T> getList(Class<T> cls, T param, String orderBy, int start, int limit);

    <T> List<T> getList(Class<T> cls, String orderBy, int start, int limit);

    int getPrimaryKey(Class<?> cls);

    void saveOrUpdate(Object entity);

    <T> T get(Class<T> tClass, Serializable id);

    //<T> T load(Class<T> tClass,Serializable id);

    void update(Object entity);

    void save(Object entity);

    Object merge(Object entity);

    void delete(Object entity);

    void delete(Class<?> obj, Serializable id);
    <T> T getOne(String where, Class<T> cls, SetParameters setParameters);

    <T> List<T> getLists(String hsql, SetParameters setParameters);

    <T> List<T> getLists(String hsql, T param, SetParameters setParameters, ConstructHql<T> constructHql, int start, int limit);

    <T> int getAllCount(T param, SetParameters setParameters, ConstructHql<T> constructHql);

    <T> T runInSession(SessionProcessor<T> processor);

    void runSessionCommit(SessionCommit sessionCommit);

}