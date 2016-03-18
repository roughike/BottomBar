package com.roughike.bottombar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.MenuRes;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.PopupMenu;

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
    protected static int getColor(Context context, int color) {
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, tv, true);
        return tv.data;
    }

    /**
     * Converts dps to pixels nicely.
     *
     * @param context the Context for getting the resources
     * @param dp      dimension in dps
     * @return dimension in pixels
     */
    protected static int dpToPixel(Context context, float dp) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (dp * (metrics.densityDpi / 160f));
    }

    /**
     * Returns screen width.
     *
     * @param context Context to get resources and device specific display metrics
     * @return screen width
     */
    protected static int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) (displayMetrics.widthPixels / displayMetrics.density);
    }

    /**
     * A hacky method for inflating menus from xml resources to an array
     * of BottomBarTabs.
     *
     * @param activity the activity context for retrieving the MenuInflater.
     * @param menuRes  the xml menu resource to inflate
     * @return an Array of BottomBarTabs.
     */
    protected static BottomBarTab[] inflateMenuFromResource(Activity activity, @MenuRes int menuRes) {
        // A bit hacky, but hey hey what can I do
        PopupMenu popupMenu = new PopupMenu(activity, null);
        Menu menu = popupMenu.getMenu();
        activity.getMenuInflater().inflate(menuRes, menu);

        int menuSize = menu.size();
        BottomBarTab[] tabs = new BottomBarTab[menuSize];

        for (int i = 0; i < menuSize; i++) {
            MenuItem item = menu.getItem(i);
            BottomBarTab tab = new BottomBarTab(item.getIcon(),
                    String.valueOf(item.getTitle()));
            tab.id = item.getItemId();
            tabs[i] = tab;
        }

        return tabs;
    }

    /**
     * Animate a background color change. Uses Circular Reveal if supported,
     * otherwise crossfades the background color in.
     *
     * @param clickedView    the view that was clicked for calculating the start position
     *                       for the Circular Reveal.
     * @param backgroundView the currently showing background color.
     * @param bgOverlay      the overlay view for the new background color that will be
     *                       animated in.
     * @param newColor       the new color.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected static void animateBGColorChange(View clickedView, final View backgroundView,
                                               final View bgOverlay, final int newColor) {
        int centerX = (int) (clickedView.getX() + (clickedView.getMeasuredWidth() / 2));
        int centerY = clickedView.getMeasuredHeight() / 2;
        int finalRadius = backgroundView.getWidth();

        backgroundView.clearAnimation();
        bgOverlay.clearAnimation();

        Animator circularReveal;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            circularReveal = ViewAnimationUtils
                    .createCircularReveal(bgOverlay, centerX, centerY, 0, finalRadius);
        } else {
            bgOverlay.setAlpha(0);
            circularReveal = ObjectAnimator.ofFloat(bgOverlay, "alpha", 0, 1);
        }

        circularReveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onCancel();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onCancel();
            }

            private void onCancel() {
                backgroundView.setBackgroundColor(newColor);
                bgOverlay.setVisibility(View.INVISIBLE);
                bgOverlay.setAlpha(1);
            }
        });

        bgOverlay.setBackgroundColor(newColor);
        bgOverlay.setVisibility(View.VISIBLE);
        circularReveal.start();
    }
}
