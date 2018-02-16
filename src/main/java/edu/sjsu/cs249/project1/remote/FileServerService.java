package edu.sjsu.cs249.project1.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI Server Interface
 * All RMI methods are defined here.
 */
public interface FileServerService extends Remote {

    void register(ClientCallback client) throws RemoteException;
    void unregister(ClientCallback client) throws RemoteException;
    void hello(String msg) throws RemoteException;
    String version() throws RemoteException;

}
