package edu.sjsu.cs249.project1.client;

import edu.sjsu.cs249.project1.remote.ClientCallback;

import java.util.HashMap;
import java.util.Set;

/**
 * The Client class represents all the operations that a client can perform.
 */
public class Client implements ClientCallback {

    private String clientId;
    // Client cache map. Any files cached locally are stored here
    private HashMap<String, File> fileMap;


    public Client(String clientId) {
        this.clientId = clientId;
        fileMap = new HashMap<>();
    }

    /**
     * Invalidate local client cache for the filename provided.
     * @param file the name of the file to invalidate
     */
    @Override
    public void invalidateCache(String file) {
        System.out.println("Received cache invalidate from server for " + file);
        File cachedFile = fileMap.get(file);
        if (cachedFile != null) {
            cachedFile.setValid(false);
            fileMap.put(file, cachedFile);
        }
    }

    /**
     * Returns the ID of the client.
     * @return the client id
     */
    @Override
    public String getId() {
        return this.clientId;
    }

    /**
     * List all the files residing on the server and assume no authentication is required for any clients
     * to operate on the files, or error message when no files are stored on the server.
     *
     * @param set set of filenames returned by remote invocation of listFiles(ClientCallback c) method
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
     * Read/Open a file with given name: if available at local cache, fetch directly based on its validation status,
     * or access server when not locally cached.
     *
     * @param data byte array return from RMI of readFiles() method
     */
    public void readFiles(byte[] data) {
        System.out.println(new String(data));
    }

    /**
     * Get cached file with given based on availability on local cache and its validation status.
     *
     * @param fileName
     * @return
     */
    public File getCachedFile(String fileName) {
        if (this.fileMap.containsKey(fileName) && this.fileMap.get(fileName).getValidStatus()) {
            return this.fileMap.get(fileName);
        } else {
            return null;
        }
    }

    /**
     * Delete a file with given name and remove local cache if present.
     *
     * @param fileName the name of file to be deleted
     */
    public void removeFile(String fileName) {
        // removes and deletes file from Client cache if it exists;
        this.fileMap.remove(fileName);
        System.out.println("Deleted " + fileName);
    }

    /**
     * Create a file and add to local cache.
     *
     * @param fileName new file's name
     * @param data     new file's contents in byte array format
     */
    public void createFile(String fileName, byte[] data) {
        this.fileMap.put(fileName, new File(data, true));
        System.out.println("Created " + fileName);
    }

    /**
     * Modify the data of an existing file.
     * @param fileName the name of the file being modified
     * @param newData the new data
     */
    public void modifyFile(String fileName, byte[] newData) {
        this.fileMap.put(fileName, new File(newData, true));
        System.out.println("Modified " + fileName);
    }

    /**
     * Changes the name of a file to something new.
     * @param fileName the file being renamed
     * @param newName the new file name
     */
    public void renameFile(String fileName, String newName) {
        File file = this.fileMap.remove(fileName);
        if (file != null) {
            this.fileMap.put(newName, file);
        }
        System.out.println("Renamed " + fileName + " to " + newName);
    }
}

