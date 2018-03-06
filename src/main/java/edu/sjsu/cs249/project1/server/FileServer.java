package edu.sjsu.cs249.project1.server;

import edu.sjsu.cs249.project1.remote.ClientCallback;
import edu.sjsu.cs249.project1.remote.FileServerService;
import java.util.Set;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * The FileServer is the entry point into the Server operations from a Client.
 * Implements the RMI Interface
 */
public class FileServer extends UnicastRemoteObject implements FileServerService {

    public FileServer() throws RemoteException {
        super();
        String absolutePath = "filesystem/";
        Path basePath = Paths.get(absolutePath);

        // Check if filesystem path exists, create it does not
        if(!Files.exists(basePath)) {
            try {
                Files.createDirectory(basePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Load existing files on the file system.
        System.out.println("Loading existing files on the file system.");

        try {
            Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    String fileName = file.toFile().getName();
                    System.out.println("Reading " + fileName);
                    try {
                        FileSystem.getInstance().createFile(fileName, Files.readAllBytes(file));
                    } catch (FileException e) {
                        e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Register a client with the server. The server needs to track all the clients and the files
     * they have cached.
     * @param client the client callback
     * @throws RemoteException
     */
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

    /**
     * Unregister the client from the server when the client exits. Should be called before the client exits
     * so that the server no longer tracks it.
     * @param client the client callback in this case used to identify the client
     * @throws RemoteException
     */
    @Override
    public void unregister(ClientCallback client) throws RemoteException {
        try {
            ClientCacheManager.getInstance().unregisterClient(client.getId());
        } catch (CacheException e) {
            e.printStackTrace();
        }
    }

    @Override
    /**
     *  List all available files on the server.
     */
    public Set<String> listFiles() throws RemoteException{
        return FileSystem.getInstance().listFiles();
    }

    /**
     * Creation of a file:
     * if already exists, prompt for a different name;
     * if not, create File and Client from server's view and
     * register with Singleton FileSystem and ClientCashManager
     * @param client the client creating the file
     * @param fileName file name of the file
     * @param data data of the file
     * @throws RemoteException
     */
    public void createFile(ClientCallback client, String fileName, byte[] data) throws RemoteException{
        FileSystem fileSystem = FileSystem.getInstance();
        try {
            fileSystem.createFile(fileName, data);
        } catch (FileException e) {
            e.printStackTrace();
        }

        // Track client cache
        String clientId = client.getId();
        Client serverClient = ClientCacheManager.getInstance().getClient(clientId);
        try {
            ClientCacheManager.getInstance().registerCachedFile(serverClient, fileName);
        } catch (CacheException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read a file:
     *  if not existing, print error message ;
     *  if existing, search Singleton FileSystem and print file data in byte[] and
     *  register in ClientCacheManager
     * @param client the client opening the file
     * @param fileName file name of the file
     * @return byte array of the data
     * @throws RemoteException
     */
    public byte[] openFile(ClientCallback client, String fileName) throws RemoteException{
        byte[] temp = null;
        FileSystem fileSystem=FileSystem.getInstance();

        try {
            temp = fileSystem.readFile(fileName);
        } catch (FileException e) {
            System.out.println(e.getMessage());
        }

        // Track client cache
        if (temp != null) {
            String clientId = client.getId();
            Client serverClient = ClientCacheManager.getInstance().getClient(clientId);
            try {
                ClientCacheManager.getInstance().registerCachedFile(serverClient, fileName);
            } catch (CacheException e) {
                e.printStackTrace();
            }
        }

        return temp;
    }

    /**
     * Delete the specified file. FileSystem will handle notifying clients that have this file
     * cached that is no longr valid.
     * @param fileName name of the file being deleted
     * @throws RemoteException
     */
    public void removeFile(String fileName) throws RemoteException{
        FileSystem fileSystem = FileSystem.getInstance();
        try {
            fileSystem.deleteFile(fileName);
        } catch (FileException e) {
            System.out.println(e.getMessage());
        }

    }


    /**
     * Change a file's contents. The FileSystem will notify  all the relevant clients.
     * @param fileName name of the file being modified
     * @param newData new data for the specified file
     * @throws RemoteException
     */
    public void editFile(String fileName, byte[] newData) throws RemoteException{
        FileSystem fileSystem=FileSystem.getInstance();
        try {
            fileSystem.modifyFile(fileName, newData);
        } catch (FileException e) {
            e.printStackTrace();
        }

    }

    /**
     * Renames the file to a new name.
     * @param fileName old file name
     * @param newFileName new file name
     * @throws RemoteException
     */
    public void renameFile(String fileName, String newFileName) throws RemoteException{
        FileSystem fileSystem=FileSystem.getInstance();
        try {
            fileSystem.renameFile(fileName, newFileName);
        } catch (FileException e) {
            e.printStackTrace();
        }
    }
}
