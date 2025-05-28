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

package com.damienwesterman.defensedrill.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.WeeklyHourPolicyEntity;
import com.damienwesterman.defensedrill.ui.viewholder.PolicyViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * RecyclerView Adapter class for use with {@link WeeklyHourPolicyEntity} objects.
 * <br><br>
 * Each item represents a policy, which is really a list of WeeklyPolicyEntity objects that share a
 * policy name and act together to span multiple days/hours.
 */
public class PolicyAdapter extends RecyclerView.Adapter<PolicyViewHolder> {
    @NonNull
    private final Map<String, List<WeeklyHourPolicyEntity>> policiesByName;
    /** This will be used as the list of items, as the policies are grouped by policy name */
    @NonNull
    private final List<String> policyNames;
    @Nullable
    private final Consumer<String> onClickListener;
    @Nullable
    private final Consumer<String> onLongClickListener;
    @Nullable
    private final BiConsumer<String, Boolean> onCheckedListener;

    public PolicyAdapter(@NonNull Map<String, List<WeeklyHourPolicyEntity>> policiesByName,
                         @Nullable Consumer<String> onClickListener,
                         @Nullable Consumer<String> onLongClickListener,
                         @Nullable BiConsumer<String, Boolean> onCheckedListener) {
        this.policiesByName = policiesByName;
        this.policyNames = new ArrayList<>(policiesByName.keySet());
        this.onClickListener = onClickListener;
        this.onLongClickListener = onLongClickListener;
        this.onCheckedListener = onCheckedListener;
    }

    @NonNull
    @Override
    public PolicyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.layout_policy_details_item, parent, false
        );
        return new PolicyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PolicyViewHolder holder, int position) {
        List<WeeklyHourPolicyEntity> weeklyPolicies = Optional.ofNullable(
                    policiesByName.get(policyNames.get(position)))
                .orElse(List.of());
        holder.setCardDetails(weeklyPolicies);
        if (null != onClickListener) {
            holder.setOnClickListener(onClickListener, policyNames.get(position));
        }
        if (null != onLongClickListener) {
        holder.setOnLongClickListener(onLongClickListener, policyNames.get(position));
        }
        if (null != onCheckedListener) {
            holder.setCheckedListener(onCheckedListener, policyNames.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return policyNames.size();
    }
}
