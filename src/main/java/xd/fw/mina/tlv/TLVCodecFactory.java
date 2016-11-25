package xd.fw.mina.tlv;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xd on 2016/5/13.
 */
public class TLVCodecFactory implements ProtocolCodecFactory {

    static Logger logger = LoggerFactory.getLogger(TLVCodecFactory.class);
    private ProtocolEncoder encoder;
    private ProtocolDecoder decoder;
    private String charset;

    public TLVCodecFactory(String charset) {
        encoder = new TLVEncoder();
        decoder = new TLVDecoder();
        this.charset = charset;
    }

    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
        return encoder;
    }

    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return decoder;
    }

    static byte[] magic = new byte[]{5, 16};// my daughter's birthday
    static int HEAD_LENGTH = magic.length + Integer.SIZE / 8;

    class TLVEncoder extends ProtocolEncoderAdapter {
        @Override
        public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
            TLVMessage msg = (TLVMessage) message;
            IoBuffer buffer = IoBuffer.allocate(1024, false);
            buffer.setAutoExpand(true);
            buffer.put(magic);
            buffer.putInt(0); // placeHolder for message length
            TLVMessage tmp = msg;
            do {
                tmp.fill(buffer, charset);
                tmp = tmp.getNext();
            } while (tmp != null);
            buffer.flip();

            buffer.position(magic.length);
            buffer.putInt(buffer.limit() - HEAD_LENGTH);
            buffer.position(0);
            out.write(buffer);
        }
    }

    class Parse{
        boolean head = false;
        int length;
        void reset(){
            head = false;
        }
    }
    final String KEY = "PARSER";

    class TLVDecoder extends CumulativeProtocolDecoder {
        @Override
        protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
            Parse parse = (Parse)session.getAttribute(KEY);
            if (parse == null){
                parse = new Parse();
                session.setAttribute(KEY,parse);
            }

            while(in.hasRemaining()){
                if (!parse.head){
                    if (in.remaining() >= HEAD_LENGTH){
                        if (in.get() == magic[0]
                                && in.get() == magic[1]) {
                            logger.debug("magic is right.");
                        } else {
                            throw new Exception("wrong magic");
                        }
                        parse.length = in.getInt();
                        parse.head = true;
                    } else {
                        return false;
                    }
                } else {
                    if (in.remaining() >= parse.length){
                        TLVMessage message = null, tmp,currentMsg = null;
                        int position = in.position();
                        while (in.position() - position < parse.length) {
                            tmp = TLVMessage.parse(in, charset);
                            if (message == null) {
                                currentMsg = message = tmp;
                            } else {
                                currentMsg.setNext(tmp);
                                currentMsg = tmp;
                            }
                        }
                        out.write(message);
                        parse.reset();
                    } else {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
