package com.rvkernel.manager;

import android.content.Context;
import android.content.res.Configuration;
import com.google.android.material.imageview.ShapeableImageView;

public class RvBanner {

    public static void RvBannerTheme(ShapeableImageView imageView, Context context) {
        int nightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        switch (nightMode) {
            case Configuration.UI_MODE_NIGHT_YES:
                imageView.setImageResource(R.drawable.rvkernel_banner_dark);
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                imageView.setImageResource(R.drawable.rvkernel_banner_light);
                break;

            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                break;
        }
    }
}