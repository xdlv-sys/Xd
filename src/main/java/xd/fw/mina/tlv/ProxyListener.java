package xd.fw.mina.tlv;

import org.apache.mina.core.session.IoSession;

public interface ProxyListener {
    void proxyCreated(String parkId, IoSession session);

    void proxyRemoved(String parkId, IoSession session);
}