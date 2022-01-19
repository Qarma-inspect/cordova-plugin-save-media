package com.qarma.cordova;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.json.JSONArray;
import org.json.JSONException;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

/**
 * The SaveImage class offers a method saving an image to the devices' media gallery.
 */
public class SaveImage extends CordovaPlugin {
    public static final int WRITE_IMAGE_PERM_REQUEST_CODE = 1;
    public static final int WRITE_VIDEO_PERM_REQUEST_CODE = 2;
    private final String IMAGE_ACTION = "saveImageToGallery";
    private final String VIDEO_ACTION = "saveVideoToGallery";
    private final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private CallbackContext callbackContext;
    private String imageFilePath;
    private String videoFilePath;
    

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals(IMAGE_ACTION)) {
            saveImageToGallery(args, callbackContext);
            return true;
        } else if(action.equals(VIDEO_ACTION)) {
            saveVideoToGallery(args, callbackContext);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check saveImage arguments and app permissions
     *
     * @param args              JSON Array of args
     * @param callbackContext   callback id for optional progress reports
     *
     * args[0] filePath         file path string to image file to be saved to gallery
     */  
    private void saveImageToGallery(JSONArray args, CallbackContext callback) throws JSONException {
    	this.imageFilePath = args.getString(0);
    	this.callbackContext = callback;
        Log.d("SaveImage", "SaveImage in filePath: " + imageFilePath);
        
        if (imageFilePath == null || imageFilePath.equals("")) {
        	callback.error("Missing filePath");
            return;
        }
        
        if (PermissionHelper.hasPermission(this, WRITE_EXTERNAL_STORAGE)) {
        	Log.d("SaveImage", "Permissions already granted, or Android version is lower than 6");
        	performImageSave();
        } else {
        	Log.d("SaveImage", "Requesting permissions for WRITE_EXTERNAL_STORAGE");
        	PermissionHelper.requestPermission(this, WRITE_IMAGE_PERM_REQUEST_CODE, WRITE_EXTERNAL_STORAGE);
        }      
    }

    /**
     * Check saveVideo arguments and app permissions
     *
     * @param args              JSON Array of args
     * @param callbackContext   callback id for optional progress reports
     *
     * args[0] filePath         file path string to image file to be saved to gallery
     */  
    private void saveVideoToGallery(JSONArray args, CallbackContext callback) throws JSONException {
    	this.videoFilePath = args.getString(0);
    	this.callbackContext = callback;
        Log.d("SaveImage", "SaveImage in filePath: " + videoFilePath);
        
        if (videoFilePath == null || videoFilePath.equals("")) {
        	callback.error("Missing filePath");
            return;
        }
        
        if (PermissionHelper.hasPermission(this, WRITE_EXTERNAL_STORAGE)) {
        	Log.d("SaveImage", "Permissions already granted, or Android version is lower than 6");
        	performVideoSave();
        } else {
        	Log.d("SaveImage", "Requesting permissions for WRITE_EXTERNAL_STORAGE");
        	PermissionHelper.requestPermission(this, WRITE_IMAGE_PERM_REQUEST_CODE, WRITE_EXTERNAL_STORAGE);
        }      
    }
    
    /**
     * Save image to device gallery
     */
    private void performImageSave() throws JSONException {
        // create file from passed path
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        ContentResolver contentResolver = this.cordova.getContext().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, timeStamp);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, timeStamp);
        values.put(MediaStore.Images.Media.DESCRIPTION, timeStamp);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // Add the date meta data to ensure the image is added at the front of the gallery
        long millis = System.currentTimeMillis();
        values.put(MediaStore.Images.Media.DATE_ADDED, millis / 1000L);
        values.put(MediaStore.Images.Media.DATE_MODIFIED, millis / 1000L);
        values.put(MediaStore.Images.Media.DATE_TAKEN, millis);

        Uri fileUri = null;

        try {
            fileUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (imageFilePath != null) {
                final int BUFFER_SIZE = 1024;

                FileInputStream fileStream = new FileInputStream(imageFilePath);
                try {
                    OutputStream imageOut = contentResolver.openOutputStream(fileUri);
                    try {
                        byte[] buffer = new byte[BUFFER_SIZE];
                        while (true) {
                            int numBytesRead = fileStream.read(buffer);
                            if (numBytesRead <= 0) {
                                break;
                            }
                            imageOut.write(buffer, 0, numBytesRead);
                        }
                    } finally {
                        imageOut.close();
                    }
                } finally {
                    fileStream.close();
                    callbackContext.success(fileUri.getPath());
                }
            } else {
                contentResolver.delete(fileUri, null, null);
            }
        } catch (Exception e) {
            callbackContext.error("RuntimeException occurred: " + e.getMessage());
            if (fileUri != null) {
                contentResolver.delete(fileUri, null, null);
            }
        }
    }

    /**
     * Save image to device gallery
     */
    private void performVideoSave() throws JSONException {
        // create file from passed path
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        ContentResolver contentResolver = this.cordova.getContext().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.TITLE, timeStamp);
        values.put(MediaStore.Video.Media.DISPLAY_NAME, timeStamp);
        values.put(MediaStore.Video.Media.DESCRIPTION, timeStamp);
        // Add the date meta data to ensure the image is added at the front of the gallery
        long millis = System.currentTimeMillis();
        values.put(MediaStore.Video.Media.DATE_ADDED, millis / 1000L);
        values.put(MediaStore.Video.Media.DATE_MODIFIED, millis / 1000L);
        values.put(MediaStore.Video.Media.DATE_TAKEN, millis);
        
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");

        Uri fileUri = null;

        try {
            fileUri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

            if (videoFilePath != null) {
                final int BUFFER_SIZE = 1024;

                FileInputStream fileStream = new FileInputStream(videoFilePath);
                try {
                    OutputStream imageOut = contentResolver.openOutputStream(fileUri);
                    try {
                        byte[] buffer = new byte[BUFFER_SIZE];
                        while (true) {
                            int numBytesRead = fileStream.read(buffer);
                            if (numBytesRead <= 0) {
                                break;
                            }
                            imageOut.write(buffer, 0, numBytesRead);
                        }
                    } finally {
                        imageOut.close();
                    }
                } finally {
                    fileStream.close();
                    callbackContext.success(fileUri.getPath());
                }
            } else {
                contentResolver.delete(fileUri, null, null);
            }
        } catch (Exception e) {
            callbackContext.error("RuntimeException occurred: " + e.getMessage());
            if (fileUri != null) {
                contentResolver.delete(fileUri, null, null);
            }
        }
    }
    
    /**
     * Callback from PermissionHelper.requestPermission method
     */
	public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
		for (int r : grantResults) {
			if (r == PackageManager.PERMISSION_DENIED) {
				Log.d("SaveImage", "Permission not granted by the user");
				callbackContext.error("Permissions denied");
				return;
			}
		}
		
		switch (requestCode) {
            case WRITE_IMAGE_PERM_REQUEST_CODE:
                Log.d("SaveImage", "User granted the permission for WRITE_EXTERNAL_STORAGE");
                performImageSave();
                break;
            case WRITE_VIDEO_PERM_REQUEST_CODE:
                Log.d("SaveImage", "User granted the permission for WRITE_EXTERNAL_STORAGE");
                performVideoSave();
                break;
        }
    }
}
