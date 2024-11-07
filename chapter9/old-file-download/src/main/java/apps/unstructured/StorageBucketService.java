package apps.unstructured;

import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StorageBucketService {

    final Storage storage = StorageOptions.getDefaultInstance().getService();
    @Value("${gcp.storage.bucket.name}")
    String bucketName;

    public String uploadFile(String originalFilename, byte[] bytes) {
        String fileID = UUID.randomUUID().toString() + originalFilename;
        BlobId blobId = BlobId.of(bucketName, fileID);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, bytes);

        return fileID;
    }


    public void deleteAllItemsInBucket() {
        // List all blobs in the specified bucket
        Iterable<Blob> blobs = storage.list(bucketName).iterateAll();

        // Iterate through each blob and delete it
        for (Blob blob : blobs) {
            storage.delete(blob.getBlobId());
        }
    }

    public Blob getFile(String fileID) {
        return storage.get(BlobId.of(bucketName, fileID));

    }

    public void deleteFile(String fileID) {
        storage.delete(bucketName, fileID);

    }
}
