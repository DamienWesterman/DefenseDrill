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

package com.damienwesterman.defensedrill.ui.view_holders;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.WeeklyHourPolicyEntity;
import com.damienwesterman.defensedrill.ui.utils.PolicyDetailsCard;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lombok.Getter;

/**
 * Custom ViewHolder for use in a RecyclerView. Uses the {@link PolicyDetailsCard} as its view for
 * each element. Allows setting of onClickListener, onLongClickListener, and onCheckedListener.
 */
@Getter
public class PolicyViewHolder extends RecyclerView.ViewHolder {
    private final PolicyDetailsCard card;

    public PolicyViewHolder(@NonNull View view) {
        super(view);

        card = view.findViewById(R.id.cardViewHolder);
    }

    /**
     * Fill the details of the card with information from the list of policies. The policies should
     * all be part of the same policy, denoted by the same policy name.
     *
     * @param policies List of WeeklyHourPolicyEntity objects that belong to the same policy name.
     */
    public void setCardDetails(@NonNull List<WeeklyHourPolicyEntity> policies) {
        if (policies.isEmpty()) {
            throw new RuntimeException("setCardDetails() policies are empty!");
        }

        // Any of the policies should have the same common information, so just use the first one
        WeeklyHourPolicyEntity modelPolicy = policies.get(0);
        card.setChecked(modelPolicy.isActive());
        card.setPolicyName(modelPolicy.getPolicyName());
        card.setFrequency(itemView.getResources()
                // - 1 because of the default NO_ATTACKS frequency
                .getStringArray(R.array.frequency_options)[modelPolicy.getFrequency().ordinal() - 1]);

        // Set Time Window
        policies.sort(Comparator.comparingInt(WeeklyHourPolicyEntity::getWeeklyHour));
        int startingHour = modelPolicy.getWeeklyHour() % 24;
        /*
         Now we iterate through the sorted list of policies until we find the first one that is not
         contiguous.
         */
        int endingHour = startingHour;
        for (WeeklyHourPolicyEntity policy : policies) {
            if ((policy.getWeeklyHour() % 24) > (endingHour + 1)) {
                // We have found the non-contiguous policy, so the previous ending hour is correct
                break;
            }
            endingHour = policy.getWeeklyHour() % 24;
        }
        // Make sure that we include the last full hour
        endingHour += 1;
        String timeWindow = itemView.getResources().getStringArray(R.array.daily_hours)[startingHour] +
                " - " +
                itemView.getResources().getStringArray(R.array.daily_hours)[endingHour];
        card.setTimeWindow(timeWindow);

        card.setActiveDaysOfWeek(policies.stream()
                .map(policy -> policy.getWeeklyHour() / 24)
                .collect(Collectors.toSet()));
    }

    /**
     * Set the onClickListener for the card.
     *
     * @param listener      Consumer that acts as the onClickListener. Accepts the policyName of the
     *                      policy that was clicked.
     * @param policyName    Name of the policy being clicked.
     */
    public void setOnClickListener(@NonNull Consumer<String> listener, @NonNull String policyName) {
        card.setOnClickListener(v -> listener.accept(policyName));
    }

    /**
     * Set the onLongClickListener for the card.
     *
     * @param listener      Consumer that acts as the onLongClickListener. Accepts the policyName of
     *                      the policy that was clicked.
     * @param policyName    Name of the policy being clicked.
     */
    public void setOnLongClickListener(@NonNull Consumer<String> listener, @NonNull String policyName) {
        card.setOnLongClickListener(v -> {
            listener.accept(policyName);
            return true;
        });
    }

    /**
     * Set the onCheckedListener for when the radio button in the card denoting activeness changes.
     *
     * @param listener      BiConsumer that acts as the onCheckedChangeListener. Accepts the
     *                      policyName of the policy that was clicked as well as a boolean if it is
     *                      checked.
     * @param policyName    Name of the policy being clicked.
     */
    public void setCheckedListener(@NonNull BiConsumer<String, Boolean> listener,
                                   @NonNull String policyName) {
        card.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            listener.accept(policyName, isChecked);
            card.setEnabled(isChecked);
        });
    }
}
