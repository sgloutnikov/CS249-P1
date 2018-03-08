package edu.sjsu.cs249.project1.server;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

import edu.sjsu.cs249.project1.remote.ClientCallback;
import edu.sjsu.cs249.project1.remote.FileServerService;

/**
 * The FileServer is the entry point into the Server operations from a Client. <br/>
 * Implements the RMI Interface.
 */
public class FileServer extends UnicastRemoteObject implements FileServerService {
    private static final long serialVersionUID = -6819647968679623776L;
    private static final String EXCEPTION_MESSAGE = "Operation fault";

    public FileServer() throws RemoteException {
        super();
        final Path basePath = Paths.get("filesystem/");

        /**
         * Check if filesystem path exists. If it does not exist, then create it.
         */
        if (!Files.exists(basePath)) {
            try {
                Files.createDirectory(basePath);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Load existing files on the file system.
         */
        System.out.println("Loading existing files on the file system...");

        try {
            Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
                    final String fileName = file.toFile().getName();
                    System.out.println("Reading " + fileName);
                    try {
                        FileSystem.getInstance().createFile(fileName, Files.readAllBytes(file));
                    } catch (final FileException e) {
                        e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Register a client with the server. <br/>
     * The server needs to track all the clients and the files they have cached.
     *
     * @param client
     *            The client callback.
     * @throws RemoteException
     *             If any RMI error occurs.
     */
    @Override
    public void register(final ClientCallback client) throws RemoteException {
        try {
            ClientCacheManager.getInstance().registerClient(client.getId(), new Client(client.getId(), client));
        } catch (final CacheException e) {
            e.printStackTrace();
            throw new RemoteException(EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Unregister the client from the server when the client exits. <br/>
     * Note: this should be called before the client exits so that the server no longer tracks it.
     *
     * @param client
     *            The client callback used to uniquely identify the client.
     * @throws RemoteException
     *             If any RMI error occurs.
     */
    @Override
    public void unregister(final ClientCallback client) throws RemoteException {
        try {
            ClientCacheManager.getInstance().unregisterClient(client.getId());
        } catch (final CacheException e) {
            e.printStackTrace();
            throw new RemoteException(EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Returns a set composed of the names of all files currently being hosted on the file system.
     *
     * @return A set of all files currently being hosted on the file system.
     * @throws RemoteException
     *             If any RMI error occurs.
     */
    @Override
    public Set<String> listFiles() throws RemoteException {
        return FileSystem.getInstance().getFileNames();
    }

    /**
     * Create a new file on the server. <br/>
     * Process of file creation: <br/>
     * 1. If the name already exists, prompt for a different name. <br/>
     * 2. If the name does not already exist, create the file on the server. <br/>
     * 3. Register the client in ClientCacheManager.
     *
     * @param client
     *            The client creating the file.
     * @param fileName
     *            The name of the file to create.
     * @param data
     *            The contents of the file to create.
     * @throws RemoteException
     *             If any RMI error occurs.
     */
    @Override
    public void createFile(final ClientCallback client, final String fileName, final byte[] data) throws RemoteException {
        final FileSystem fileSystem = FileSystem.getInstance();
        try {
            /**
             * Create the file.
             */
            fileSystem.createFile(fileName, data);

            /**
             * Start tracking the cache of this client.
             */
            ClientCacheManager.getInstance().registerCachedFile(client.getId(), fileName);
        } catch (final FileException | CacheException e) {
            e.printStackTrace();
            throw new RemoteException(EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Read a file from the server. <br/>
     * Process of file read: <br/>
     * 1. If the file does not exist, print an error message. <br/>
     * 2. If the file does exist, search Singleton FileSystem and return file data as a byte array. 3. Register the
     * client in ClientCacheManager.
     *
     * @param client
     *            The client opening the file.
     * @param fileName
     *            The name of the file to read.
     * @return The contents of the file as a byte array.
     * @throws RemoteException
     *             If any RMI error occurs.
     */
    @Override
    public byte[] openFile(final ClientCallback client, final String fileName) throws RemoteException {
        try {
            /**
             * Read the file.
             */
            final byte[] result = FileSystem.getInstance().readFile(fileName);

            /**
             * Start tracking the cache of this client.
             */
            ClientCacheManager.getInstance().registerCachedFile(client.getId(), fileName);

            return result;
        } catch (final FileException | CacheException e) {
            e.printStackTrace();
            throw new RemoteException(EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Delete a file from the server. <br/>
     * Note: FileSystem will invalidate relevant client caches.
     *
     * @param fileName
     *            The name of the file to delete.
     * @throws RemoteException
     *             If any RMI error occurs.
     */
    @Override
    public void removeFile(final String fileName) throws RemoteException {
        try {
            /**
             * Delete the file.
             */
            FileSystem.getInstance().deleteFile(fileName);
        } catch (final FileException e) {
            e.printStackTrace();
            throw new RemoteException(EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Edit the contents of a file on the server. <br/>
     * Note: FileSystem will invalidate relevant client caches.
     *
     * @param fileName
     *            The name of the file to edit.
     * @param newData
     *            The new contents of the file.
     * @throws RemoteException
     *             If any RMI error occurs.
     */
    @Override
    public void editFile(final String fileName, final byte[] newData) throws RemoteException {
        try {
            /**
             * Modify the file.
             */
            FileSystem.getInstance().modifyFile(fileName, newData);
        } catch (final FileException e) {
            e.printStackTrace();
            throw new RemoteException(EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Rename a file on the server. <br/>
     * Note: FileSystem will invalidate relevant client caches.
     *
     * @param fileName
     *            The current name of the file to rename.
     * @param newFileName
     *            The new name of the file.
     * @throws RemoteException
     *             If any RMI error occurs.
     */
    @Override
    public void renameFile(final String fileName, final String newFileName) throws RemoteException {
        try {
            /**
             * Rename the file.
             */
            FileSystem.getInstance().renameFile(fileName, newFileName);
        } catch (final FileException e) {
            e.printStackTrace();
            throw new RemoteException(EXCEPTION_MESSAGE, e);
        }
    }
}