package edu.sjsu.cs249.project1.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

/**
 * RMI Server Interface. The clients will use these methods to communicate with the server.
 * All RMI methods are defined here.
 */
public interface FileServerService extends Remote {

    void register(ClientCallback client) throws RemoteException;
    void unregister(ClientCallback client) throws RemoteException;
    Set<String> listFiles() throws RemoteException;
    void createFile(ClientCallback client, String fileName, byte[] data) throws RemoteException;
    byte[] openFile(ClientCallback client, String fileName) throws RemoteException;
    void removeFile(String fileName) throws RemoteException;
    void editFile(String fileName, byte[] newData) throws RemoteException;
    void renameFile(String fileName, String newFileName) throws RemoteException;

}
