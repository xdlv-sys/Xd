package xd.fw.mina.tlv;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.filter.logging.LoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BufferLoggingFilter extends LoggingFilter{

    Logger logger = LoggerFactory.getLogger(BufferLoggingFilter.class);

    public BufferLoggingFilter(){
        super(BufferLoggingFilter.class);
    }
    public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
        if (logger.isDebugEnabled()){
            logger.debug("RECEIVE: {}",((IoBuffer)message).getHexDump());
        }
        nextFilter.messageReceived(session, message);
    }

    @Override
    public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        if (logger.isDebugEnabled()){
            logger.debug("SEND: {}",((IoBuffer)writeRequest.getMessage()).getHexDump());
        }
        super.filterWrite(nextFilter, session, writeRequest);
    }
}
