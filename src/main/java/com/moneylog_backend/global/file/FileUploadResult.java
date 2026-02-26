package com.moneylog_backend.global.file;

public record FileUploadResult(
    String fileUrl,
    String originalName,
    String contentType,
    long size
) {
}
