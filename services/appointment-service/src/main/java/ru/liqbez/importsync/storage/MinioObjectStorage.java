package ru.liqbez.importsync.storage;

import java.io.ByteArrayInputStream;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "demo.import", name = "storage", havingValue = "minio")
public class MinioObjectStorage implements ObjectStorage {
    private final MinioClient client;
    private final String bucket;

    public MinioObjectStorage(@Value("${demo.import.minio.url}") String url,
                              @Value("${demo.import.minio.access-key}") String accessKey,
                              @Value("${demo.import.minio.secret-key}") String secretKey,
                              @Value("${demo.import.minio.bucket}") String bucket) {
        this.client = MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build();
        this.bucket = bucket;
    }

    @Override
    public void put(String key, byte[] content) {
        try {
            if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket).object(key)
                    .stream(new ByteArrayInputStream(content), (long) content.length, -1L)
                    .contentType("text/plain")
                    .build());
        } catch (Exception ex) {
            throw new IllegalStateException("Could not store import object in MinIO: " + key, ex);
        }
    }

    @Override
    public void remove(String key) {
        try {
            client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(key).build());
        } catch (Exception ex) {
            throw new IllegalStateException("Could not remove import object from MinIO: " + key, ex);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            client.statObject(StatObjectArgs.builder().bucket(bucket).object(key).build());
            return true;
        } catch (ErrorResponseException ex) {
            if ("NoSuchKey".equals(ex.errorResponse().code())
                    || "NoSuchBucket".equals(ex.errorResponse().code())) return false;
            throw new IllegalStateException("Could not inspect import object in MinIO: " + key, ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not inspect import object in MinIO: " + key, ex);
        }
    }
}
