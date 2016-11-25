package xd.fw.service;

import org.hibernate.Query;

public interface SetParameters {
    void process(Query query);
}