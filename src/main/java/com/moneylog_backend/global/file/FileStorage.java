package com.moneylog_backend.global.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorage {
    boolean supports(String fileRef);

    String store(MultipartFile multipartFile, String dirHint) throws IOException;

    void delete(String fileRef);

    FileDownloadResult resolveDownload(String fileRef, String originalName);
}
