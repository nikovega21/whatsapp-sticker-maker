package com.unusualapps.whatsappstickers.activities;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import com.unusualapps.whatsappstickers.R;
import com.unusualapps.whatsappstickers.identities.StickerPacksContainer;
import com.unusualapps.whatsappstickers.utils.StickerPacksManager;

import java.util.ArrayList;

public class AddToStickerPackActivity extends AppCompatActivity {

    Uri stickerUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_sticker_pack);
        this.stickerUri = this.getIntent().getData();
        StickerPacksManager.stickerPacksContainer = new StickerPacksContainer("", "", StickerPacksManager.getStickerPacks(this));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Toast.makeText(this, stickerUri.getPath(), Toast.LENGTH_LONG).show();
    }
}