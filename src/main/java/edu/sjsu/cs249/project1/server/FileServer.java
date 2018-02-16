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
        String clientId = client.getId();
        Client c = new Client(clientId, client);
        try {
            ClientCacheManager.getInstance().registerClient(clientId, c);
        } catch (CacheException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unregister(ClientCallback client) throws RemoteException {
        try {
            ClientCacheManager.getInstance().unregisterClient(client.getId());
        } catch (CacheException e) {
            e.printStackTrace();
        }
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
