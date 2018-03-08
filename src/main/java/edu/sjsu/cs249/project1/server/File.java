package edu.sjsu.cs249.project1.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class represents a file in a file system. Data is represented as a byte array.
 *
 * @author David Fisher
 */
public class File {
    /**
     * Note: FILE_PATH_PREFIX is relative to your workspace installation directory.
     */
    private static final String FILE_PATH_PREFIX = "filesystem/";
    private final String absolutePath;
    private byte[] cachedFile;
    private boolean isActive;
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    /**
     * Instantiates a new File with the given data.
     *
     * @param name
     *            The name of this file.
     * @param data
     *            The initial data to store in this file.
     * @throws FileException
     *             If any problem is encountered while creating the file on disk.
     */
    public File(final String name, final byte[] data) throws FileException {
        this.absolutePath = FILE_PATH_PREFIX + name;
        try (FileOutputStream outputStream = new FileOutputStream(this.absolutePath)) {
            outputStream.write(data);
        } catch (IOException | SecurityException e) {
            throw new FileException("Error occurred while creating file.", e);
        }
        this.cachedFile = Arrays.copyOf(data, data.length);
        this.isActive = true;
    }

    /**
     * Returns the contents of this file as a byte array. <br/>
     * Lock used: <i>Read</i>
     *
     * @return The contents of this file.
     * @throws FileException
     *             If this file was deleted before it could be read.
     */
    public byte[] read() throws FileException {
        this.lock.readLock().lock();
        try {
            if (this.isActive) {
                /**
                 * Note: no need to read from the physical file since we have it locally cached in memory.
                 */
                return this.cachedFile;
            } else {
                throw new FileException("This file no longer exists.");
            }
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /**
     * Modifies the data of this file to some new value. <br/>
     * Lock used: <i>Write</i>
     *
     * @param newData
     *            The new contents of the file.
     * @return True if the data was changed, or false if the existing data was equivalent to the new data (i.e., nothing
     *         to update).
     * @throws FileException
     *             If this file was deleted before it could be modified.
     */
    public boolean modify(final byte[] newData) throws FileException {
        this.lock.writeLock().lock();
        try (FileOutputStream outputStream = new FileOutputStream(this.absolutePath)) {
            if (this.isActive) {
                if (!Arrays.equals(this.cachedFile, newData)) {
                    /**
                     * Write to disk.
                     */
                    outputStream.write(newData);

                    /**
                     * Update the cached copy. <br/>
                     * Note: Must be done after writing to disk in case writing throws an exception.
                     */
                    this.cachedFile = Arrays.copyOf(newData, newData.length);
                    return true;
                } else {
                    return false;
                }
            } else {
                throw new FileException("This file no longer exists.");
            }
        } catch (IOException | SecurityException e) {
            throw new FileException(e);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Deletes this file. <br/>
     * Lock used: <i>Write</i> <br/>
     * Note: This object will still exist, but it will be marked as inactive and the file will be deleted.
     *
     * @throws FileException
     *             If this file was already deleted by another process.
     */
    public void delete() throws FileException {
        this.lock.writeLock().lock();
        try {
            if (this.isActive) {
                Files.delete(Paths.get(this.absolutePath));
                this.cachedFile = null;
                this.isActive = false;
            } else {
                throw new FileException("This file no longer exists.");
            }
        } catch (IOException | SecurityException e) {
            throw new FileException(e);
        } finally {
            this.lock.writeLock().unlock();
        }
    }
}