package edu.sjsu.cs249.project1.client;

import edu.sjsu.cs249.project1.remote.ClientCallback;

import java.util.HashMap;

public class Client implements ClientCallback {

    private String clientId;
    private HashMap<String, File> fileMap;


    public Client(String clientId) {
        this.clientId = clientId;
        fileMap = new HashMap<>();
        /**
        byte[] t = "hello".getBytes();
        File f = new File(t, true);
        fileMap.put("x", f);
         */
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

    public void readFiles(byte[] data) {

        File file=new File(data, true);
        System.out.println(new String(file.getData()));
        //fileMap.put(fileName, file);

        }
        // else this client will access server for its contents and then cache contents locally

    //Yaoyan
    public File getCachedFile(String fileName){
        if ( this.fileMap.containsKey(fileName)) {

            if (this.fileMap.get(fileName).getValidStatus()){
                return this.fileMap.get(fileName);
            }
            else
                return null;
        }
        else
            return null;


      }
    }

