package edu.sjsu.cs249.project1.client;

import edu.sjsu.cs249.project1.remote.FileServerService;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ClientApplication {

    public static void main(final String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        // TODO Block and prompt for commands from user
        System.out.println("+ Client Started +");

        FileServerService serverService = (FileServerService) Naming.lookup("rmi://localhost:5099/fileService");
        serverService.hello("Hello from the client via RMI!");
        System.out.println(serverService.version());
    }
}