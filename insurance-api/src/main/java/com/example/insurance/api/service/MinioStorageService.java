package com.example.insurance.api.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket:insurance-documents}")
    private String defaultBucket;

    /**
     * 上传文件到 MinIO
     *
     * @param bucket     存储桶
     * @param objectKey  对象键（路径）
     * @param content    文件内容
     * @param contentType 内容类型
     * @return 访问 URL
     */
    public String uploadFile(String bucket, String objectKey, byte[] content, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(new ByteArrayInputStream(content), content.length, -1)
                    .contentType(contentType)
                    .build());

            String url = minioClient.getPresignedObjectUrl(
                    io.minio.GetPresignedObjectUrlArgs.builder()
                            .method(io.minio.http.Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(7 * 24 * 60 * 60)
                            .build()
            );

            log.info("Uploaded file to MinIO: bucket={}, objectKey={}", bucket, objectKey);
            return url;

        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: bucket={}, objectKey={}", bucket, objectKey, e);
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }

    /**
     * 上传文件到默认存储桶
     */
    public String uploadFile(String objectKey, byte[] content, String contentType) {
        return uploadFile(defaultBucket, objectKey, content, contentType);
    }

    /**
     * 生成对象键
     */
    public String generateObjectKey(String productCode, String fileName) {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("products/%s/%s_%s", productCode, uuid, fileName);
    }
}