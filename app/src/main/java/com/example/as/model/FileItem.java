package com.example.as.model;

public class FileItem {
    private String name;
    private String size;
    private int progress;
    private DownloadStatus status;

    public enum DownloadStatus {
        NOT_STARTED,
        DOWNLOADING,
        COMPLETED
    }

    public FileItem(String name, String size) {
        this.name = name;
        this.size = size;
        this.progress = 0;
        this.status = DownloadStatus.NOT_STARTED;
    }

    // --- Getters y Setters ---
    public String getName() { return name; }
    public String getSize() { return size; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public DownloadStatus getStatus() { return status; }
    public void setStatus(DownloadStatus status) { this.status = status; }
}