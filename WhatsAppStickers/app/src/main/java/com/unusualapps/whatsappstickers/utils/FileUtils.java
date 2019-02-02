package com.unusualapps.whatsappstickers.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import com.unusualapps.whatsappstickers.constants.Constants;

import java.io.*;
import java.util.Objects;
import java.util.Random;

public class FileUtils {

    public static String generateRandomIdentifier() {
        String possibilities = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder generatedIdentifier = new StringBuilder();
        for (int i = 0; i < Constants.STICKER_PACK_IDENTIFIER_LENGHT; i++) {
            generatedIdentifier.append(possibilities.charAt(random.nextInt(possibilities.length() - 1)));
        }
        return generatedIdentifier.toString();
    }

    public static void initializeDirectories(Context context) {
        File directory = new File(Constants.STICKERS_DIRECTORY_PATH);
        if (!directory.exists()) {
            directory.mkdir();
            String value = "{\"androidPlayStoreLink\": \"\",\"iosAppStoreLink\": \"\",\"stickerPacks\": [ ]}";
            try {
                PrintWriter out = new PrintWriter(Constants.STICKERS_DIRECTORY_PATH + "/contents.json");
                out.write(value);
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    static void deleteFolder(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteFolder(files[i].getPath());
                } else {
                    files[i].delete();
                }
            }
        }
        dir.delete();
    }

    public static void deleteFile(String path, Context context) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        context.getContentResolver().delete(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaStore.MediaColumns.DATA + "='" + path + "'", null
        );
    }

    public static String getImageRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            if (cursor != null) {
                int column_index = Objects.requireNonNull(cursor).getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            } else {
                return contentUri.getPath();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String getFolderSizeLabel(String path) {
        long size = getFolderSize(new File(path)) / 1024; // Get size and convert bytes into Kb.
        if (size >= 1024) {
            return (size / 1024) + " MB";
        } else {
            return size + " KB";
        }
    }

    private static long getFolderSize(File file) {
        long size = 0;
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                size += getFolderSize(child);
            }
        } else {
            size = file.length();
        }
        return size;
    }
}
