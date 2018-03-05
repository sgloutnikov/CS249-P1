package edu.sjsu.cs249.project1.client;

import edu.sjsu.cs249.project1.remote.FileServerService;


import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.Set;
import java.nio.charset.*;

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
                "create <filename> <contents>- create a file with the given name \n"+
                "open <filename> - Open a file. The file can be modified. Press return to save and exit\n" +
                "modify <filename> <new data> - edit a file with new contents and update local cache \n " +
                "rm <filename> - Delete a file\n" +
                "rename <filename> - Rename a file\n" +
                "help - displays this message\n" +
                "exit - Exits the client\n";

        while (!input.equalsIgnoreCase("exit")) {
            System.out.print("client:~$ ");
            input = scanner.nextLine();

            switch (input.split(" ")[0].toLowerCase()) {
                case "help": {
                    System.out.println(help);
                    break;
                }

                // this will list all the files on the server

                case "ls": {

                    System.out.println("List files on the server");
                    Set<String> temp = serverService.listFiles(client1);
                    client1.listFiles(temp);

                    break;
                }

                // this will deliver contents of specified filename either locally (when cached and valid) or remotely
                //  through server.
                case "open": {
                    byte[] temp = null;
                    String fileName = input.split(" ")[1];
                    System.out.println("Opening " + fileName);
                    File file=client1.getCachedFile(fileName);
                    if(file!=null){
                        temp=file.getData();
                        client1.readFiles(temp);
                    }
                    else {
                        temp = serverService.openFiles(client1, fileName);
                        if (temp==null){
                            System.out.println("There is no such file on the server!");
                        }
                        else
                            client1.readFiles(temp);
                    }

                    break;
                }

                // delete a file on the server. Besides ClientCallback client's sendCacheInvalidationEvent(), local
                // cache will be removed as well.

                case "rm": {

                    String fileName = input.split(" ")[1];
                    System.out.println("Deleting " + fileName);
                    serverService.removeFiles(client1, fileName);
                    client1.removeFiles(fileName);
                    break;
                }

                // create a file on the server and add to local cache.

                case "create": {
                    String fileName = input.split(" ")[1];
                    byte[] contents = input.split(" ")[2].getBytes(Charset.forName("UTF-8"));
                    System.out.println("Creating: " + fileName);
                    serverService.createFiles(client1, fileName, contents);
                    client1.createFiles(fileName, contents);
                    break;
                }

                // Replace the byte array and update local cache as well.
                case "modify": {
                    String fileName = input.split(" ")[1];
                    byte[] newContents = input.split(" ")[2].getBytes(Charset.forName("UTF-8"));
                    System.out.println("Modifying: " + fileName);
                    serverService.editFiles(client1, fileName, newContents);
                    client1.modifyFiles(fileName, newContents);
                    break;
                }

                // Replace the file name with a new file name
                case "rename": {
                    String fileName = input.split(" ")[1];
                    String newName = input.split(" ")[2];
                    System.out.println("Renaming: " + fileName + " to: " + newName);
                    serverService.renameFile(client1, fileName, newName);
                    client1.renameFiles(fileName, newName);
                    break;
                }

                //  exit system

                case "exit": {
                    serverService.unregister(client1);
                    break;
                }

                default:
                    System.out.println("Invalid command. Enter 'help' for help.");
            }
        }

        System.exit(0);
    }
}