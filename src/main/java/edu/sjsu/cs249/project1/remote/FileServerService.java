package edu.sjsu.cs249.project1.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

/**
 * RMI Server Interface. <br/>
 * The clients will use these methods to communicate with the server. <br/>
 * All RMI methods are defined here.
 */
public interface FileServerService extends Remote {
    /**
     * Register a client with the server. <br/>
     * The server needs to track all the clients and the files they have cached.
     *
     * @param client
     *            The client callback.
     * @throws RemoteException
     *             If any RMI error occurs.
     */
    void register(ClientCallback client) throws RemoteException;

    /**
     * Unregister the client from the server when the client exits. <br/>
     * Note: this should be called before the client exits so that the server no longer tracks it.
     *
     * @param client
     *            The client callback used to uniquely identify the client.
     * @throws RemoteException
     *             If any RMI error occurs.
     */
    void unregister(ClientCallback client) throws RemoteException;

    /**
     * Returns a set composed of the names of all files currently being hosted on the file system.
     *
     * @return A set of all files currently being hosted on the file system.
     * @throws RemoteException
     *             If any RMI error occurs.
     */
    Set<String> listFiles() throws RemoteException;

    /**
     * Create a new file on the server. <br/>
     * Process of file creation: <br/>
     * 1. If the name already exists, prompt for a different name. <br/>
     * 2. If the name does not already exist, create the File and Client from server's view and register with Singleton
     * FileSystem and ClientCacheManager.
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
    void createFile(ClientCallback client, String fileName, byte[] data) throws RemoteException;

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
    byte[] openFile(ClientCallback client, String fileName) throws RemoteException;

    /**
     * Delete a file from the server. <br/>
     * Note: FileSystem will invalidate relevant client caches.
     *
     * @param fileName
     *            The name of the file to delete.
     * @throws RemoteException
     *             If any RMI error occurs.
     */
    void removeFile(String fileName) throws RemoteException;

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
    void editFile(String fileName, byte[] newData) throws RemoteException;

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
    void renameFile(String fileName, String newFileName) throws RemoteException;
}