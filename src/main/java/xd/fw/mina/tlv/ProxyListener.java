package xd.fw.mina.tlv;


import org.apache.mina.api.IoSession;

public interface ProxyListener {
    void proxyCreated(String parkId, IoSession session);

    void proxyRemoved(String parkId, IoSession session);
}