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
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

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
public class BottomBarBadge extends TextView {
    private int count;
    private boolean isVisible = false;
    private long animationDuration = 150;
    private boolean autoShowAfterUnSelection = false;
    private boolean autoHideOnSelection = true;

    /**
     * Set the unread / new item / whatever count for this Badge.
     *
     * @param count the value this Badge should show.
     */
    public void setCount(int count) {
        this.count = count;
        setText(String.valueOf(count));
    }

    /**
     * Get the currently showing count for this Badge.
     *
     * @return current count for the Badge.
     */
    public int getCount() {
        return count;
    }

    /**
     * Controls whether you want this Badge to be hidden automatically when the
     * BottomBar tab containing it is selected.
     *
     * @param autoHideOnSelection false if you don't want this Badge to hide every time
     *                              the BottomBar tab containing it is selected
     */
    public void setAutoHideOnSelection(boolean autoHideOnSelection) {
        this.autoHideOnSelection = autoHideOnSelection;
    }

    /**
     * Is this Badge automatically hidden after selecting the BottomBar tab that
     * contains it?
     *
     * @return true if this Badge is automatically hidden after selection, otherwise false. Default is true.
     */
    public boolean getAutoHideOnSelection() {
        return autoHideOnSelection;
    }

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

    protected BottomBarBadge(Context context, final int position, final View tabToAddTo, // Rhyming accidentally! That's a Smoove Move!
                             int backgroundColor) {
        super(context);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        setLayoutParams(params);
        setGravity(Gravity.CENTER);
        MiscUtils.setTextAppearance(this,
                R.style.BB_BottomBarBadge_Text);

        int three = MiscUtils.dpToPixel(context, 3);
        ShapeDrawable backgroundCircle = BadgeCircle.make(three * 3, backgroundColor);
        setPadding(three, three, three, three);
        setBackgroundCompat(backgroundCircle);

        final FrameLayout container = new FrameLayout(context);
        container.setLayoutParams(params);

        ViewGroup parent = (ViewGroup) tabToAddTo.getParent();
        parent.removeView(tabToAddTo);

        container.setTag(tabToAddTo.getTag());
        tabToAddTo.setTag(null);
        container.addView(tabToAddTo);
        container.addView(this);

        parent.addView(container, position);

        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                container.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                adjustPositionAndSize(tabToAddTo, position);
            }
        });
    }

    protected void adjustPosition(View tabToAddTo, int position) {
        if (position == 0) {
            setX((float) (tabToAddTo.getX() + (tabToAddTo.getWidth() / 2.75)));
        }else{
            setX((float) (tabToAddTo.getX() + (tabToAddTo.getWidth() / 1.75)));
        }
    }

    private void adjustPositionAndSize(View tabToAddTo, int position) {
        adjustPosition(tabToAddTo, position);
        setTranslationY(10);

        int size = Math.max(getWidth(), getHeight());

        ViewGroup.LayoutParams params = getLayoutParams();

        if (params.width != size || params.height != size) {
            params.width = size;
            params.height = size;
            setLayoutParams(params);
        }
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
