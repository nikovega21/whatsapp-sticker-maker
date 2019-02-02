package com.unusualapps.whatsappstickers.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.sangcomz.fishbun.FishBun;
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter;
import com.sangcomz.fishbun.define.Define;
import com.unusualapps.whatsappstickers.R;
import com.unusualapps.whatsappstickers.backgroundRemover.CutOut;
import com.unusualapps.whatsappstickers.constants.Constants;
import com.unusualapps.whatsappstickers.utils.FileUtils;
import com.unusualapps.whatsappstickers.utils.StickerPacksManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class CreateFragment extends Fragment {
    ImagesGridAdapter imagesGridAdapter;
    View view;

    public CreateFragment() {
        // Required empty public constructor
    }

    private ArrayList<Uri> loadStickersCreated() {
        String directoryPath = Constants.STICKERS_CREATED_DIRECTORY_PATH;
        File directory = new File(directoryPath);
        ArrayList<Uri> images = new ArrayList<>();
        if (directory.exists()) {
            File[] stickersImages = directory.listFiles();
            for (File f : stickersImages) {
                if (f.isFile() && (f.getName().contains(".png") || f.getName().contains(".PNG"))) {
                    images.add(Uri.fromFile((f)));
                }
            }
        } else {
            directory.mkdir();
        }
        verifyStickersCount();
        return images;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_create, container, false);
        view.findViewById(R.id.create_sticker).setOnClickListener(v -> FishBun.with(getActivity())
                .setImageAdapter(new GlideAdapter())
                .setMaxCount(1)
                .exceptGif(true)
                .setMinCount(1)
                .setActionBarColor(Color.parseColor("#128c7e"), Color.parseColor("#128c7e"), false)
                .setActionBarTitleColor(Color.parseColor("#ffffff"))
                .startAlbum());
        RecyclerView gridview = view.findViewById(R.id.stickers_created_grid);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(view.getContext(), 3);
        gridview.setLayoutManager(gridLayoutManager);
        imagesGridAdapter = new ImagesGridAdapter(view.getContext(), loadStickersCreated());
        gridview.setAdapter(imagesGridAdapter);
        verifyStickersCount();
        return view;
    }

    public void verifyStickersCount() {
        View linearLayout = view.findViewById(R.id.no_stickers_icon);
        if (imagesGridAdapter == null || imagesGridAdapter.getItemCount() == 0) {
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.GONE);
        }
    }

    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CutOut.CUTOUT_ACTIVITY_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Uri imageUri = CutOut.getUri(data);
                    String stickerName = FileUtils.generateRandomIdentifier();
                    Uri imagePath = Uri.parse(Constants.STICKERS_CREATED_DIRECTORY_PATH + stickerName + ".PNG");
                    StickerPacksManager.createStickerImageFile(imageUri, imagePath, getActivity(), Bitmap.CompressFormat.PNG);
                    addImageToGallery(imagePath.getPath(), getActivity());
                    imagesGridAdapter.uries = loadStickersCreated();
                    imagesGridAdapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(), "Sticker created", Toast.LENGTH_LONG).show();
                    verifyStickersCount();
                    break;
                case CutOut.CUTOUT_ACTIVITY_RESULT_ERROR_CODE:
                    Exception ex = CutOut.getError(data);
                    break;
                default:
                    System.out.print("User cancelled the CutOut screen");
            }
        } else if (requestCode == Define.ALBUM_REQUEST_CODE) {
            ArrayList<Uri> uries;
            if (resultCode == Activity.RESULT_OK) {
                uries = data.getParcelableArrayListExtra(Define.INTENT_PATH);
                CutOut.activity().src(uries.get(0)).intro().start(getActivity());
            }
        }
    }

    class ImagesGridAdapter extends RecyclerView.Adapter<ImageViewHolder> {
        ArrayList<Uri> uries = new ArrayList<>();
        Context context;

        public ImagesGridAdapter(Context context, ArrayList<Uri> uries) {
            this.uries = uries;
            this.context = context;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            final Context context = viewGroup.getContext();
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            final View view = layoutInflater.inflate(R.layout.sticker_created_item, viewGroup, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder imageAdapter, int index) {
            imageAdapter.imageView.setImageURI(uries.get(index));
            imageAdapter.imageView.setPadding(8, 8, 8, 8);
            imageAdapter.imageView.setOnLongClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(context, imageAdapter.imageView);
                popupMenu.inflate(R.menu.sticker_menu);
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.add_to_exist_stickerpack:
                            addToStickerPack(index);
                            break;
                        case R.id.delete_sticker:
                            this.deleteSticker(index);
                            break;
                        default:
                            break;
                    }
                    return false;
                });
                popupMenu.show();
                return false;
            });
        }

        void deleteSticker(int index) {
            new AlertDialog.Builder(context)
                    .setTitle("Deleting")
                    .setMessage("Are you sure you want to delete this sticker?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Uri uri = uries.get(index);
                        FileUtils.deleteFile(uri.getPath(),context);
                        uries.remove(index);
                        notifyItemRemoved(index);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                        verifyStickersCount();
                    })
                    .setNegativeButton("No", null)
                    .show();
        }

        void addToStickerPack(int index) {
            Intent intent = new Intent(context, AddToStickerPackActivity.class);
            intent.setData(uries.get(index));
            startActivity(intent);
        }

        @Override
        public int getItemCount() {
            return uries.size();
        }
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.sticker_created_image);
        }

    }
}
