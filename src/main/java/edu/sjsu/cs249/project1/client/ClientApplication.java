package edu.sjsu.cs249.project1.client;

import edu.sjsu.cs249.project1.remote.FileServerService;


import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class ClientApplication {

    public static void main(final String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a valid client port number: ");
        int port = scanner.nextInt();

        Client client1 = new Client("client-" + System.currentTimeMillis()/1000L);
        UnicastRemoteObject.exportObject(client1, port);
        FileServerService serverService = (FileServerService) Naming.lookup("rmi://localhost:5099/fileService");
        serverService.register(client1);
        System.out.println("+ Client Started +");

        String input = "";
        String help = "Please enter a command. Available commands:\n" +
                "ls - Lists the available files\n" +
                "open <filename> - Open a file. The file can be modified. Press return to save and exit\n" +
                "rm <filename> - Delete a file\n" +
                "help - displays this message\n" +
                "exit - Exits the client\n";

        while (!input.equalsIgnoreCase("exit")) {
            System.out.print("client:~$ ");
            input = scanner.nextLine();

            switch (input.toLowerCase().split(" ")[0]) {
                case "help": {
                    System.out.println(help);
                    break;
                }

                // this will list all the files on the server associated with current client


                case "ls": {
                    //TODO: Implement
                    System.out.println("List files");
                    serverService.listFiles(client1);
                    break;
                }

                // this will deliver contents of specified filename
                case "open": {
                    String fileName = input.split(" ")[1];
                    System.out.println("Opening " + fileName);
                    serverService.openFile(fileName);


                    break;
                }
                case "rm": {
                    //TODO: Implement
                    String fileName = input.split(" ")[1];
                    System.out.println("Deleting " + fileName);
                    break;
                }
                case "exit": {
                    serverService.unregister(client1);
                    break;
                }

                default:
                    System.out.println(help);
            }
        }

        System.exit(0);
    }
}