package com.shopcoup.shareloc;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.mobileconnectors.s3.transfermanager.Download;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;

import java.io.File;
import java.util.UUID;

public class S3Util {
    public static String uploadFile(File image){

        AWSCredentials awsCredentials = new AWSCredentials() {

            @Override
            public String getAWSSecretKey() {
                return "9P0j4KQpFYRuXPgs1wCXDpoTI7he3e2R/Fw4V6Pd";
            }

            @Override
            public String getAWSAccessKeyId() {
                return "AKIAIBK4YOW364T6CMVA";
            }
        };

        File outputFile = image;
        TransferManager transferManager = new TransferManager(awsCredentials);
        String fileName = UUID.randomUUID().toString();
        try {

            Upload upload = transferManager.upload("addressdata", fileName, outputFile);
        }catch(Exception e){
            Log.i("S3", "error while uploading file");
            e.printStackTrace();
        }
        Log.i("S3", "going to upload file name: "+ fileName);
//		UploadResult uploadResult = null;
//		try {
//			uploadResult = upload.waitForUploadResult();
//		} catch (AmazonServiceException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (AmazonClientException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		while(upload.isDone()){
//			//Log.i("S3", "waiting for upload");
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
        Log.i("S3", "Done");
        Log.i("S3", fileName);

        return fileName;
    }

    public static Download download(String filename , File downloadedFile){
        AWSCredentials awsCredentials = new AWSCredentials() {

            @Override
            public String getAWSSecretKey() {
                return "9P0j4KQpFYRuXPgs1wCXDpoTI7he3e2R/Fw4V6Pd";
            }

            @Override
            public String getAWSAccessKeyId() {
                return "AKIAIBK4YOW364T6CMVA";
            }
        };
        TransferManager transferManager = new TransferManager(awsCredentials);
        Download downloaded = null;
        try {
            downloaded = transferManager.download("addressdata", filename, downloadedFile);
        }catch(Exception e){
            Log.i("S3", "error while downloading file");
            e.printStackTrace();
        }
        return downloaded;
    }
}
