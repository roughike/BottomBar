package com.roughike.bottombar;

import android.support.annotation.IdRes;

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
public interface OnMenuTabLongClickListener {

    /**
     * The method being called when {@link BottomBarTab} is long clicked.
     *
     * This listener is fired whenever {@link BottomBarTab} is long clicked.
     *
     * @param menuItemId the tab's id that was long clicked.
     * @param isActiveTabLongClicked whether the active tab is long clicked.
     *
     */
    void onMenuTabLongClicked(@IdRes int menuItemId, boolean isActiveTabLongClicked);
}
