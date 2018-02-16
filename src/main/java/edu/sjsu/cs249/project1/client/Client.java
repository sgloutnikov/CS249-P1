package edu.sjsu.cs249.project1.client;

import edu.sjsu.cs249.project1.remote.ClientCallback;
import edu.sjsu.cs249.project1.server.File;

import java.util.HashMap;

public class Client implements ClientCallback {

    private String clientId;
    private HashMap<String, Boolean> cacheValidationMap;
    private HashMap<String, File> cacheMap;


    public Client(String clientId) {
        this.clientId = clientId;
        cacheValidationMap = new HashMap<>();
        cacheMap = new HashMap<>();
    }

    @Override
    public void ping() {

    }

    @Override
    public void invalidateCache(String file) {
        System.out.println("Received invalidate cache from server for: " + file);
        if (cacheValidationMap.get(file) != null) {
            cacheValidationMap.put(file, false);
        }
    }

    @Override
    public String getId() {
        return this.clientId;
    }

    public String getClientId() {
        return clientId;
    }
}
