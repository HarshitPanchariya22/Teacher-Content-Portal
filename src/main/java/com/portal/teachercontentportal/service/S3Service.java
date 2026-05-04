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
            return fileName;
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
        }
        catch (Exception e)
        {
            throw new RuntimeException("Cannot delete the bucket "+e);
        }
    }
    public String extractKeyFromUrl(String url)
    {
        return url.substring(url.indexOf(".com/")+5);
    }
}
