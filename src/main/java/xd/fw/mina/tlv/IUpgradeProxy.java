package xd.fw.mina.tlv;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IUpgradeProxy extends Remote {
    void start(String cmd) throws RemoteException;

    void stop() throws RemoteException;
}
