package com.portal.teachercontentportal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
public class S3Service {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    @Value("${aws_bucketname}")
    private String bucketName;

    public S3Service(S3Client s3Client, S3Presigner s3Presigner)
    {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }
    public String fileUpload(MultipartFile file)
    {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    putRequest,
                    software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes())
        );
            return generatePresignedUrl(fileName);
        }
        catch(Exception e)
        {
            throw new RuntimeException("File upload failed "+ e.getMessage());
        }
    }
    public String generatePresignedUrl(String fileName)
    {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(20))
                .getObjectRequest(getObjectRequest)
                .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }
    public void deleteFile(String fileUrl)
    {
        try{
            String key = extractKeyFromUrl(fileUrl);
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteRequest);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Cannot delete the bucket "+e);
        }
    }
    public String extractKeyFromUrl(String url)
    {
        if (url == null || url.isBlank()) {
            throw new RuntimeException("Invalid file key/url");
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return url;
        }

        String withoutQuery = url.split("\\?")[0];
        int markerIndex = withoutQuery.indexOf(".com/");
        if (markerIndex >= 0) {
            return withoutQuery.substring(markerIndex + 5);
        }

        int pathStart = withoutQuery.indexOf('/', withoutQuery.indexOf("://") + 3);
        if (pathStart >= 0 && pathStart + 1 < withoutQuery.length()) {
            return withoutQuery.substring(pathStart + 1);
        }

        throw new RuntimeException("Unable to extract S3 key from url");
    }
}
