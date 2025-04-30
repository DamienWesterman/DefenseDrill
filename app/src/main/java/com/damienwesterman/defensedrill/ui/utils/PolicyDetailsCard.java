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
 * Copyright 2024 Damien Westerman
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

package com.damienwesterman.defensedrill.ui.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import com.damienwesterman.defensedrill.R;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom implementation of {@link CardView} to display all information relevant to a Simulated
 * Attack alarm policy.
 */
public class PolicyDetailsCard extends CardView {
    private CardView policyDetailsCard;
    private LinearLayout detailsContainer;
    private TextView policyName;
    /** In order: Sunday through Saturday */
    private List<TextView> daysOfWeekActive;
    private TextView timeWindow;
    private TextView frequency;
    private SwitchCompat enabledSwitch;

    public PolicyDetailsCard(Context context) {
        super(context);
        init(context);
    }

    public PolicyDetailsCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PolicyDetailsCard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener listener) {
        this.detailsContainer.setOnClickListener(listener);
    }

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener listener) {
        this.detailsContainer.setOnLongClickListener(listener);
    }

    /**
     * Set if the policy is active or not. This does NOT affect the OnClickListeners, as they will
     * still work.
     *
     * @param isEnabled Boolean if this policy is enabled
     */
    @Override
    public void setEnabled(boolean isEnabled) {
        this.policyDetailsCard.setBackgroundColor(isEnabled ?
                getResources().getColor(R.color.field_label, getContext().getTheme()) :
                getResources().getColor(android.R.color.transparent, getContext().getTheme()));
    }

    public void setPolicyName(@NonNull String policyName) {
        this.policyName.setText(policyName);
    }

    /**
     * Set which days of the week are active. Any day not provided in the parameter will be
     * displayed as inactive.
     *
     * @param daysOfWeekActiveness Set containing Integers of which days of the week are active.
     *                             Each entry should be 0 - 6, where 0 is Sunday and 6 is Saturday.
     */
    public void setActiveDaysOfWeek(@Nullable Set<Integer> daysOfWeekActiveness) {
        // Set all to the default gray first
        this.daysOfWeekActive.forEach(dayOfWeek ->
                dayOfWeek.setTextColor(getResources()
                        .getColor(android.R.color.darker_gray, getContext().getTheme())));

        if (null != daysOfWeekActiveness) {
            daysOfWeekActiveness.forEach(dayOfWeekActiveness -> {
                if (0 <= dayOfWeekActiveness && 7 > dayOfWeekActiveness) {
                    this.daysOfWeekActive.get(dayOfWeekActiveness).setTextColor(getResources()
                            .getColor(R.color.drill_yellow, getContext().getTheme()));
                }
            });
        }
    }

    public void setTimeWindow(@NonNull String timeWindow) {
        this.timeWindow.setText(timeWindow);
    }

    public void setFrequency(@NonNull String frequency) {
        this.frequency.setText(frequency);
    }

    public void setChecked(boolean isChecked) {
        this.enabledSwitch.setChecked(isChecked);
        this.setEnabled(isChecked);
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        this.enabledSwitch.setOnCheckedChangeListener(listener);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_policy_details_card, this, true);

        this.policyDetailsCard = findViewById(R.id.policyDetailsCard);
        this.detailsContainer = findViewById(R.id.detailsContainer);
        this.policyName = findViewById(R.id.policyName);
        int[] daysOfWeekIds = {
                R.id.sundayActive, R.id.mondayActive, R.id.tuesdayActive,
                R.id.wednesdayActive, R.id.thursdayActive, R.id.fridayActive,
                R.id.saturdayActive
        };
        this.daysOfWeekActive = Arrays.stream(daysOfWeekIds)
                .mapToObj(id -> (TextView) findViewById(id))
                .collect(Collectors.toList());
        this.timeWindow = findViewById(R.id.timeWindow);
        this.frequency = findViewById(R.id.frequency);
        this.enabledSwitch = findViewById(R.id.enabledSwitch);
    }
}
