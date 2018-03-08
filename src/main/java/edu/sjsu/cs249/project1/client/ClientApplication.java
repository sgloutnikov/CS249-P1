package edu.sjsu.cs249.project1.client;

import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

import edu.sjsu.cs249.project1.remote.FileServerService;
import edu.sjsu.cs249.project1.server.CacheException;
import edu.sjsu.cs249.project1.server.FileException;

/**
 * Entry point into the Client application. ClientApplication handles user input and interaction with the Client and
 * registering/interacting with the remote FileServer.
 */
public class ClientApplication {
    public static final String HELP = "Available commands:\n" + "ls - Lists the available files\n"
            + "create <filename> <contents> - create a file with the given name \n"
            + "open <filename> - Open a file. The file can be modified. Press return to save and exit\n"
            + "modify <filename> <new data> - edit a file with new contents and update local cache\n"
            + "rm <filename> - Delete a file\n" + "rename <filename> <new filename> - Rename a file\n"
            + "help - displays this message\n" + "exit - Exits the client\n";

    public static void main(final String[] args) {
        /**
         * Prompt for server host and client port.
         */
        try (Scanner scanner = new Scanner(System.in)) {
            String scannerInput;
            String host = "localhost";
            System.out.print("Enter the server location or default to [" + host + "]");
            scannerInput = scanner.nextLine();
            if (!scannerInput.isEmpty()) {
                host = scannerInput;
            }
            System.out.println("Server host: " + host);
            int port = 2000;
            System.out.print("Enter a valid/open client port number or default to [" + port + "]");
            scannerInput = scanner.nextLine();
            if (scannerInput.length() > 1) {
                port = Integer.parseInt(scannerInput);
            }
            System.out.println("Server port: " + port);

            /**
             * Setup Client and connect to Server via RMI.
             */
            final Client client1 = new Client("client-" + (System.currentTimeMillis() / 1000L));
            UnicastRemoteObject.exportObject(client1, port);
            final FileServerService serverService = (FileServerService) Naming
                    .lookup("rmi://" + host + ":5099/fileService");
            serverService.register(client1);
            System.out.println("+ Client Started +");

            /**
             * Interact with the user in a continuous loop. Break when the user types command "exit".
             */
            boolean keepGoing = true;
            while (keepGoing) {
                System.out.print("client:~$ ");

                final String input = scanner.nextLine();
                final String[] inputs = input.split(" ");
                final String command = inputs[0].toLowerCase();

                System.out.println();

                switch (command) {
                case "help": {
                    /**
                     * Print out all supported commands.
                     */
                    System.out.println(HELP);
                    break;
                }

                case "ls": {
                    try {
                        /**
                         * Print the names of all files currently being hosted on the server.
                         */
                        client1.printFileNames(serverService.listFiles());
                    } catch (final RemoteException e) {
                        printRelevantExceptionInfo(e);
                    }
                    break;
                }

                case "open": {
                    if (inputs.length < 2) {
                        System.out.println("Error. No file name given.");
                        break;
                    }
                    final String fileName = inputs[1];
                    System.out.println("Opening: " + fileName);
                    /**
                     * Read the file with the given name. <br/>
                     * For demonstration purposes, we are simply printing to console.
                     */
                    final File cachedFile = client1.getCachedFile(fileName);
                    if (cachedFile != null) {
                        /**
                         * If the file is already cached and is still considered valid, print the cached copy of the
                         * file.
                         */
                        client1.printFile(cachedFile.getData());
                    } else {
                        /**
                         * Else the cache does not contain the file. In this case, we need to interact with the server.
                         * <br/>
                         * 1. Pull the file from the server. <br/>
                         * 2. Cache the file locally for future reuse. <br/>
                         * 3. Print the result received from the server.
                         */
                        try {
                            final byte[] serverFileContents = serverService.openFile(client1, fileName);
                            client1.cacheFile(fileName, serverFileContents);
                            client1.printFile(serverFileContents);
                        } catch (final RemoteException e) {
                            printRelevantExceptionInfo(e);
                        }
                    }
                    break;
                }

                case "rm": {
                    if (inputs.length < 2) {
                        System.out.println("Error. No file name given.");
                        break;
                    }
                    final String fileName = inputs[1];
                    try {
                        /**
                         * Send remove request to the server. <br/>
                         * Note 1: it is possible to remove files on the server which are not cached locally. <br/>
                         * Note 2: if this client currently has the specified file cached, then it will be invalidated
                         * by the server.
                         */
                        serverService.removeFile(fileName);
                    } catch (final RemoteException e) {
                        printRelevantExceptionInfo(e);
                    }
                    break;
                }

                case "create": {
                    if (inputs.length < 2) {
                        System.out.println("Error. No file name given.");
                        break;
                    }
                    final String fileName = inputs[1];
                    if (inputs.length < 3) {
                        System.out.println("Error. No data given.");
                        break;
                    }
                    final String data = input.substring(input.indexOf(inputs[2]));
                    final byte[] contents = data.getBytes(Charset.forName("UTF-8"));
                    try {
                        /**
                         * Send create request to the server. <br/>
                         * If server request completes without exception, then cache the created file locally.
                         */
                        serverService.createFile(client1, fileName, contents);
                        client1.cacheFile(fileName, contents);
                    } catch (final RemoteException e) {
                        printRelevantExceptionInfo(e);
                    }
                    break;
                }

                case "modify": {
                    if (inputs.length < 2) {
                        System.out.println("Error. No file name given.");
                        break;
                    }
                    final String fileName = inputs[1];
                    if (inputs.length < 3) {
                        System.out.println("Error. No data given.");
                        break;
                    }
                    final String data = input.substring(input.indexOf(inputs[2]));
                    final byte[] newContents = data.getBytes(Charset.forName("UTF-8"));
                    System.out.println("Modifying: " + fileName);
                    try {
                        /**
                         * Send edit request to the server. <br/>
                         * Note 1: it is possible to edit files on the server which are not cached locally. <br/>
                         * Note 2: if this client currently has the specified file cached, then it will be invalidated
                         * by the server.
                         */
                        serverService.editFile(fileName, newContents);
                    } catch (final RemoteException e) {
                        printRelevantExceptionInfo(e);
                    }
                    break;
                }

                case "rename": {
                    if (inputs.length < 2) {
                        System.out.println("Error. No file name given.");
                        break;
                    }
                    final String fileName = inputs[1];
                    if (inputs.length < 3) {
                        System.out.println("Error. No new file name given.");
                        break;
                    }
                    final String newName = inputs[2];
                    System.out.println("Renaming \"" + fileName + "\" to: \"" + newName + "\".");
                    try {
                        /**
                         * Send rename request to the server. <br/>
                         * Note 1: it is possible to rename files on the server which are not cached locally. <br/>
                         * Note 2: if this client currently has the specified file cached, then it will be invalidated
                         * by the server.
                         */
                        serverService.renameFile(fileName, newName);
                    } catch (final RemoteException e) {
                        printRelevantExceptionInfo(e);
                    }
                    break;
                }

                case "": {
                    /**
                     * No input was entered. Print informational message.
                     */
                    System.out.println("Enter a command. Use 'help' for a list of possible commands.");
                    break;
                }

                case "exit": {
                    /**
                     * Unregister the client, break the loop, and exit.
                     */
                    try {
                        serverService.unregister(client1);
                    } catch (final RemoteException e) {
                        printRelevantExceptionInfo(e);
                    }
                    keepGoing = false;
                    break;
                }

                default: {
                    System.out.println("Invalid command: [" + command + "]  Enter 'help' for help.");
                }
                }
            }
        } catch (RemoteException | MalformedURLException | NotBoundException e) {
            e.printStackTrace();
            System.out.println("Critical exception occurred - forcing program termination.");
        } finally {
            System.out.println("Exiting...");
            System.exit(0);
        }
    }

    /**
     * Unwraps a RemoteException to determine if it was caused by either CacheException or FileException. If yes, print
     * only that part of the stack trace. Else, print the whole stack trace.
     *
     * @param e
     *            A RemoteException caused by an RMI fault.
     */
    private static void printRelevantExceptionInfo(final RemoteException e) {
        if (e != null) {
            final Throwable cause = e.getCause();
            if ((cause != null) && (cause instanceof RemoteException)) {
                final Throwable nestedCause = cause.getCause();
                if ((nestedCause != null)
                        && ((nestedCause instanceof CacheException) || (nestedCause instanceof FileException))) {
                    System.err.println("\n" + cause.getMessage() + " - " + nestedCause.getMessage());
                } else {
                    System.err.println();
                    e.printStackTrace();
                }
            } else {
                System.err.println();
                e.printStackTrace();
            }
        }
    }
}