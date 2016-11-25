package xd.fw.mina.tlv;

import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Value;
import xd.fw.I18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class ReversedHandler extends TLVHandler implements IMinaConst, ProxyListener {
    @Value("${mina_timeout}")
    int minaTimeout;

    final static List<String> discardRequests = new LinkedList<>();

    final static Map<String, IoSession> sessionMap = new HashMap<>();

    final static List<ProxyListener> proxyListeners = new ArrayList<>();

    protected static void addProxyListeners(ProxyListener listener) {
        if (!proxyListeners.contains(listener)) {
            proxyListeners.add(listener);
        }
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        TLVMessage msg = (TLVMessage) message;
        int code = (int) msg.getValue();

        if (code == REGISTRY) {
            String id = (String) msg.getNextValue(0);
            if ("000".equals(id)){
                return;
            }
            synchronized (sessionMap) {
                IoSession proxySession = sessionMap.get(id);
                if (session != proxySession) {
                    if (proxySession != null){
                        proxySession.closeNow();
                        logger.info("drop old session:{} from {}", id, proxySession.getRemoteAddress());
                    }
                    sessionMap.put(id, session);
                    for (ProxyListener listener : proxyListeners) {
                        listener.proxyCreated(id, session);
                    }
                }
            }
            session.setAttribute(ID_KEY, id);
            handlerRegistry(msg, session);
            return;
        }

        logger.info("receive: {}", message);
        // message is handled already
        if (handlerMessage(msg,session)){
            return;
        }

        String messageId = (String)msg.getNextValue(0);

        boolean discard;
        synchronized (discardRequests) {
            discard = discardRequests.remove(messageId);
        }
        if (discard) {
            logger.debug("discard message for timeout:{} ",messageId);
        } else {
            session.setAttribute(messageId, msg);
        }
    }

    protected boolean handlerMessage(TLVMessage msg, IoSession session){
        return false;
    }

    protected void handlerRegistry(TLVMessage msg, IoSession session) {
    }

    public final boolean upgrade(String id, int version) throws Exception {
        File[] patches = I18n.getPatches(version);
        if (patches == null || patches.length < 1) {
            return false;
        }
        TLVMessage message = createRequest(UPGRADE,1);
        TLVMessage result = request(id, message);
        if (result == null){
            return false;
        }
        String dir = (String)result.getValue();
        for (File patch : patches) {
            pushFile(id,dir,patch);
        }
        //trigger UPGRADE
        message = createRequest(UPGRADE,2);
        result = request(id, message);
        return result != null && "OK".equals(result.getValue());
    }

    public final boolean pushFile(String id,String directory,File file) throws Exception{

        try(InputStream is = new FileInputStream(file)){
            byte[] content = new byte[is.available()];
            is.read(content);
            TLVMessage message = createRequest(PUSH_FILE,directory,file.getName(),content);
            TLVMessage result = request(id, message);
            return result != null && "OK".equals(result.getValue());
        }
    }

    public final byte[] pullFile(String id, String directory, String file) throws Exception{
        TLVMessage message = createRequest(PULL_FILE,directory,file);
        message.timeout = 4000;
        TLVMessage result = request(id, message);
        if ("OK".equals(result.getValue())){
            return (byte[])result.getNextValue(0);
        }
        return null;
    }

    public final String[] executeCmd(String id, String directory, String prefix,String cmd) throws Exception{
        if (cmd.startsWith("cd ")){
            String subPath = cmd.substring(3);
            if (StringUtils.isNotBlank(directory)){
                directory = new File(directory,subPath).getPath();
            }
            return new String[]{directory, cmd};
        }
        if (cmd.startsWith("push ")){
            String file = cmd.substring(5);
            File pushFile = new File(I18n.getWebInfDir(), file);
            if (!pushFile.exists() || pushFile.isDirectory()){
                cmd = "file is directory or don't exist";
                return new String[]{directory, cmd};
            }
            boolean success = pushFile(id,directory,pushFile);
            cmd = success ? "success" : "fail";
            return new String[]{directory, cmd};
        }

        TLVMessage message = createRequest(EXECUTE
                , directory == null ? "" : directory, prefix + " " + cmd);
        TLVMessage result = request(id, message);
        if (result == null){
            return null;
        }
        return new String[]{(String)result.getValue(), (String)result.getNextValue(0)};
    }

    public final void upgrade(int id, int version){

    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);
        removeSession(session);
    }

    protected TLVMessage createRequest(Object ... args){
        TLVMessage message = new TLVMessage(Integer.parseInt(args[0] + ""));
        // add timestamp after code
        TLVMessage next = message.setNext(generateId());
        int i = 0;
        while (args.length > ++i){
            next = next.setNext(args[i]);
        }
        return message;
    }

    private void removeSession(IoSession session) {
        String id = (String) session.getAttribute(ID_KEY);
        if (StringUtils.isNotBlank(id)) {
            synchronized (sessionMap) {
                if (sessionMap.remove(id) != null) {
                    for (ProxyListener listener : proxyListeners) {
                        listener.proxyRemoved(id, session);
                    }
                }
            }
        }
    }

    protected String generateId(){
        return String.valueOf(UUID.randomUUID());
    }

    private IoSession getSession(String id) {
        synchronized (sessionMap) {
            return sessionMap.get(id);
        }
    }

    protected List<TLVMessage> notifyAllId(TLVMessage message){
        List<TLVMessage> messages = new ArrayList<>();
        Collection<IoSession> sessions = new HashSet<>();
        synchronized (sessionMap) {
            sessions.addAll(sessionMap.values());
        }
        TLVMessage ret;
        for (IoSession session : sessions){
            ret = doSend(session, message);
            if (ret == null){
                logger.warn("fail to notify {}" , session.getAttribute(ID_KEY));
            } else {
                messages.add(ret);
            }
            //reset message id
            message.getNext(0).setValue(generateId());
        }

        return messages;
    }

    protected TLVMessage request(String id, TLVMessage message) {
        IoSession session = getSession(id);
        if (session == null) {
            logger.info("there is no park session:{}" , id);
            return null;
        }
        return doSend(session, message);
    }

    private TLVMessage doSend(IoSession session, TLVMessage message){

        // timestamp is just behind code
        String messageId = (String)message.getNextValue(0);
        session.write(message).awaitUninterruptibly();

        TLVMessage ret;
        int count = 0;
        int timeout = message.timeout > 0 ? message.timeout : minaTimeout;
        while ((ret = (TLVMessage) session.removeAttribute(messageId)) == null) {
            if (count++ > timeout) {
                break;
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
        }
        if (ret == null) {
            synchronized (discardRequests) {
                discardRequests.add(messageId);
            }

            logger.info("add discard message:{}" , messageId);
            return null;
        }
        /*remove code adn timestamp*/
        ret = ret.getNext(1);
        if (ret.getValue() instanceof Integer
                && (int) ret.getValue() == NULL_MSG){
            return null;
        }
        return ret;
    }

    @Override
    public void proxyCreated(String id, IoSession session) {
        logger.info("proxy create:{} from {}", id, session.getRemoteAddress());
    }

    @Override
    public void proxyRemoved(String id,IoSession session) {
        logger.info("proxy remove:{}", id);
    }
}
