package edu.sjsu.cs249.project1.client;

/**
 * Represents a File on the Client side. <br/>
 * Note: Synchronization is not needed here, since a client file is only used by one entity (the client itself).
 */
public class File {
    private byte[] data;
    private boolean isValid;

    public File(final byte[] data) {
        this.data = data;
        this.isValid = true;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(final byte[] data) {
        this.data = data;
    }

    public boolean isValid() {
        return this.isValid;
    }

    public void setValid(final boolean isValid) {
        this.isValid = isValid;
    }
}