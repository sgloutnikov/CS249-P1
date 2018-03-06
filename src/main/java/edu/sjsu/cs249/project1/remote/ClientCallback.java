package edu.sjsu.cs249.project1.remote;


import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI Client Callback Interface. The server will use these methods to reach the client back when it needs
 * to initiate the communication.
 */
public interface ClientCallback extends Remote {

    void invalidateCache(String file) throws RemoteException;
    String getId() throws RemoteException;
}
