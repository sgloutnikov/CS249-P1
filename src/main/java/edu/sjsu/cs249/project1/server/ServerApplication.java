package edu.sjsu.cs249.project1.server;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * This class is invoked on startup of the server application. <br/>
 * It is used only to instantiate a FileServer and bind it to the fileService name.
 */
public class ServerApplication {
    public static void main(final String[] args) {
        System.out.println("+ Server Started +");
        try {
            /**
             * Create registry at 5099.
             */
            final Registry registry = LocateRegistry.createRegistry(5099);

            /**
             * Create a new FileServer and bind it to the name "fileService".
             */
            registry.bind("fileService", new FileServer());
            System.out.println("Server initialization complete.");
        } catch (final RemoteException | AlreadyBoundException e) {
            e.printStackTrace();
        }
    }
}