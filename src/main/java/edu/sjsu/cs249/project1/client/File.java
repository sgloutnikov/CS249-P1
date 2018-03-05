package edu.sjsu.cs249.project1.client;

public class File {

    private byte[] data;
    private boolean isValid;


    public File(byte[] data, boolean isValid) {
        this.data = data;
        this.isValid = isValid;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public boolean getValidStatus(){
        return this.isValid;
    }
}
