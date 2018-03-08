package edu.sjsu.cs249.project1.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.sjsu.cs249.project1.remote.ClientCallback;

/**
 * This class represents all the operations that a client can perform.
 */
public class Client implements ClientCallback {
    private final String clientId;

    /**
     * Files cached by the client are stored here.
     */
    private final Map<String, File> fileMap = new HashMap<>();

    public Client(final String clientId) {
        this.clientId = clientId;
    }

    /**
     * Invalidate local client cache for the filename provided.
     *
     * @param fileName
     *            The name of the file to invalidate.
     */
    @Override
    public void invalidateCache(final String fileName) {
        final File cachedFile = this.fileMap.get(fileName);
        if ((cachedFile != null) && cachedFile.isValid()) {
            /**
             * Mark cached file as invalid. <br/>
             * Note: we could have also just removed it from the cache.
             */
            cachedFile.setValid(false);
            System.out.println("Cached version of \"" + fileName + "\" was invalidated by the server.");
        }
    }

    /**
     * Returns the ID of this client.
     *
     * @return The ID of this client.
     */
    @Override
    public String getId() {
        return this.clientId;
    }

    /**
     * Lists all the files residing on the server, or an informational message when no files are found. <br/>
     * Assumption: no authentication is required for any clients to operate on the files.
     *
     * @param fileNames
     *            The set of filenames returned by remote invocation of listFiles(ClientCallback c) method.
     */
    public void printFileNames(final Set<String> fileNames) {
        if ((fileNames != null) && !fileNames.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            boolean isFirst = true;
            for (final String s : fileNames) {
                if (!isFirst) {
                    sb.append("\n");
                } else {
                    isFirst = false;
                }
                sb.append(s);
            }
            System.out.println(sb.toString());
        } else {
            System.out.println("There are no files on server to list.");
        }
    }

    /**
     * Converts data to a String and prints it to the console.
     *
     * @param data
     *            A byte array returned from RMI of readFiles() method.
     */
    public void printFile(final byte[] data) {
        System.out.println(new String(data));
    }

    /**
     * Checks the local file cache for the provided fileName and returns such file if it exists and is valid.
     *
     * @param fileName
     *            The name of the file.
     * @return If the local cache contains the file, and if the cached file is valid, then the cached file is returned.
     *         Otherwise, null is returned.
     */
    public File getCachedFile(final String fileName) {
        final File file = this.fileMap.get(fileName);
        if ((file != null) && file.isValid()) {
            return file;
        } else {
            return null;
        }
    }

    /**
     * Caches a file in the local client cache. <br/>
     * If the cache already contains a file with fileName, then the existing file will be updated with data and marked
     * as valid. <br/>
     * If the cache does not contain a file with fileName, then a new File object will be created and stored in the
     * cache using the provided fileName and data.
     *
     * @param fileName
     * @param data
     */
    public void cacheFile(final String fileName, final byte[] data) {
        final File file = this.fileMap.get(fileName);
        if (file != null) {
            file.setData(data);
            file.setValid(true);
            System.out.println("Updated cached contents of \"" + fileName + "\".");
        } else {
            this.fileMap.put(fileName, new File(data));
            System.out.println("Cached \"" + fileName + "\".");
        }
    }
}