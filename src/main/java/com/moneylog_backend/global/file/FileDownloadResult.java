package com.moneylog_backend.global.file;

import org.springframework.core.io.Resource;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FileDownloadResult {
    private final Resource resource;
    private final String redirectUrl;

    public static FileDownloadResult local(Resource resource) {
        return new FileDownloadResult(resource, null);
    }

    public static FileDownloadResult redirect(String redirectUrl) {
        return new FileDownloadResult(null, redirectUrl);
    }

    public boolean isRedirect() {
        return redirectUrl != null && !redirectUrl.isBlank();
    }
}
