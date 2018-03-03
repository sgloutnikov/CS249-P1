package edu.sjsu.cs249.project1.client;

import edu.sjsu.cs249.project1.remote.ClientCallback;

import java.util.HashMap;

public class Client implements ClientCallback {

    private String clientId;
    private HashMap<String, File> fileMap;


    public Client(String clientId) {
        this.clientId = clientId;
        fileMap = new HashMap<>();
    }

    @Override
    public void ping() {

    }

    @Override
    public void invalidateCache(String file) {
        System.out.println("Received invalidate cache from server for: " + file);
        File cachedFile = fileMap.get(file);
        if (cachedFile != null) {
            cachedFile.setValid(false);
            fileMap.put(file, cachedFile);
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
