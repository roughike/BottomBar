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

package com.roughike.bottombar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.lang.ref.SoftReference;

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
public class BottomBarBadge extends View {
//    private int count;
    private boolean isVisible = false;
    private long animationDuration = 150;
    private boolean autoShowAfterUnSelection = false;

//    /**
//     * Set the unread / new item / whatever count for this Badge.
//     *
//     * @param count the value this Badge should show.
//     */
//    public void setCount(int count) {
//        this.count = count;
//         setText(String.valueOf(count));
//    }

//    /**
//     * Get the currently showing count for this Badge.
//     *
//     * @return current count for the Badge.
//     */
//    public int getCount() {
//        return count;
//    }

    /**
     * Controls whether you want this Badge to be shown automatically when the
     * BottomBar tab containing it is unselected.
     *
     * @param autoShowAfterUnSelection false if you don't want to this Badge reappear every time
     *                                 the BottomBar tab containing it is unselected.
     */
    public void setAutoShowAfterUnSelection(boolean autoShowAfterUnSelection) {
        this.autoShowAfterUnSelection = autoShowAfterUnSelection;
    }

    /**
     * Is this Badge automatically shown after unselecting the BottomBar tab that
     * contains it?
     *
     * @return true if this Badge is automatically shown after unselection, otherwise false.
     */
    public boolean getAutoShowAfterUnSelection() {
        return autoShowAfterUnSelection;
    }

    /**
     * Set the scale animation duration in milliseconds.
     *
     * @param duration animation duration in milliseconds.
     */
    public void setAnimationDuration(long duration) {
        this.animationDuration = duration;
    }

    /**
     * Shows the badge with a neat little scale animation.
     */
    public void show() {
        isVisible = true;
        ViewCompat.animate(this)
                .setDuration(animationDuration)
                .scaleX(1)
                .scaleY(1)
                .start();
    }

    /**
     * Hides the badge with a neat little scale animation.
     */
    public void hide() {
        isVisible = false;
        ViewCompat.animate(this)
                .setDuration(animationDuration)
                .scaleX(0)
                .scaleY(0)
                .start();
    }

    /**
     * Is this badge currently visible?
     *
     * @return true is this badge is visible, otherwise false.
     */
    public boolean isVisible() {
        return isVisible;
    }

    private SoftReference<View> mTabToAddTo;

    protected BottomBarBadge(Context context, final View tabToAddTo, // Rhyming accidentally! That's a Smoove Move!
                             int backgroundColor, int strokeColor, float strokeSize) {
        super(context);
        mTabToAddTo = new SoftReference<>(tabToAddTo);

//        setLayoutParams(new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        setGravity(Gravity.CENTER);
//        MiscUtils.setTextAppearance(this,
//                R.style.BB_BottomBarBadge_Text);

//        int three = MiscUtils.dpToPixel(context, 20);
        Drawable backgroundCircle = BadgeCircle.make(100, backgroundColor, strokeColor, strokeSize);
//        setPadding(three, three, three, three);
        setBackgroundCompat(backgroundCircle);
//
//        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @SuppressWarnings("deprecation")
//            @Override
//            public void onGlobalLayout() {
//                adjustPositionAndSize(tabToAddTo);
//                ViewTreeObserver obs = getViewTreeObserver();
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    obs.removeOnGlobalLayoutListener(this);
//                } else {
//                    obs.removeGlobalOnLayoutListener(this);
//                }
//            }
//        });
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        adjustPositionAndSize(mTabToAddTo.get());
    }

    private void adjustPositionAndSize(View tabToAddTo) {
        if (null == tabToAddTo) {
            return;
        }

        View image = tabToAddTo.findViewById(R.id.bb_bottom_bar_icon);
//        Log.v(getClass().getSimpleName(), "tab position: " + tabToAddTo.getX() + "x" + tabToAddTo.getY());
//        Log.v(getClass().getSimpleName(), "image position: " + image.getX() + "x" + image.getY());

        setX(tabToAddTo.getX() + image.getX() + image.getWidth() - getWidth() / 2);
        setTranslationY(image.getY() - getHeight() / 2);

//        setX((float) (tabToAddTo.getX() + (tabToAddTo.getWidth() / 1.75)));
//        setY(tabToAddTo.getTop());
//         setTranslationY(MiscUtils.dpToPixel(getContext(), 6));

//        Log.v(getClass().getSimpleName(), "final position: " + getX() + ", " + getTranslationY());

//        int size = Math.max(getWidth(), getHeight());
//        getLayoutParams().width = size;
//        getLayoutParams().height = size;
    }

    @SuppressWarnings("deprecation")
    private void setBackgroundCompat(Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(background);
        } else {
            setBackgroundDrawable(background);
        }
    }
}
