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

import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

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
public class BadgeCircle {
    /**
     * Creates a new circle for the Badge background.
     *
     * @param size  the width and height for the circle
     * @param color the color for the circle
     * @param strokeWidth
     * @return a nice and adorable circle.
     */
    protected static Drawable make(int size, int color, int strokeColor, final float strokeWidth) {
        ShapeDrawable indicator = new ShapeDrawable(new OvalShape());
        indicator.setIntrinsicWidth(size);
        indicator.setIntrinsicHeight(size);
        indicator.getPaint().setStyle(Paint.Style.FILL);
        indicator.getPaint().setColor(color);

        if (strokeColor != 0 && strokeWidth > 0) {
            ShapeDrawable indicator2 = new ShapeDrawable(new OvalShape());
            indicator2.setIntrinsicWidth(size);
            indicator2.setIntrinsicHeight(size);
            indicator2.getPaint().setColor(strokeColor);
            indicator2.getPaint().setStrokeWidth(strokeWidth);
            indicator2.getPaint().setStyle(Paint.Style.STROKE);

            int s = (int) (strokeWidth / 2);
            int s2 = (int) (strokeWidth);
            final LayerDrawable drawable = new LayerDrawable(new Drawable[]{indicator, indicator2});
            drawable.setLayerInset(0, s2, s2, s2, s2);
            drawable.setLayerInset(1, s, s, s, s);
            return drawable;
        } else {
            return indicator;
        }
    }
}
