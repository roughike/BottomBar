package com.roughike.bottombar;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;

/**
 * Created by iiro on 17.8.2016.
 */
final class NavbarUtils {
    private static final int RESOURCE_NOT_FOUND = 0;

    @IntRange(from = 0)
    static int getNavbarHeight(@NonNull Context context) {
        Resources res = context.getResources();
        int navBarIdentifier = res.getIdentifier("navigation_bar_height", "dimen", "android");
        return navBarIdentifier != RESOURCE_NOT_FOUND
                ? res.getDimensionPixelSize(navBarIdentifier) : 0;
    }

    static boolean shouldDrawBehindNavbar(@NonNull Context context) {
        return isPortrait(context)
                && hasSoftKeys(context);
    }

    private static boolean isPortrait(@NonNull Context context) {
        Resources res = context.getResources();
        return res.getBoolean(R.bool.bb_bottom_bar_is_portrait_mode);
    }

    /**
     * http://stackoverflow.com/a/14871974
     */
    private static boolean hasSoftKeys(@NonNull Context context) {
        boolean hasSoftwareKeys = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display d = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

            DisplayMetrics realDisplayMetrics = new DisplayMetrics();
            d.getRealMetrics(realDisplayMetrics);

            int realHeight = realDisplayMetrics.heightPixels;
            int realWidth = realDisplayMetrics.widthPixels;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            d.getMetrics(displayMetrics);

            int displayHeight = displayMetrics.heightPixels;
            int displayWidth = displayMetrics.widthPixels;

            hasSoftwareKeys = (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
            boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            hasSoftwareKeys = !hasMenuKey && !hasBackKey;
        }

        return hasSoftwareKeys;
    }
}
