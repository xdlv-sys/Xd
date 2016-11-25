package xd.fw.mina.tlv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

public class UpgradeProxyHook extends UnicastRemoteObject implements IUpgradeProxy {
    static int port = 38888;
    static String URL = "rmi://localhost:" + port + "/UpgradeProxyHook";
    static Logger logger = LoggerFactory.getLogger(UpgradeProxyHook.class);

    protected UpgradeProxyHook() throws RemoteException {
    }

    public static void main(String[] args) throws Exception {
        logger.info("proxy:{}", Arrays.toString(args));
        String action = args != null && args.length > 0 ? args[0] : "start";

        if (action.equals("start")) {
            tryStop();
            Thread.sleep(1000);

            LocateRegistry.createRegistry(port);
            Naming.bind(URL, new UpgradeProxyHook());
            logger.info("proxy started.");
        }
        if (action.equals("stop")) {
            tryStop();
        }
    }

    public static void tryStop() {
        try {
            IUpgradeProxy upgradeProxy = (IUpgradeProxy) Naming.lookup(URL);
            upgradeProxy.stop();
            logger.info("stop hook already");
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public static void upgrade(String upgradeBatch) throws Exception {
        IUpgradeProxy upgradeProxy = (IUpgradeProxy) Naming.lookup(URL);
        upgradeProxy.start(upgradeBatch);
    }


    @Override
    public void start(String upgradeBatch) throws RemoteException {
        logger.info("start upgrade");
        new Thread() {
            public void run() {
                ProcessBuilder builder = new ProcessBuilder(upgradeBatch);
                builder.directory(new File(upgradeBatch).getParentFile());
                Process process;
                try {
                    process = builder.start();
                    BufferedReader ins = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = ins.readLine()) != null) logger.info(line);
                    ins.close();
                    process.waitFor();
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        }.start();
    }

    public void stop() throws RemoteException {
        System.exit(0);
    }

}
