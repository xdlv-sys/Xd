package xd.fw.service.impl;

import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly=true)
public abstract class MyBatisServiceImpl extends BaseServiceImpl{
}
