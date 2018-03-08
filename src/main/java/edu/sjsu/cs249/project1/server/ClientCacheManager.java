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
     * Structure: client ID -> Client wrapper object.
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
    public void registerCachedFile(final String clientId, final String fileName) throws CacheException {
        final Client client = clientId != null ? this.registeredClientsMap.get(clientId) : null;
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
     * Sends an invalidation event to all clients which have the given file cached. All clients need to invalidate their
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
                if (client.isActive()) {
                    /**
                     * Asynchronously send a cache invalidation event to each active client.
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
    }

    /**
     * Registers a client with the server. <br/>
     * Note: this method uses synchronization at the object level to handle the following scenarios: <br/>
     * 1. Multiple registrations happening concurrently with the same ID. <br/>
     * 2. A registration event occurring concurrently with an unregister event which happen to be using the same client
     * ID in both events.
     *
     * @param id
     *            The ID of the client.
     * @param client
     *            The client wrapper.
     * @throws CacheException
     *             If id or client are passed as null.
     */
    public void registerClient(final String id, final Client client) throws CacheException {
        if ((id != null) && (client != null)) {
            synchronized (this.registeredClientsMap) {
                if (!this.registeredClientsMap.containsKey(id)) {
                    this.registeredClientsMap.put(id, client);
                    System.out.println("Client registered with ID \"" + id + "\" and callback: \""
                            + client.getCallback() + "\".");
                } else {
                    throw new CacheException("Client is already registered with ID \"" + id + "\".");
                }
            }
        } else {
            throw new CacheException("ID and Client are required to register a client.");
        }
    }

    /**
     * Unregisters a client with the server. <br/>
     * Note: this method is synchronized at the method level to handle the scenario where multiple unregisterClient()
     * calls are made concurrently using the same ID.
     *
     * @param id
     *            The ID of the client to unregister.
     * @throws CacheException
     *             If id is null or is not currently registered with the server.
     */
    public synchronized void unregisterClient(final String id) throws CacheException {
        if (id != null) {
            if (this.registeredClientsMap.containsKey(id)) {
                /**
                 * Note: we both remove the client from the registration map as well as mark it as inactive. <br/>
                 * Marking as inactive is required so that clientCacheMap is essentially aware of the removal from
                 * registeredClientsMap.
                 */
                this.registeredClientsMap.remove(id).deactivateClient();
                System.out.println("Client Unregistered: " + id);
                System.out.println("Now tracking " + this.registeredClientsMap.size() + " clients.");
            } else {
                throw new CacheException("ID \"" + id + "\" is not currently registered with the server.");
            }
        } else {
            throw new CacheException("ID is required to unregister a client.");
        }
    }
}