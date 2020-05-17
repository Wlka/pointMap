package com.example.pointmap;

public class File {
    private String fileName;
    private int imageId;

    public File(String fileName, int imageId) {
        this.fileName = fileName;
        this.imageId = imageId;
    }

    public String getFileName() {
        return fileName;
    }

    public int getImageId() {
        return imageId;
    }
}
