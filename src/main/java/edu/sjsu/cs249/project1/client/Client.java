package edu.sjsu.cs249.project1.client;

import edu.sjsu.cs249.project1.remote.ClientCallback;

import java.util.HashMap;
import java.util.Set;

public class Client implements ClientCallback {

    private String clientId;
    private HashMap<String, File> fileMap;


    public Client(String clientId) {
        this.clientId = clientId;
        fileMap = new HashMap<>();
    }

    @Override
    public void invalidateCache(String file) {
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

    /** List all the files residing on the server and assume no authentication is required for any clients
     *  to operate on the files, or error message when no files are stored on the server.
     *
     *  @param set set of filenames returned by remote invocation of listFiles(ClientCallback c) method
     *
     */
    public void listFiles(Set<String> set) {
        String result = "";
        if (set.size() > 0) {
            for (String s : set) {
                result = result + s + "\n";
            }
            System.out.println(result);
        } else {
            System.out.println("There are no files on server to list.");
        }
    }

    /**
     *  Read/Open a file with given name: if available at local cache, fetch directly based on its validation status,
     *  or access server when not locally cached.
     *
     * @param data byte array return from RMI of readFiles() method
     */
    public void readFiles(byte[] data) {
        System.out.println(new String(data));

    }

    /**
     *
     *  Get cached file with given based on availability on local cache and its validation status.
     *
     * @param fileName
     * @return
     */
    public File getCachedFile(String fileName){
        if (this.fileMap.containsKey(fileName) && this.fileMap.get(fileName).getValidStatus()) {
            return this.fileMap.get(fileName);
        } else {
            return null;
        }
    }

    /**
     * Delete a file with given name and remove local cache if present.
     *
     *
     * @param fileName the name of file to be deleted
     */
    public void removeFile(String fileName){
        // removes and deletes file from Client cache if it exists;
        this.fileMap.remove(fileName);
        File file = this.fileMap.remove(fileName);
     }

    /**
     *
     * Create a file and add to local cache.
     *
     * @param fileName  new file's name
     * @param data      new file's contents in byte array format
     */
     public void createFile(String fileName, byte[] data){
         this.fileMap.put(fileName, new File(data, true));
     }

     public void modifyFile(String fileName, byte[] newData){
         this.fileMap.put(fileName, new File(newData, true));
     }

    public void renameFile(String fileName, String newName){
        File file = this.fileMap.remove(fileName);
        if (file != null) {
            this.fileMap.put(newName, file);
        }
    }
}

