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

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

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
public class BottomBarFragment extends BottomBarItemBase {
    private String tag;
    private Class<?> clss;
    private Bundle args;
    Fragment fragment;

    /**
     * Creates a new Tab for the BottomBar.
     * @param clss a Fragment Class to be shown when this Tab is selected.
     * @param iconResource a resource for the Tab icon.
     * @param title title for the Tab.
     */
    public BottomBarFragment(Class<?> clss, @DrawableRes int iconResource, @NonNull String title) {
        this.clss = clss;
        this.iconResource = iconResource;
        this.title = title;
    }

    /**
     * Creates a new Tab for the BottomBar.
     * @param clss a Fragment Class to be shown when this Tab is selected.
     * @param icon an icon for the Tab.
     * @param title title for the Tab.
     */
    public BottomBarFragment(Class<?> clss, Drawable icon, @NonNull String title) {
        this.clss = clss;
        this.icon = icon;
        this.title = title;
    }

    /**
     * Creates a new Tab for the BottomBar.
     * @param clss a Fragment Class to be shown when this Tab is selected.
     * @param icon an icon for the Tab.
     * @param titleResource resource for the title.
     */
    public BottomBarFragment(Class<?> clss, Drawable icon, @StringRes int titleResource) {
        this.clss = clss;
        this.icon = icon;
        this.titleResource = titleResource;
    }

    /**
     * Creates a new Tab for the BottomBar.
     * @param clss a Fragment Class to be shown when this Tab is selected.
     * @param iconResource a resource for the Tab icon.
     * @param titleResource resource for the title.
     */
    public BottomBarFragment(Class<?> clss, @DrawableRes int iconResource, @StringRes int titleResource) {
        this.clss = clss;
        this.iconResource = iconResource;
        this.titleResource = titleResource;
    }

    public BottomBarFragment setTag(String tag){
        this.tag = tag;
        return this;
    }

    public BottomBarFragment setArgs(Bundle args){
        this.args = args;
        return this;
    }

    public String getTag() {
        return tag == null ? clss.getName() : tag;
    }

    public Class<?> getClss() {
        return clss;
    }

    public Bundle getArgs() {
        return args;
    }
}
