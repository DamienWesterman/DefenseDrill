/****************************\
 *      ________________      *
 *     /  _             \     *
 *     \   \ |\   _  \  /     *
 *      \  / | \ / \  \/      *
 *      /  \ | / | /  /\      *
 *     /  _/ |/  \__ /  \     *
 *     \________________/     *
 *                            *
 \****************************/
/*
 * Copyright 2025 Damien Westerman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.damienwesterman.defensedrill.ui.common;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.damienwesterman.defensedrill.R;
import com.getkeepsafe.taptargetview.TapTarget;

/**
 * Common static utility functions for use during onboarding activities. Ensures consistent UI
 * throughout activities.
 */
public class OnboardingUtils {
    /**
     * No need for instantiation.
     */
    private OnboardingUtils() { }

    /**
     * Create a TapTarget for any view to be used during onboarding screens. This customizes the
     * TapTarget to ensure a consistent look across activities.
     * @param view          View to create a TapTarget for.
     * @param title         Title for the TapTarget.
     * @param description   Description of the TapTarget.
     * @param cancelable    true if the user is able to cancel the TapTarget.
     * @return              TapTarget with consistent customizations.
     */
    public static TapTarget createTapTarget(@NonNull View view,
                                            @NonNull String title,
                                            @NonNull String description,
                                            boolean cancelable) {
        return TapTarget.forView(view, title, description)
                .outerCircleColor(R.color.tapTargetOuterCircleColor)
                .tintTarget(false)
                .cancelable(cancelable);
    }

    /**
     * Create a TapTarget specifically for the Home button in the Toolbar to be used during
     * onboarding screens . This customizes the TapTarget to ensure a consistent look across
     * activities.
     * @param context       Activity context.
     * @param toolbar       Toolbar in the activity.
     * @param cancelable    true if the user is able to cancel the TapTarget.
     * @return              TapTarget with consistent customizations.
     */
    public static TapTarget createToolbarHomeTapTarget(@NonNull Context context,
                                                       @NonNull Toolbar toolbar,
                                                       boolean cancelable) {
        return TapTarget.forToolbarMenuItem(
                        toolbar,
                        R.id.homeButton,
                        "Home",
                        context.getString(R.string.onboarding_home_navigation_description))
                .outerCircleColor(R.color.tapTargetOuterCircleColor)
                .tintTarget(false)
                .cancelable(cancelable);
    }
}
