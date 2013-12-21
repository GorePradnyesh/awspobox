package com.pgore.generator;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class PutUrlGenerator {
    private static final String CONTENT_TYPE = "application/octet-stream";

    private static AmazonS3Client getS3Client() throws IOException {
        InputStream credStream = PutUrlGenerator.class.getResourceAsStream("/credentials.properties");
        return new AmazonS3Client(new PropertiesCredentials(credStream));
    }

    public static URL generateUrl(final String bucketName, final String keyName, final long expiryMS) throws IOException {
        AmazonS3Client s3Client = getS3Client();
        java.util.Date expiration = new java.util.Date();
        expiration.setTime(System.currentTimeMillis() + expiryMS);

        GeneratePresignedUrlRequest req =
                new GeneratePresignedUrlRequest(bucketName, keyName);
        req.setMethod(HttpMethod.PUT);
        req.setExpiration(expiration);
        req.setContentType(CONTENT_TYPE);

        URL s = s3Client.generatePresignedUrl(req);
        return s;
    }

}
