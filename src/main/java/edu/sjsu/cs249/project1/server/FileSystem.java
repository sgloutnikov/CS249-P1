package edu.sjsu.cs249.project1.server;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This singleton class represents a file system supporting four primary operations. <br/>
 * 1. Create <br/>
 * 2. Delete <br/>
 * 3. Read <br/>
 * 4. Modify
 * 5. List
 * 6. Rename
 *
 * @author David Fisher
 */
public class FileSystem {
    private static final FileSystem INSTANCE = new FileSystem();

    private final Map<String, File> fileMap = new ConcurrentHashMap<>();

    /**
     * Singleton class, hide constructor by making it private.
     */
    private FileSystem() {

    }

    /**
     * Returns the singleton instance of FileSystem.
     *
     * @return The singleton instance of FileSystem.
     */
    public static FileSystem getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a new file with the given name and data in the file system. <br/>
     * Note: This method is synchronized at method level because we want to pipe all creations into a sequential order.
     * This is important because for each file, we need to check if the name already exists or not. Imagine the scenario
     * where two clients try to create a new file with the same name at the same time - this synchronization approach
     * will properly handle it by performing the creates one at a time.
     *
     * @param fileName
     *            The name to give to the new file.
     * @param data
     *            The data to store in the file.
     * @throws FileException
     *             If the provided name is null or if a file with the provided name already exists.
     */
    public synchronized void createFile(final String fileName, final byte[] data) throws FileException {
        if ((fileName != null) && (data != null)) {
            if (!this.fileMap.containsKey(fileName)) {
                this.fileMap.put(fileName, new File(fileName, data));
            } else {
                throw new FileException("File with name [" + fileName + "] already exists.");
            }
        } else {
            throw new FileException("Both a file name and data are required to create a new file.");
        }
    }

    /**
     * Deletes the file with the given name from the file system. <br/>
     * Note: This method synchronizes fileMap at object level. This is because we need to lock access to the file system
     * during the deletion process. This approach properly handles the below scenarios when they occur on the same file:
     * <br/>
     * 1. Two or more delete operations happening concurrently. First operation will succeed, subsequent operations will
     * fail. <br/>
     * 2. A create operation and a delete operation happening concurrently (in either order). <br/>
     * 3. A read/modify operation and a delete operation happening concurrently (in either order).
     *
     * @param fileName
     *            The name of the file to delete.
     * @throws FileException
     *             If the provided name is null, if a file with the provided name does not exist, or if the file was
     *             already deleted before the new delete operation could be performed.
     */
    public void deleteFile(final String fileName) throws FileException {
        if (fileName != null) {
            synchronized (this.fileMap) {
                /**
                 * First, remove the file from the "directory" (fileMap).
                 */
                final File file = this.fileMap.remove(fileName);
                if (file != null) {
                    /**
                     * Second, delete (nullify) the data. Note: delete uses an internal write lock, which handles
                     * scenario #3 listed above.
                     */
                    file.delete();

                    /**
                     * Since the file has been deleted, notify the clients to invalidate their cached files.
                     */
                    ClientCacheManager.getInstance().sendCacheInvalidationEventToAllClients(fileName);
                } else {
                    throw new FileException("No file with name [" + fileName + "] exists and therefore cannot be deleted.");
                }
            }
        } else {
            throw new FileException("A file name is required to delete a file.");
        }
    }

    /**
     * Reads the data of the file with the given name in the file system. <br/>
     * Note: This method does not require synchronization of the file system structure (fileMap), as the structure is
     * not changing.
     *
     * @param fileName
     *            The name of the file to read.
     * @return The contents of the file as a byte array.
     * @throws FileException
     *             If the provided name is null, if a file with the provided name does not exist, or if the file was
     *             deleted before it could be read.
     */
    public byte[] readFile(final String fileName) throws FileException {
        if (fileName != null) {
            /**
             * Note: We need to do an atomic "get" here (i.e., not do a containsKey(), then get()) to correctly handle
             * the synchronization logic in deleteFile().
             */
            final File file = this.fileMap.get(fileName);
            if (file != null) {
                /**
                 * Read the file. Note: read uses an internal read lock.
                 */
                return file.read();
            } else {
                throw new FileException("No file with name [" + fileName + "] exists and therefore cannot be read.");
            }
        } else {
            throw new FileException("A file name is required to read a file.");
        }
    }

