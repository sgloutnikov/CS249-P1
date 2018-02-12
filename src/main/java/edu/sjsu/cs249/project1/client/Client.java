package edu.sjsu.cs249.project1.client;

import edu.sjsu.cs249.project1.remote.ClientCallback;

public class Client implements ClientCallback {

    private String clientId;

    public Client(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public void ping() {

    }

    @Override
    public void invalidateCache(String file) {

    }

    @Override
    public String getId() {
        return this.clientId;
    }

    public String getClientId() {
        return clientId;
    }
}
