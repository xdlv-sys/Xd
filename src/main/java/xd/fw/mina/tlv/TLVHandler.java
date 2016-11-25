package xd.fw.mina.tlv;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xd.fw.service.IConst;

/**
 * Created by xd on 2016/5/13.
 */
@Service("tlvHandler")
public class TLVHandler extends IoHandlerAdapter implements IConst{
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        logger.debug("session idle:" + status);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        logger.error("exception caught in handler", cause);
        session.closeNow();
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        logger.info("receive:" + message);
        session.write(new TLVMessage("OK"));
    }
}
