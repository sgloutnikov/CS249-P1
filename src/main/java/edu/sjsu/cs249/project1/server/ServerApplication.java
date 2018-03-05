package edu.sjsu.cs249.project1.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerApplication {

    public static void main(final String[] args) {
        System.out.println("+ Server Started +");
        try {
            Registry registry = LocateRegistry.createRegistry(5099);
            registry.rebind("fileService", new FileServer());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
