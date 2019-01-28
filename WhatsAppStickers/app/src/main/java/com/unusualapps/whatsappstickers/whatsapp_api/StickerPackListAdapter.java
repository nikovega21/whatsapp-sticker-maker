/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.unusualapps.whatsappstickers.whatsapp_api;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.facebook.drawee.view.SimpleDraweeView;
import com.unusualapps.whatsappstickers.R;
import com.unusualapps.whatsappstickers.activities.MyStickersFragment;
import com.unusualapps.whatsappstickers.constants.Constants;
import com.unusualapps.whatsappstickers.utils.FileUtils;
import com.unusualapps.whatsappstickers.utils.ImageUtils;
import com.unusualapps.whatsappstickers.utils.StickerPacksManager;

import java.util.List;

public class StickerPackListAdapter extends RecyclerView.Adapter<StickerPackListItemViewHolder> {
    @NonNull
    private List<StickerPack> stickerPacks;
    @NonNull
    private final OnAddButtonClickedListener onAddButtonClickedListener;
    private int maxNumberOfStickersInARow;
    private MyStickersFragment parent;

    public StickerPackListAdapter(@NonNull List<StickerPack> stickerPacks, @NonNull OnAddButtonClickedListener onAddButtonClickedListener, MyStickersFragment parent) {
        this.stickerPacks = stickerPacks;
        this.onAddButtonClickedListener = onAddButtonClickedListener;
        this.parent = parent;
    }

    @NonNull
    @Override
    public StickerPackListItemViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int i) {
        final Context context = viewGroup.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View stickerPackRow = layoutInflater.inflate(R.layout.sticker_packs_list_item, viewGroup, false);
        return new StickerPackListItemViewHolder(stickerPackRow);
    }

    @Override
    public void onBindViewHolder(@NonNull final StickerPackListItemViewHolder viewHolder, final int index) {
        StickerPack pack = stickerPacks.get(index);
        final Context context = viewHolder.publisherView.getContext();
        viewHolder.publisherView.setText(pack.publisher);
        viewHolder.filesizeView.setText(FileUtils.getFolderSizeLabel(Constants.STICKERS_DIRECTORY_PATH + pack.identifier));

        viewHolder.titleView.setText(pack.name);
        viewHolder.container.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), StickerPackDetailsActivity.class);
            intent.putExtra(StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON, true);
            intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA, pack);
            view.getContext().startActivity(intent);
        });
        viewHolder.imageRowView.removeAllViews();
        //if this sticker pack contains less stickers than the max, then take the smaller size.
        int actualNumberOfStickersToShow = Math.min(maxNumberOfStickersInARow, pack.getStickers().size());
        for (int i = 0; i < actualNumberOfStickersToShow; i++) {
            final SimpleDraweeView rowImage = (SimpleDraweeView) LayoutInflater.from(context).inflate(R.layout.sticker_pack_list_item_image, viewHolder.imageRowView, false);
            rowImage.setImageURI(ImageUtils.getStickerImageAsset(pack.identifier, pack.getStickers().get(i).imageFileName));
            final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) rowImage.getLayoutParams();
            final int marginBetweenImages = (viewHolder.imageRowView.getMeasuredWidth() - maxNumberOfStickersInARow * viewHolder.imageRowView.getContext().getResources().getDimensionPixelSize(R.dimen.sticker_pack_list_item_preview_image_size)) / (maxNumberOfStickersInARow - 1) - lp.leftMargin - lp.rightMargin;
            if (i != actualNumberOfStickersToShow - 1 && marginBetweenImages > 0) { //do not set the margin for the last image
                lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin + marginBetweenImages, lp.bottomMargin);
                rowImage.setLayoutParams(lp);
            }
            viewHolder.imageRowView.addView(rowImage);
        }
        setAddButtonAppearance(viewHolder.addButton, pack);

        viewHolder.container.setOnLongClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(context, viewHolder.addButton);
            popupMenu.inflate(R.menu.sticker_option_menu);
            popupMenu.setOnMenuItemClickListener(item -> {

                switch (item.getItemId()) {
                    case R.id.sticker_share:
                        Toast.makeText(context, "Shared", Toast.LENGTH_LONG).show();
                        break;
                    case R.id.sticker_delete:
                        new AlertDialog.Builder(context)
                                .setTitle("Deleting")
                                .setMessage("Are you sure you want to delete this sticker pack?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    removeStickerPack(index);
                                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                                    parent.verifyStickersCount();
                                })
                                .setNegativeButton("No", null)
                                .show();
                        break;
                    case R.id.sticker_edit:

                        Toast.makeText(context, "Edited", Toast.LENGTH_LONG).show();
                        break;
                    case R.id.sticker_save_gallery:
                        Toast.makeText(context, "Saved", Toast.LENGTH_LONG).show();
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

    private void removeStickerPack(int index) {
        stickerPacks.remove(index);
        notifyItemRemoved(index);
        notifyItemRangeChanged(index, stickerPacks.size());
        StickerPacksManager.deleteStickerPack(index);
    }

    private void setAddButtonAppearance(ImageView addButton, StickerPack pack) {
        if (pack.getIsWhitelisted()) {
            addButton.setImageResource(R.drawable.sticker_3rdparty_added);
            addButton.setClickable(false);
            addButton.setOnClickListener(null);
            setBackground(addButton);
        } else {
            addButton.setImageResource(R.drawable.sticker_3rdparty_add);
            addButton.setOnClickListener(v -> onAddButtonClickedListener.onAddButtonClicked(pack));
            TypedValue outValue = new TypedValue();
            addButton.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            addButton.setBackgroundResource(outValue.resourceId);
        }
    }

    private void setBackground(View view) {
        view.setBackground(null);
    }

    @Override
    public int getItemCount() {
        return stickerPacks.size();
    }

    public void setMaxNumberOfStickersInARow(int maxNumberOfStickersInARow) {
        if (this.maxNumberOfStickersInARow != maxNumberOfStickersInARow) {
            this.maxNumberOfStickersInARow = maxNumberOfStickersInARow;
            notifyDataSetChanged();
        }
    }

    public void setStickerPackList(List<StickerPack> stickerPackList) {
        this.stickerPacks = stickerPackList;
        notifyDataSetChanged();
    }

    public interface OnAddButtonClickedListener {
        void onAddButtonClicked(StickerPack stickerPack);
    }
}
