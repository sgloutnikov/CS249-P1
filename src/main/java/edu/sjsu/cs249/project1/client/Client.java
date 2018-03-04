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

    public HashMap<String,File> getFileMap() {
        return this.fileMap;
    }

    /** additional functions
     *
     *
     */
    public void listFiles() {

    }

    public void readFiles() {

        // if this file resides locally in the Client cache
        if (client1.getFileMap().containsKey(fileName)) {
            System.out.print("Contents of "+fileName+"is as follows:\n");
            System.out.println()
            return client1.getFileMap().get(fileName);

        }
        // else this client will access server for its contents and then cache contents locally
        else {
            FileSystem fileSystem=FileSystem.getInstance();
            //
            if (fileSystem.fileMap.contaisKey(fileName)) {
                String temp= fileSystem.fileMap.get(fileName);
                client1.fileMap.put(fileName, temp);
                return temp;
            }
        }
    }
