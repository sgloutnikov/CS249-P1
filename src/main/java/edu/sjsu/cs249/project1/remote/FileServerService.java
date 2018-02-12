package edu.sjsu.cs249.project1.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI Interface
 * All RMI methods are defined here.
 */
public interface FileServerService extends Remote {

    //TODO: Register clients for server to client push
    void register(ClientCallback client) throws RemoteException;
    void unRegister(ClientCallback client) throws RemoteException;
    void hello(String msg) throws RemoteException;
    String version() throws RemoteException;

}
