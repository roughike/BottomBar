package com.roughike.bottombar;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.Dimension;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Px;
import android.support.annotation.StyleRes;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.TextView;

import static android.support.annotation.Dimension.DP;

/*
 * BottomBar library for Android
 * Copyright (c) 2016 Iiro Krankka (http://github.com/roughike).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class MiscUtils {

    @NonNull protected static TypedValue getTypedValue(@NonNull Context context, @AttrRes int resId) {
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(resId, tv, true);
        return tv;
    }

    @ColorInt
    protected static int getColor(@NonNull Context context, @AttrRes int color) {
        return getTypedValue(context, color).data;
    }

    @DrawableRes
    protected static int getDrawableRes(@NonNull Context context, @AttrRes int drawable) {
        return getTypedValue(context, drawable).resourceId;
    }

    /**
     * Converts dps to pixels nicely.
     *
     * @param context the Context for getting the resources
     * @param dp      dimension in dps
     * @return dimension in pixels
     */
    protected static int dpToPixel(@NonNull Context context, @Dimension(unit = DP) float dp) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();

        try {
            return (int) (dp * metrics.density);
        } catch (NoSuchFieldError ignored) {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
        }
    }

    /**
     * Converts pixels to dps just as well.
     *
     * @param context the Context for getting the resources
     * @param px      dimension in pixels
     * @return dimension in dps
     */
    protected static int pixelToDp(@NonNull Context context, @Px int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / displayMetrics.density);
    }

    /**
     * Returns screen width.
     *
     * @param context Context to get resources and device specific display metrics
     * @return screen width
     */
    protected static int getScreenWidth(@NonNull Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) (displayMetrics.widthPixels / displayMetrics.density);
    }

    /**
     * A convenience method for setting text appearance.
     *
     * @param textView a TextView which textAppearance to modify.
     * @param resId    a style resource for the text appearance.
     */
    @SuppressWarnings("deprecation")
    protected static void setTextAppearance(@NonNull TextView textView, @StyleRes int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.setTextAppearance(resId);
        } else {
            textView.setTextAppearance(textView.getContext(), resId);
        }
    }

    /**
     * Determine if the current UI Mode is Night Mode.
     *
     * @param context Context to get the configuration.
     * @return true if the night mode is enabled, otherwise false.
     */
    protected static boolean isNightMode(@NonNull Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }
}
