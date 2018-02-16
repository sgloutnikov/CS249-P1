package edu.sjsu.cs249.project1.server;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This singleton class manages the relation of files to clients. It has two purposes: <br/>
 * 1. Keeps track of which clients have which files cached locally. <br/>
 * 2. Sends out cache invalidation notices to clients when a file changes in the file system.
 *
 * @author David Fisher
 */
public class ClientCacheManager {
    private static final ClientCacheManager INSTANCE = new ClientCacheManager();

    /**
     * Structure: file name -> set of clients who have the file cached.
     */
    private final Map<String, Set<Client>> clientCacheMap = new ConcurrentHashMap<>();

    /**
     * Structure: client ID -> client. Anyone registered with the server is tracked here.
     *
     */
    private final Map<String, Client> registeredClientsMap = new ConcurrentHashMap<>();

    /**
     * Singleton class, hide constructor by making it private.
     */
    private ClientCacheManager() {

    }

    /**
     * Returns the singleton instance of ClientCacheManager.
     *
     * @return The singleton instance of ClientCacheManager.
     */
    public static ClientCacheManager getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a client under the given file name. This method should be called whenever a client caches a file for
     * the first time. <br/>
     * Note: this method requires synchronization on clientCacheMap because we are directly manipulating it and may
     * impact sendCacheInvalidationEvent().
     *
     * @param client
     *            The client which is caching the given file.
     * @param fileName
     *            The name of the file which the client is caching.
     * @throws CacheException
     *             If client or fileName are passed as null.
     */
    public void registerCachedFile(final Client client, final String fileName) throws CacheException {
        if ((client != null) && (fileName != null)) {
            synchronized (this.clientCacheMap) {
                /**
                 * Retrieve the set of all clients who have the given file name cached.
                 */
                Set<Client> clients = this.clientCacheMap.get(fileName);
                if (clients == null) {
                    /**
                     * Instantiate the set if null.
                     */
                    clients = new LinkedHashSet<>();
                    this.clientCacheMap.put(fileName, clients);
                }

                /**
                 * Add the new Client. Note: it doesn't matter if the client is already known to have cached the file -
                 * since this is a set, it eliminates duplicate values. However, because LinkedHashSet determines
                 * equivalency by calling the equals() method on an object, we need to define our own implementation of
                 * Client.equals().
                 */
                clients.add(client);
            }
        } else {
            throw new CacheException("Client and file name are required to register a cache event.");
        }
    }

    /**
     * Sends an invalidation event to all clients which have the given file cached. All clients need to DELETE their
     * cached copy.
     *
     * @param fileName
     *            The name of the file which has been changed or deleted, and hence requires a cache invalidation event
     *            to be triggered.
     */
    public void sendCacheInvalidationEventToAllClients(final String fileName) {
        final Set<Client> clients = this.clientCacheMap.get(fileName);
        if (clients != null) {
            for (final Client client : clients) {
                /**
                 * Asynchronously send a cache invalidation event to each client.
                 */
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        client.sendCacheInvalidationEvent(fileName);
                    }
                }).start();
            }
        }
    }

    public void registerClient(String id, Client client) throws CacheException {
        if (id != null && client != null) {
            this.registeredClientsMap.put(id, client);
            System.out.println("Client Registered: " + id + " with callback: " + client.getCallback());
        } else {
            throw new CacheException("ID and Client are required to register a client.");
        }
    }

    public void unregisterClient(String id) throws CacheException {
        if (id != null) {
            this.registeredClientsMap.remove(id);
            System.out.println(registeredClientsMap.size());
        } else {
            throw new CacheException("ID is required to unregister a client.");
        }
    }

}