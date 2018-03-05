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
 * Implements the RMI Interface
 */
public class FileServer extends UnicastRemoteObject implements FileServerService {

    public FileServer() throws RemoteException {
        super();
        // Load existing files on the file system.
        System.out.println("Loading existing files on the file system.");
        String absolutePath = "filesystem/";
        try {
            Files.walkFileTree(Paths.get(absolutePath), new SimpleFileVisitor<Path>() {
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

    @Override
    /**
     *  List all the files on the server.
     *
     *
     */
    public Set<String> listFiles(ClientCallback client) throws RemoteException{
        Set result = null;
        FileSystem fileSystem = FileSystem.getInstance();
        result = fileSystem.listFiles();
        return result;
    }

    /**
     * Creation of a file:
     * if already exists, prompt for a different name;
     * if not, create File and Client from server's view and
     * register with Singleton FileSystem and ClientCashManager
     *
     */

    public void createFiles(ClientCallback client, String fileName, byte[] data) throws RemoteException{
        FileSystem fileSystem=FileSystem.getInstance();
        try {
            fileSystem.createFile(fileName, data);
            File file=new File(fileName, data);
        } catch (FileException e) {
            e.printStackTrace();
        }

        String clientId = client.getId();
        Client c = new Client(clientId, client);
        try {
            ClientCacheManager.getInstance().registerCachedFile(c, fileName);
        } catch (CacheException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Read a file:
     *  if not existing, print error message ;
     *  if existing, search Singleton FileSystem and print file data in byte[] and
     *  register in ClientCacheManager
     */
    public byte[] openFiles(ClientCallback client, String fileName) throws RemoteException{
        byte[] temp = null;
        FileSystem fileSystem=FileSystem.getInstance();

        try {
            temp = fileSystem.readFile(fileName);
        } catch (FileException e) {
            System.out.println(e.getMessage());
        }

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

    /** Delete a file:
     *  if not existing, print error message;
     *  if existing, remove from Singleton FileSystem and ClientCashManager and
     *  notify all the relevant clients of this deletion.
     *
     *
     *
     *
     */
    public void removeFiles(ClientCallback client, String fileName) throws RemoteException{
        FileSystem fileSystem=FileSystem.getInstance();
        try {
            fileSystem.deleteFile(fileName);
        } catch (FileException e) {
            e.printStackTrace();
        }

    }

    /** change file's contents and update all the relevant clients
     *
     *
     */
    public void editFiles(ClientCallback client, String fileName, byte[] newData) throws RemoteException{
        FileSystem fileSystem=FileSystem.getInstance();
        try {
            fileSystem.modifyFile(fileName, newData);
        } catch (FileException e) {
            e.printStackTrace();
        }

    }

    public void renameFile(ClientCallback client, String fileName, String newFileName) throws RemoteException{
        FileSystem fileSystem=FileSystem.getInstance();
        try {
            fileSystem.renameFile(fileName, newFileName);
        } catch (FileException e) {
            e.printStackTrace();
        }
    }
}
