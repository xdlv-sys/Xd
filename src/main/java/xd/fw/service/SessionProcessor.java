package xd.fw.service;

import org.springframework.orm.hibernate5.HibernateTemplate;

public interface SessionProcessor<T> {
    T process(HibernateTemplate htpl);
}
