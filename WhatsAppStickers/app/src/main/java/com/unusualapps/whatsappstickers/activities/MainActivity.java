package com.unusualapps.whatsappstickers.activities;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;
import android.view.View;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.github.florent37.depth.DepthProvider;
import com.github.florent37.depth.animations.EnterConfiguration;
import com.github.florent37.depth.animations.ExitConfiguration;
import com.github.florent37.depth.animations.ReduceConfiguration;
import com.sangcomz.fishbun.define.Define;
import com.unusualapps.whatsappstickers.R;
import com.unusualapps.whatsappstickers.backgroundRemover.CutOut;
import com.unusualapps.whatsappstickers.identities.StickerPacksContainer;
import com.unusualapps.whatsappstickers.utils.StickerPacksManager;
import com.unusualapps.whatsappstickers.whatsapp_api.AddStickerPackActivity;

import java.util.Objects;


public class MainActivity extends AddStickerPackActivity {
    private MyStickersFragment myStickersFragment;
    private ExploreFragment exploreFragment;
    private CreateFragment createFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fresco.initialize(this);
        this.initBottomNavigation();
        this.setupFragments();
        setFragmento(myStickersFragment);
        StickerPacksManager.stickerPacksContainer = new StickerPacksContainer("", "", StickerPacksManager.getStickerPacks(this));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_home:
                    setFragmento(myStickersFragment);
                    return true;
                /*case R.id.menu_explore:
                    setFragmento(exploreFragment);
                    return true;*/
                case R.id.menu_create:
                    setFragmento(createFragment);
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CutOut.CUTOUT_ACTIVITY_REQUEST_CODE || requestCode == Define.ALBUM_REQUEST_CODE) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_principal);
            Objects.requireNonNull(fragment).onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void setupFragments() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        myStickersFragment = new MyStickersFragment();
        exploreFragment = new ExploreFragment();
        createFragment = new CreateFragment();
        fragmentTransaction.add(R.id.frame_principal, myStickersFragment);
        fragmentTransaction.add(R.id.frame_principal, exploreFragment);
        fragmentTransaction.add(R.id.frame_principal, createFragment);
        fragmentTransaction.hide(exploreFragment);
        fragmentTransaction.hide(createFragment);
        fragmentTransaction.commit();
    }

    private void setFragmento(Fragment fragmento) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        if (fragmento == myStickersFragment) {
            fragmentTransaction.hide(exploreFragment);
            fragmentTransaction.hide(createFragment);
        } else if (fragmento == exploreFragment) {
            fragmentTransaction.hide(myStickersFragment);
            fragmentTransaction.hide(createFragment);
        } else if (fragmento == createFragment) {
            fragmentTransaction.hide(myStickersFragment);
            fragmentTransaction.hide(exploreFragment);
        }
        fragmentTransaction.show(fragmento);
        fragmentTransaction.commit();
    }
}
