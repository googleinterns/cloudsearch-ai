package com.google.cloudsearch.ai;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class CloudStorageHandler {
    public static boolean isCouldStorageURI(String str){
        if(str.substring(0,5).equals("gs://")) {
            return true;
        }
        else
            return false;
    }
    /**
     *
     * @param URI   The CloudStorage Bucket URI of the Object
     * @return      The content of the object
     */
    public static String getObject(String URI) {

        Storage storage = StorageOptions.newBuilder().build().getService();
        String[] uri = URI.split("/");
        String bucketName = uri[2];
        String objectName = uri[3];
        Blob blob = storage.get(BlobId.of(bucketName, objectName));
        Path tempStoragePath = Paths.get("./src/main/resources"+objectName);
        System.out.println(
                "Downloaded object "
                        + objectName
                        + " from bucket name "
                        + bucketName
                        + " to "
                        + tempStoragePath);

        File tempFile = new File(String.valueOf(tempStoragePath));
        String data = "";
        try {
            Scanner fileReader = new Scanner(tempFile);
            while (fileReader.hasNextLine()) {
                data += fileReader.nextLine();
            }
            //Delete the temporary file
            tempFile.delete();
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    return data;
    }
     public static void main(String... args) throws Exception {
        if(isCouldStorageURI("gs://cloudsearch-ai-covid-dataset/dataset.csv"))
            System.out.println(getObject("gs://cloudsearch-ai-covid-dataset/dataset.csv"));
     }

}
