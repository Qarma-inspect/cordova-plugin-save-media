package com.qarma.cordova;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.json.JSONArray;
import org.json.JSONException;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

public class ImageSaver extends CordovaPlugin {
    public static final int WRITE_IMAGE_PERM_REQUEST_CODE = 1;
    public static final int WRITE_VIDEO_PERM_REQUEST_CODE = 2;
    private final String IMAGE_ACTION = "saveImageToGallery";
    private final String VIDEO_ACTION = "saveVideoToGallery";
    private final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private String videoFilePath;
    private String videoTitle;
    private String imagePath;
    private String imageTitle;
    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callback) {
        callbackContext = callback;
        if (action.equals(IMAGE_ACTION)) {
            saveImageToGallery(args);
            return true;
        } else if(action.equals(VIDEO_ACTION)) {
            saveVideoToGallery(args);
            return true;
        } else {
            return false;
        }
    }
    private void saveImageToGallery(JSONArray args) {
        try {
            imagePath = args.getString(0);
            imageTitle = args.getString(1);
            if (imagePath == null || imageTitle.equals("")) {
                callbackContext.error("Missing filePath");
            }
        } catch (JSONException e) {
            callbackContext.error(e.getMessage());
        }
        if(isOlderThanAPI33()) {
            if (PermissionHelper.hasPermission(this, WRITE_EXTERNAL_STORAGE)) {
                performImageSave();
            } else {
               requestForPermissions();
            }
        } else {
            performImageSave();
        }
    }

    private void performImageSave() {
        try {
            MediaStore.Images.Media.insertImage(cordova.getContext().getContentResolver(), imagePath, imageTitle, "");
            callbackContext.success(imagePath);
        } catch (IOException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void saveVideoToGallery(JSONArray args) {
    	try {
            this.videoFilePath = args.getString(0);
            this.videoTitle = args.getString(1);

            if (videoFilePath == null || videoFilePath.equals("")) {
                callbackContext.error("Missing filePath");
                return;
            }
            if(isOlderThanAPI33()) {
                if (PermissionHelper.hasPermission(this, WRITE_EXTERNAL_STORAGE)) {
                    performVideoSave();
                } else {
                    requestForPermissions();
                }
            } else {
                performVideoSave();
            }
        } catch (JSONException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void performVideoSave() {
        ContentResolver contentResolver = this.cordova.getContext().getContentResolver();
        ContentValues contentValues = prepareVideoContentResolver();
        Uri fileUri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
        writeFile(fileUri);
    }

    private ContentValues prepareVideoContentResolver() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.TITLE, this.videoTitle);
        values.put(MediaStore.Video.Media.DISPLAY_NAME, this.videoTitle);
        values.put(MediaStore.Video.Media.DESCRIPTION, this.videoTitle);
        long millis = System.currentTimeMillis();
        values.put(MediaStore.Video.Media.DATE_ADDED, millis / 1000L);
        values.put(MediaStore.Video.Media.DATE_MODIFIED, millis / 1000L);
        values.put(MediaStore.Video.Media.DATE_TAKEN, millis);

        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        return values;
    }

    private void writeFile(Uri fileUri) {
        ContentResolver contentResolver = this.cordova.getContext().getContentResolver();
        try {
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

    private void requestForPermissions() {
        PermissionHelper.requestPermission(this, WRITE_IMAGE_PERM_REQUEST_CODE, WRITE_EXTERNAL_STORAGE);
    }

    private boolean isOlderThanAPI33() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU;
    }


	public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
		for (int r : grantResults) {
			if (r == PackageManager.PERMISSION_DENIED) {
				callbackContext.error("Permissions denied");
				return;
			}
		}

		switch (requestCode) {
            case WRITE_VIDEO_PERM_REQUEST_CODE:
                delayInMilliseconds(() -> performVideoSave(), 1000);
                performVideoSave();
                break;
            case WRITE_IMAGE_PERM_REQUEST_CODE:
                delayInMilliseconds(() -> performImageSave(), 1000);
                break;
        }
    }

    private void delayInMilliseconds(Runnable runnable, long duration) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        }, duration);
    }
}