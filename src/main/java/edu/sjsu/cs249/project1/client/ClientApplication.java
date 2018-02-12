package edu.sjsu.cs249.project1.client;

import edu.sjsu.cs249.project1.remote.FileServerService;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientApplication {

    public static void main(final String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        System.out.println("+ Client Started +");

        //TODO: Prompt for name and port.
        Client client1 = new Client("c1");
        UnicastRemoteObject.exportObject(client1, 2001);

        FileServerService serverService = (FileServerService) Naming.lookup("rmi://localhost:5099/fileService");

        serverService.register(client1);
        serverService.hello("Hello from the client via RMI!");
        System.out.println(serverService.version());
    }
}