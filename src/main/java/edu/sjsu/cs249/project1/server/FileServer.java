package edu.sjsu.cs249.project1.server;

import edu.sjsu.cs249.project1.remote.ClientCallback;
import edu.sjsu.cs249.project1.remote.FileServerService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Implements the RMI Interface
 */
public class FileServer extends UnicastRemoteObject implements FileServerService {

    public FileServer() throws RemoteException {
        super();
    }

    @Override
    public void register(ClientCallback client) throws RemoteException {
        System.out.println("Registered " + client.getId());
        //TODO: Implement tracking of client on the server side
    }

    @Override
    public void unRegister(ClientCallback client) throws RemoteException {

    }

    @Override
    public void hello(String msg) {
        System.out.println("Client said: " + msg);
    }

    @Override
    public String version() {
        return "Server is running version 0.1";
    }


}
