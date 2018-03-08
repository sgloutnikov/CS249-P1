package edu.sjsu.cs249.project1.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI Client Callback Interface. <br/>
 * The server will use these methods to reach the client back when it needs to initiate the communication.
 */
public interface ClientCallback extends Remote {
    /**
     * Invalidate local client cache for the filename provided.
     *
     * @param fileName
     *            The name of the file to invalidate.
     */
    void invalidateCache(String file) throws RemoteException;

    /**
     * Returns the ID of this client.
     *
     * @return The ID of this client.
     */
    String getId() throws RemoteException;
}