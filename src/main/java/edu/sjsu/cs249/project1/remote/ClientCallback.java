package edu.sjsu.cs249.project1.remote;


import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallback extends Remote {

    void invalidateCache(String file) throws RemoteException;
    String getId() throws RemoteException;
}
