package xd.fw.mina.tlv;



import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;

/**
 * Created by xd on 2016/5/14.
 */
public class Test extends IoHandlerAdapter {
    public static final int CONNECT_TIMEOUT = 3000;

    private String host;
    private int port;
    private SocketConnector connector;
    private IoSession session;

    public Test(String host, int port) {
        this.host = host;
        this.port = port;
        connector = new NioSocketConnector();
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(
                new TLVCodecFactory("UTF-8")));
        connector.setHandler(this);

        ConnectFuture future = connector.connect(new InetSocketAddress(host, port));
        future.awaitUninterruptibly();
        session = future.getSession();

        TLVMessage tlvMessage = new TLVMessage(123);
        TLVMessage floatMessage = new TLVMessage(12.01f);
        TLVMessage stringMessage = new TLVMessage("Hello word");
        tlvMessage.setNext(floatMessage).setNext(stringMessage);
        session.write(tlvMessage);

        TLVMessage tlvMessage2 = new TLVMessage(89L);
        TLVMessage tlvMessage3 = new TLVMessage((byte)9);
        TLVMessage tlvMessage4 = new TLVMessage(1.02);
        tlvMessage2.setNext(tlvMessage3).setNext(tlvMessage4);
        //session.write(tlvMessage2);
    }

    public void messageReceived(IoSession session, Object message) throws Exception {
        System.out.println("receive:" + message);
    }

    public static void main(String[] args)throws Exception{
        Test t = new Test("127.0.0.1",18080);
        System.in.read();
        t.session.closeNow();
        System.exit(0);
    }
}
