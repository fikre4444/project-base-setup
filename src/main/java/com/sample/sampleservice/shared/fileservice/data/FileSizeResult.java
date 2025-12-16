package com.sample.sampleservice.shared.fileservice.data;

public class FileSizeResult {
    private final long bytes;
    private final boolean usedDefault;

    public FileSizeResult(long bytes, boolean usedDefault) {
        this.bytes = bytes;
        this.usedDefault = usedDefault;
    }

    public long getBytes() {
        return bytes;
    }

    public boolean isUsedDefault() {
        return usedDefault;
    }
}
