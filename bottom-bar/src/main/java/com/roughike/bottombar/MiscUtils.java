package com.roughike.bottombar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.util.Xml;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

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
     * Converts dps to pixels nicely to floats.
     *
     * @param context the Context for getting the resources
     * @param dp      dimension in dps
     * @return dimension in pixels
     */
    protected static float dpToPixelf(Context context, float dp) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (dp * (metrics.densityDpi / 160f));
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
    protected static BottomBarTab[] inflateMenuFromResource(Activity activity, int menuRes) {
        List<BottomBarTab> list = new ArrayList<>();

        try {
            final XmlResourceParser parser = activity.getResources().getLayout(menuRes);
            AttributeSet attrs = Xml.asAttributeSet(parser);

            String tagName;
            int eventType = parser.getEventType();
            boolean lookingForEndOfUnknownTag = false;
            String unknownTagName = null;

            do {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                    if (tagName.equals("menu")) {
                        eventType = parser.next();
                        break;
                    }
                    throw new RuntimeException("Expecting menu, got " + tagName);
                }
                eventType = parser.next();
            } while (eventType != XmlPullParser.END_DOCUMENT);

            boolean reachedEndOfMenu = false;
            MenuParser menuParser = new MenuParser();

            while (!reachedEndOfMenu) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (lookingForEndOfUnknownTag) {
                            break;
                        }
                        tagName = parser.getName();
                        if (tagName.equals("item")) {
                            menuParser.readItem(activity, attrs);
                        } else {
                            lookingForEndOfUnknownTag = true;
                            unknownTagName = tagName;
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        tagName = parser.getName();
                        if (lookingForEndOfUnknownTag && tagName.equals(unknownTagName)) {
                            lookingForEndOfUnknownTag = false;
                            unknownTagName = null;
                        } else if (tagName.equals("item")) {
                            if(menuParser.hasItem()) {
                                MenuParser.MenuItem item = menuParser.pull();
                                BottomBarTab tab = new BottomBarTab(item.getItemIconResId(), String.valueOf(item.getItemTitle()));
                                tab.id = item.getItemId();
                                tab.color = item.getItemColor();
                                list.add(tab);
                            }
                        } else if (tagName.equals("menu")) {
                            reachedEndOfMenu = true;
                        }
                        break;

                    case XmlPullParser.END_DOCUMENT:
                        throw new RuntimeException("Unexpected end of document");
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            return null;
        }

        if (list.size() > 0) {
            return list.toArray(new BottomBarTab[list.size()]);
        } else {
            return new BottomBarTab[0];
        }
    }

    /**
     * A method for animating width for the tabs.
     * @param tab tab to animate.
     * @param start starting width.
     * @param end final width after animation.
     */
    protected static void resizeTab(final View tab, float start, float end) {
        ValueAnimator animator = ValueAnimator.ofFloat(start, end);
        animator.setDuration(150);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tab.getLayoutParams();
                if (params == null) return;

                params.width = Math.round((float) animator.getAnimatedValue());
                tab.setLayoutParams(params);
            }
        });
        animator.start();
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
        int centerX = (int) (ViewCompat.getX(clickedView) + (clickedView.getMeasuredWidth() / 2));
        int centerY = clickedView.getMeasuredHeight() / 2;
        int finalRadius = backgroundView.getWidth();

        backgroundView.clearAnimation();
        bgOverlay.clearAnimation();

        Object animator;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!bgOverlay.isAttachedToWindow()) {
                return;
            }

            animator = ViewAnimationUtils
                    .createCircularReveal(bgOverlay, centerX, centerY, 0, finalRadius);
        } else {
            ViewCompat.setAlpha(bgOverlay, 0);
            animator = ViewCompat.animate(bgOverlay).alpha(1);
        }

        if (animator instanceof ViewPropertyAnimatorCompat) {
            ((ViewPropertyAnimatorCompat) animator).setListener(new ViewPropertyAnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(View view) {
                    onCancel();
                }

                @Override
                public void onAnimationCancel(View view) {
                    onCancel();
                }

                private void onCancel() {
                    backgroundView.setBackgroundColor(newColor);
                    bgOverlay.setVisibility(View.INVISIBLE);
                    ViewCompat.setAlpha(bgOverlay, 1);
                }
            }).start();
        } else if (animator != null) {
            ((Animator) animator).addListener(new AnimatorListenerAdapter() {
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
                    ViewCompat.setAlpha(bgOverlay, 1);
                }
            });

            ((Animator) animator).start();
        }

        bgOverlay.setBackgroundColor(newColor);
        bgOverlay.setVisibility(View.VISIBLE);
    }

    /**
     * A convenience method for setting text appearance.
     *
     * @param textView a TextView which textAppearance to modify.
     * @param resId    a style resource for the text appearance.
     */
    @SuppressWarnings("deprecation")
    protected static void setTextAppearance(TextView textView, int resId) {
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
    protected static boolean isNightMode(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }
}
