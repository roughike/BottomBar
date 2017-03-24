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
public interface TabSelectionInterceptor {
    /**
     * The method being called when currently visible {@link BottomBarTab} is about to change.
     * <p>
     * This listener is fired when the current {@link BottomBar} is about to change. This gives
     * an opportunity to interrupt the {@link BottomBarTab} change.
     *
     * @param oldTabId the currently visible {@link BottomBarTab}
     * @param newTabId the {@link BottomBarTab} that will be switched to
     * @return true if you want to override/stop the tab change, false to continue as normal
     */
    boolean shouldInterceptTabSelection(@IdRes int oldTabId, @IdRes int newTabId);
}
