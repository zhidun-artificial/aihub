package ai.zhidun.app.hub.store.service;

import ai.zhidun.app.hub.store.config.S3Properties;
import ai.zhidun.app.hub.store.utils.FileParser.ParsedResult;
import jakarta.annotation.PreDestroy;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketCannedACL;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.MetadataDirective;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

@Service
public class S3Service implements AutoCloseable {

    private final S3Client client;

    private final String publicEndpoint;

    private final String endpoint;

    public S3Service(S3Properties properties) {
        this.publicEndpoint = properties.publicEndpoint();
        this.endpoint = properties.endpoint();

        var provider = StaticCredentialsProvider.create(AwsBasicCredentials.create(
                properties.accessKeyId(),
                properties.secretAccessKey()
        ));

        this.client = S3Client.builder()
                .endpointOverride(URI.create(properties.endpoint()))
                .credentialsProvider(provider)
                .region(Region.EU_WEST_2)
                .build();
    }

    public void rename(String bucket, String key, String newFileName) {
        String contentDisposition = getContentDisposition(newFileName);

        HeadObjectResponse response = client.headObject(b -> b.bucket(bucket).key(key));

        client.copyObject(builder -> builder
                .sourceBucket(bucket)
                .sourceKey(key)
                .destinationBucket(bucket)
                .destinationKey(key)
                .contentDisposition(contentDisposition)
                .contentType(response.contentType())
                .metadataDirective(MetadataDirective.REPLACE)
                .metadata(Map.of())
        );
    }

    public void put(String bucket, String key, ParsedResult file) {
        String contentDisposition = getContentDisposition(file.fileName());

        client.putObject(builder -> builder
                        .bucket(bucket)
                        .key(key)
                        .contentDisposition(contentDisposition),
                RequestBody.fromContentProvider(ContentStreamProvider.fromInputStreamSupplier(file::getInputStream),
                        Objects.requireNonNull(file.contentType()))
        );
    }

    public void putCover(String bucket, String key, byte[] jpeg) {
        String contentDisposition = getContentDisposition("cover.jpeg");

        client.putObject(builder -> builder
                        .bucket(bucket)
                        .key(key)
                        .contentDisposition(contentDisposition),
                RequestBody.fromContentProvider(ContentStreamProvider.fromByteArray(jpeg), MimeTypeUtils.IMAGE_JPEG_VALUE)
        );
    }

    private static String getContentDisposition(String originalFilename) {
        if (originalFilename == null) {
            originalFilename = "";
        }
        ContentDisposition disposition = ContentDisposition
                .attachment()
                .filename(URLEncoder.encode(originalFilename, StandardCharsets.UTF_8))
                .build();

        return disposition.toString();
    }

    public String localUrl(String url) {
        return url.replaceFirst(publicEndpoint, endpoint);
    }

    public String publicUrl(String url) {
        return url.replaceFirst(endpoint, publicEndpoint);
    }

    public String url(String bucket, String key) {
        return publicEndpoint + "/" + bucket + "/" + key;
    }

    @PreDestroy
    @Override
    public void close() {
        client.close();
    }

    public void createBucketIfNotExists(String bucket) {
        try {
            client.headBucket(builder -> builder
                    .bucket(bucket)
            );
        } catch (NoSuchBucketException e) {
            client.createBucket(builder -> builder
                    .bucket(bucket)
                    .acl(BucketCannedACL.PUBLIC_READ)
            );
        }
    }

    public void delete(String bucket, String key) {
        client.deleteObject(b -> b
                .bucket(bucket)
                .key(key));
    }
}