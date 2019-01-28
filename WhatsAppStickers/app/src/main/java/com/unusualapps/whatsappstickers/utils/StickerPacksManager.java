package com.unusualapps.whatsappstickers.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.unusualapps.whatsappstickers.constants.Constants;
import com.unusualapps.whatsappstickers.identities.StickerPacksContainer;
import com.unusualapps.whatsappstickers.whatsapp_api.ContentFileParser;
import com.unusualapps.whatsappstickers.whatsapp_api.Sticker;
import com.unusualapps.whatsappstickers.whatsapp_api.StickerPack;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StickerPacksManager {

    public static StickerPacksContainer stickerPacksContainer = null;

    public static List<Sticker> saveStickerPackFilesLocally(String identifier, List<Uri> stickersUries, Context context) {
        String stickerPath = Constants.STICKERS_DIRECTORY_PATH + identifier;
        List<Sticker> stickerList = new ArrayList<>();
        File directory = new File(stickerPath);
        if (!directory.exists()) {
            directory.mkdir();
        }
        for (Uri uri : stickersUries) {
            Sticker sticker = new Sticker(FileUtils.generateRandomIdentifier() + ".webp", null);
            stickerList.add(sticker);
            saveStickerFilesLocally(sticker, uri, stickerPath, context);
        }
        return stickerList;
    }

    private static void saveStickerFilesLocally(Sticker sticker, Uri stickerUri, String stickerPath, Context context) {
        createStickerImageFile(stickerUri, Uri.parse(stickerPath + "/" + sticker.imageFileName), context, Bitmap.CompressFormat.WEBP);
    }

    public static List<StickerPack> getStickerPacks(Context context) {
        List<StickerPack> stickerPackList = new ArrayList<>();

        if (RequestPermissionsHelper.verifyPermissions(context)) {
            File contentFile = new File(Constants.STICKERS_DIRECTORY_PATH + "contents.json");
            try (InputStream contentsInputStream = new FileInputStream(contentFile)) {
                stickerPackList = ContentFileParser.parseStickerPacks(contentsInputStream);
            } catch (IOException | IllegalStateException e) {
                //throw new RuntimeException("contents.json" + " file has some issues: " + e.getMessage(), e);
                Log.i("Content provider: ", "contents.json" + " file has some issues: " + e.getMessage());
            }
        }
        return stickerPackList;
    }

    public static void saveStickerPacksToJson(StickerPacksContainer container) {
        String json = new Gson().toJson(container);
        try {
            File file = new File(Constants.STICKERS_DIRECTORY_PATH + "/contents.json");
            Writer output = new BufferedWriter(new FileWriter(file));
            output.write(json);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createStickerImageFile(Uri sourceUri, Uri destinyUri, Context context, Bitmap.CompressFormat format) {
        String destinationFilename = destinyUri.getPath();
        try {
            File file = new File(destinationFilename);
            byte[] bitmapdata = ImageUtils.compressImageToBytes(sourceUri, 70, 512, 512, context, format);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createStickerPackTrayIconFile(Uri sourceUri, Uri destinyUri, Context context) {
        String destinationFilename = destinyUri.getPath();
        try {
            File file = new File(destinationFilename);
            byte[] bitmapdata = ImageUtils.compressImageToBytes(sourceUri, 80, 96, 96, context, Bitmap.CompressFormat.PNG);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteStickerPack(int index) {
        StickerPack pack = stickerPacksContainer.removeStickerPack(index);
        FileUtils.deleteFolder(Constants.STICKERS_DIRECTORY_PATH + pack.identifier);
        saveStickerPacksToJson(stickerPacksContainer);
    }
}