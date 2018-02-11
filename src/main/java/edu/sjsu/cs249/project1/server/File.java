package edu.sjsu.cs249.project1.server;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class represents a file in a file system. Data is represented as a byte array.
 *
 * @author David Fisher
 */
public class File {
    private byte[] data;
    private boolean isActive;
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    /**
     * Instantiates a new File with the given data.
     *
     * @param data
     *            The initial data to store in this file.
     */
    public File(final byte[] data) {
        this.data = data;
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
                return this.data;
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
        try {
            if (this.isActive) {
                if (((this.data != null) && !this.data.equals(newData)) || ((this.data == null) && (newData != null))) {
                    this.data = newData;
                    return true;
                } else {
                    return false;
                }
            } else {
                throw new FileException("This file no longer exists.");
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Deletes this file. <br/>
     * Lock used: <i>Write</i> <br/>
     * Note: This object will still exist, but it will be marked as inactive and the data will be nullified.
     *
     * @throws FileException
     *             If this file was already deleted by another process.
     */
    public void delete() throws FileException {
        this.lock.writeLock().lock();
        try {
            if (this.isActive) {
                this.data = null;
                this.isActive = false;
            } else {
                throw new FileException("This file no longer exists.");
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }
}
