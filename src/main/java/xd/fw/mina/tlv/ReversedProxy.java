package xd.fw.mina.tlv;


import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import xd.fw.FwUtil;
import xd.fw.I18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;

@Service
public abstract class ReversedProxy implements IMinaConst {

    protected static Logger logger = LoggerFactory.getLogger(ReversedProxy.class);
    private SocketConnector connector;
    private IoSession session;
    private boolean stop = false;

    @Qualifier("executor")
    @Autowired
    AsyncTaskExecutor taskExecutor;
    @Autowired
    UpgradeTask upgradeTask;

    public ReversedProxy() {
        connector = new NioSocketConnector(MinaWrapper.getPool());
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(
                new TLVCodecFactory(FwUtil.UTF8)));
        connector.setHandler(new TLVHandler() {
            @Override
            public void messageReceived(IoSession session, Object message) throws Exception {
                TLVMessage msg = (TLVMessage) message;
                if (!processInnerMessage(msg)) {
                    handlerQuery(msg);
                }
            }

            @Override
            public void sessionClosed(IoSession session) throws Exception {
                synchronized (ReversedProxy.this){
                    ReversedProxy.this.notifyAll();
                }
            }
        });
    }

    private boolean processInnerMessage(TLVMessage msg) throws Exception {
        int code = (int) msg.getValue();
        TLVMessage next = msg.getNext(0);
        switch (code) {
            case EXECUTE:
                String directory = (String) next.getNextValue(0);
                File dir = I18n.getWebInfDir().getParentFile().getParentFile().getParentFile();
                if (StringUtils.isNotBlank(directory) && new File(directory).exists()) {
                    dir = new File(directory);
                }
                String command = (String) next.getNextValue(1);
                String result = FwUtil.executeCmd(taskExecutor,dir, command.split(" "));
                next.setNext(dir.getCanonicalPath()).setNext(result);
                response(msg);
                break;
            case PUSH_FILE:
                directory = (String) next.getNextValue(0);
                String name = (String) next.getNextValue(1);
                File dest = new File(new File(directory), name);
                try (FileOutputStream os = new FileOutputStream(dest)) {
                    os.write((byte[]) next.getNextValue(2));
                    os.flush();
                }
                next.setNext("OK");
                response(msg);
                break;
            case PULL_FILE:
                directory = (String) next.getNextValue(0);
                name = (String) next.getNextValue(1);
                dest = new File(new File(directory), name);
                byte[] buffer;
                try (FileInputStream ins = new FileInputStream(dest)) {
                    buffer = new byte[ins.available()];
                    ins.read(buffer);
                }
                next.setNext("OK").setNext(buffer);
                response(msg);
                break;
            case UPGRADE:
                if ((int) next.getNextValue(0) == 1) {
                    next.setNext(new File(I18n.getWebInfDir(), "patch").getCanonicalPath());
                } else {
                    taskExecutor.submit(upgradeTask);
                    next.setNext("OK");
                }
                response(msg);
                break;
            default:
                return false;
        }
        return true;
    }

    public void destroy() {
        connector.dispose();
        stop = true;
    }

    @Scheduled(cron="0/10 * * * * ?")
    public void heartBeat() throws Exception {
        logger.info("start to send heart beat message session: " +
                "{}",session == null ? "" : "" + session.isActive());
        checkSession();

        TLVMessage registryMessage = new TLVMessage(REGISTRY);
        constructRegistryMessage(registryMessage);

        session.write(registryMessage).awaitUninterruptibly();
    }

    protected abstract void constructRegistryMessage(TLVMessage registryMessage);

    protected abstract InetSocketAddress inetSocketAddress();

    protected abstract void handlerQuery(TLVMessage msg) throws Exception;

    protected void response(TLVMessage response) {
        session.write(response);
    }

    private void checkSession() throws Exception {
        int count = 0;
        boolean reconnect = false;
        while (session == null || !session.isConnected() || !session.isActive()) {
            logger.info("try to connect center");
            reconnect = true;
            ConnectFuture future = connector.connect(inetSocketAddress());
            future.awaitUninterruptibly();
            try {
                if (session != null){
                    session.closeNow();
                }
                session = future.getSession();
            } catch (Exception e) {
                logger.warn("can not connect center, try again later:" + e);
            }
            if (stop){
                return;
            }
            if (count++ > 0) {
                synchronized (this){
                    wait(count * 1000);
                }
            }
        }
        if (reconnect && session != null) {
            logger.info("connected center...");
        }
    }
}