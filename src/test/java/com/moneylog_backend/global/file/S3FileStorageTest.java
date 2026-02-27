package com.moneylog_backend.global.file;

import com.moneylog_backend.global.constant.ErrorMessageConstants;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

class S3FileStorageTest {
    private S3Client s3Client;
    private S3Presigner s3Presigner;
    private S3FileStorage s3FileStorage;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        s3Presigner = mock(S3Presigner.class);

        FileProperties fileProperties = new FileProperties();
        fileProperties.getS3().setBucket("moneylog-bucket");
        fileProperties.getS3().setKeyPrefix("uploads");

        s3FileStorage = new S3FileStorage(s3Client, s3Presigner, fileProperties);
    }

    @Test
    void 다른_버킷_fileRef는_delete를_거부한다() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                   () -> s3FileStorage.delete("s3://attacker-bucket/uploads/a.jpg"));
        assertEquals(ErrorMessageConstants.INVALID_FILE_URL, ex.getMessage());
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void keyPrefix_밖_fileRef는_view_다운로드를_거부한다() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                   () -> s3FileStorage.resolveDownload(
                                                       "s3://moneylog-bucket/other-prefix/a.jpg",
                                                       null));
        assertEquals(ErrorMessageConstants.INVALID_FILE_URL, ex.getMessage());
        verify(s3Presigner, never()).presignGetObject(any(GetObjectPresignRequest.class));
    }
}
