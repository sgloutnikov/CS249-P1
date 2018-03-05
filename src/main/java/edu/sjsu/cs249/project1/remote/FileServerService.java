package edu.sjsu.cs249.project1.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

/**
 * RMI Server Interface
 * All RMI methods are defined here.
 */
public interface FileServerService extends Remote {

    void register(ClientCallback client) throws RemoteException;
    void unregister(ClientCallback client) throws RemoteException;
    void hello(String msg) throws RemoteException;
    String version() throws RemoteException;
    Set<String> listFiles(ClientCallback client) throws RemoteException;
    void createFiles(ClientCallback client, String fileName, byte[] data) throws RemoteException;
    byte[] openFiles(ClientCallback client, String fileName) throws RemoteException;
    void removeFiles(ClientCallback client, String fileName) throws RemoteException;
    void editFiles(ClientCallback client, String fileName, byte[] newData) throws RemoteException;
    void renameFile(ClientCallback client, String fileName, String newFileName) throws RemoteException;

}
