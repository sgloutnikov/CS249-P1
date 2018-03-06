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

/**
 * Entry point into the Client application. ClientApplication handles user input and interaction with
 * the Client and registering/interacting with the FileServer.
 */
public class ClientApplication {

    public static void main(final String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        // Prompt for server host and client port
        Scanner scanner = new Scanner(System.in);
        String scannerInput;
        String host;
        int port;
        System.out.print("Enter the server location [localhost]: ");
        scannerInput = scanner.nextLine();
        if (scannerInput.length() > 1) {
            host = scannerInput;
        } else {
            host = "localhost";
        }
        System.out.println("Server host: " + host);
        System.out.print("Enter a valid/open client port number [2000]: ");
        scannerInput = scanner.nextLine();
        if (scannerInput.length() > 1) {
            port = Integer.parseInt(scannerInput);
        } else {
            port = 2000;
        }
        System.out.println("Server port: " + port);

        // Setup Client and connect to Server via RMI
        Client client1 = new Client("client-" + System.currentTimeMillis()/1000L);
        UnicastRemoteObject.exportObject(client1, port);
        FileServerService serverService = (FileServerService) Naming.lookup("rmi://" + host + ":5099/fileService");
        serverService.register(client1);
        System.out.println("+ Client Started +");

        // Handle user input
        String help = "Available commands:\n" +
                "ls - Lists the available files\n" +
                "create <filename> <contents> - create a file with the given name \n"+
                "open <filename> - Open a file. The file can be modified. Press return to save and exit\n" +
                "modify <filename> <new data> - edit a file with new contents and update local cache\n" +
                "rm <filename> - Delete a file\n" +
                "rename <filename> <new filename> - Rename a file\n" +
                "help - displays this message\n" +
                "exit - Exits the client\n";

        boolean keepGoing = true;
        while (keepGoing) {
            System.out.print("client:~$ ");
            String input = scanner.nextLine();
            String[] inputs = input.split(" ");
            String command = inputs[0].toLowerCase();

            switch (command) {
                // list out commands
                case "help": {
                    System.out.println(help);
                    break;
                }

                // lists out files
                case "ls": {
                    Set<String> temp = serverService.listFiles();
                    client1.listFiles(temp);
                    break;
                }

                // this will deliver contents of specified filename either locally (when cached and valid) or remotely
                // through server.
                case "open": {
                    byte[] temp = null;
                    if (inputs.length < 2) {
                        System.out.println("Error. No file name given.");
                        break;
                    }
                    String fileName = inputs[1];
                    System.out.println("Opening: " + fileName);
                    File file = client1.getCachedFile(fileName);
                    if(file != null){
                        temp = file.getData();
                        client1.readFiles(temp);
                    } else {
                        temp = serverService.openFile(client1, fileName);
                        if (temp == null) {
                            System.out.println("There is no such file on the server!");
                        } else {
                            client1.readFiles(temp);
                        }
                    }
                    break;
                }

                // delete a file on the server. Besides ClientCallback client's sendCacheInvalidationEvent(), local
                // cache will be removed as well.
                case "rm": {
                    if (inputs.length < 2) {
                        System.out.println("Error. No file name given.");
                        break;
                    }
                    String fileName = inputs[1];
                    serverService.removeFile(fileName);
                    client1.removeFile(fileName);
                    break;
                }

                // create a file on the server and add to local cache.
                case "create": {
                    if (inputs.length < 2) {
                        System.out.println("Error. No file name given.");
                        break;
                    }
                    String fileName = inputs[1];
                    if (inputs.length < 3) {
                        System.out.println("Error. No data given.");
                        break;
                    }
                    String data = input.substring(input.indexOf(inputs[2]));
                    byte[] contents = data.getBytes(Charset.forName("UTF-8"));
                    serverService.createFile(client1, fileName, contents);
                    client1.createFile(fileName, contents);
                    break;
                }

                // Replace the byte array and update local cache as well.
                case "modify": {
                    if (inputs.length < 2) {
                        System.out.println("Error. No file name given.");
                        break;
                    }
                    String fileName = inputs[1];
                    if (inputs.length < 3) {
                        System.out.println("Error. No data given.");
                        break;
                    }
                    String data = input.substring(input.indexOf(inputs[2]));
                    byte[] newContents = data.getBytes(Charset.forName("UTF-8"));
                    System.out.println("Modifying: " + fileName);
                    serverService.editFile(fileName, newContents);
                    client1.modifyFile(fileName, newContents);
                    break;
                }

                // Replace the file name with a new file name
                case "rename": {
                    if (inputs.length < 2) {
                        System.out.println("Error. No file name given.");
                        break;
                    }
                    String fileName = inputs[1];
                    if (inputs.length < 3) {
                        System.out.println("Error. No new file name given.");
                        break;
                    }
                    String newName = inputs[2];
                    System.out.println("Renaming: " + fileName + " to: " + newName);
                    serverService.renameFile(fileName, newName);
                    client1.renameFile(fileName, newName);
                    break;
                }

                // case where no input is entered
                case "": {
                    System.out.println("Enter a command. Use 'help' for a list of possible commands.");
                    break;
                }

                //  exit system

                case "exit": {
                    serverService.unregister(client1);
                    keepGoing = false;
                    break;
                }

                default: {
                    System.out.println("Invalid command: [" + command + "]  Enter 'help' for help.");
                }
            }
        }

        System.exit(0);
    }
}