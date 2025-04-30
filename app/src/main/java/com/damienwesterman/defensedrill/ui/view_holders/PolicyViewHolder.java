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

package com.damienwesterman.defensedrill.ui.view_holders;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.WeeklyHourPolicyEntity;
import com.damienwesterman.defensedrill.ui.utils.PolicyDetailsCard;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lombok.Getter;

// TODO: Doc comments
@Getter
public class PolicyViewHolder extends RecyclerView.ViewHolder {
    // TODO: Implement
    private final PolicyDetailsCard card;

    public PolicyViewHolder(@NonNull View view) {
        super(view);

        card = view.findViewById(R.id.cardViewHolder);
    }

    // TODO: Doc comments
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
        int startingHour = policies.get(0).getWeeklyHour() % 24;
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

    // TODO: Doc comments, consumer variables
    public void setOnClickListener(@NonNull Consumer<String> listener, String policyName) {
        card.setOnClickListener(v -> listener.accept(policyName));
    }

    // TODO: Doc comments, consumer variables
    public void setOnLongClickListener(@NonNull Consumer<String> listener, String policyName) {
        card.setOnLongClickListener(v -> {
            listener.accept(policyName);
            return true;
        });
    }

    // TODO: Doc comments, consumer variables
    public void setCheckedListener(BiConsumer<String, Boolean> listener, String policyName) {
        card.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            listener.accept(policyName, isChecked);
            card.setEnabled(isChecked);
        });
    }
}