    /**
     * Modifies the data of the file with the given name to the given value. <br/>
     * Note: This method does not require synchronization of the file system structure (fileMap), as the structure is
     * not changing.
     *
     * @param fileName
     *            The name of the file to modify.
     * @param newData
     *            The new data to store in the file.
     * @throws FileException
     *             If the provided name is null, if a file with the provided name does not exist, or if the file was
     *             deleted before it could be modified.
     */
    public void modifyFile(final String fileName, final byte[] newData) throws FileException {
        if ((fileName != null) && (newData != null)) {
            /**
             * Note: We need to do an atomic "get" here (i.e., not do a containsKey(), then get()) to correctly handle
             * the synchronization logic in deleteFile().
             */
            final File file = this.fileMap.get(fileName);
            if (file != null) {
                /**
                 * Write the new data to the file. Note: modify uses an internal write lock. <br/>
                 * If the file was changed, then we need to invalidate client caches.
                 */
                if (file.modify(newData)) {
                    /**
                     * Since the file has been modified, notify the clients to invalidate their cached files.
                     */
                    ClientCacheManager.getInstance().sendCacheInvalidationEventToAllClients(fileName);
                }
            } else {
                throw new FileException("No file with name [" + fileName + "] exists and therefore cannot be modified.");
            }
        } else {
            throw new FileException("Both a file name and data are required to modify an existing file.");
        }
    }

    /**
     *
     *  List all the files on the server and return in Set</fileNames>
     *
     *
     */
    public Set<String> listFiles() {
        return fileMap.keySet();
    }

    public Map<String, File> getFileMap() {
        return this.fileMap;
    }

    /**
     * Renames the file with the given name from the file system and a new name. <br/>
     * Note: This method is synchronized at method level because we want to pipe all renames into a sequential order.
     * This is important because for each file, we need to check if the name already exists or not. Imagine the scenario
     * where two clients try to rename same file at the same time - this synchronization approachwill properly handle
     * it by performing the renames one at a time.
     *
     * Note: This method synchronizes fileMap at object level. This is because we need to lock access to the file system
     * during the rename process. This approach properly handles the below scenarios when they occur on the same file:
     * <br/>
     * 1. Two or more rename operations happening concurrently. First operation will succeed, subsequent operations will
     * fail. <br/>
     * 2. A create operation and a delete operation happening concurrently (in either order). <br/>
     * 3. A read/modify operation and a delete operation happening concurrently (in either order).
     *
     *
     @param fileName
      * The name of the file to rename.
      *
     @param newName
      * The new name of the file.
      *
     @throws FileException
      * If the provided name is null, if a file with the provided name does not exist, or if the file was
      * already deleted before the new delete operation could be performed.
     */
    public synchronized void renameFile(final String fileName, final String newName) throws FileException {
        if ((fileName != null) && (newName != null)) {
            synchronized (this.fileMap) {
                /**
                 * First, remove the file from the "directory" (fileMap).
                 */
                if (!this.fileMap.containsKey(newName))
                {
                    final File file = this.fileMap.remove(fileName);
                    if (file != null) {
                        this.fileMap.put(newName, file);

                        /**
                         * Since the file has been renamed, notify the clients to invalidate their cached files.
                         */
                        ClientCacheManager.getInstance().sendCacheInvalidationEventToAllClients(fileName);
                    } else {
                        throw new FileException("No file with name" + fileName + " exists and therefore cannot be renamed.");
                    }

                } else {
                    throw new FileException("File with name" + newName + " already exists.");
                }
            }
        } else {
            throw new FileException("Both a file name and a new file name are required to rename a file.");
        }
    }
}