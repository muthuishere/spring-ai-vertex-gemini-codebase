package apps.unstructured;

import com.google.cloud.storage.Blob;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StorageBucketServiceIntegrationTest {


    @Autowired
    private StorageBucketService storageBucketService;  // Your service containing the uploadFile method


    @Test
    @SneakyThrows
    public void testUploadFileE2E() {


        // Arrange
        String originalFilename = "testFile.txt";  // The name of the file to be uploaded
        byte[] bytes = "This is a test file".getBytes();

        //Act
        String fileID = storageBucketService.uploadFile(originalFilename, bytes);


        // Assert

        Blob blob = storageBucketService.getFile(fileID);

        assertThat(blob).isNotNull();  // Check if the file exists in the bucket
        assertThat(blob.getName()).isEqualTo(fileID);  // Ensure the file name matches
        assertThat(blob.getSize()).isEqualTo(bytes.length);  // Verify file size

        storageBucketService.deleteFile(fileID);

         blob = storageBucketService.getFile(fileID);
         assertThat(blob).isNull();
    }
}