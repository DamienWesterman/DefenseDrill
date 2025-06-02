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

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.WeeklyHourPolicyEntity;
import com.damienwesterman.defensedrill.ui.viewholder.PolicyViewHolder;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * RecyclerView Adapter class for use with {@link WeeklyHourPolicyEntity} objects.
 * <br><br>
 * Each item is in the form of a Pair {@code <stringPolicyName, policyEntitiesList>} in ascending
 * order by the policy name for consistency. Each item conceptually represents a policy, which is
 * really a list of WeeklyPolicyEntity objects that share a policy name and act together to span
 * multiple days/hours.
 */
public class PolicyAdapter
        extends ListAdapter<Pair<String, List<WeeklyHourPolicyEntity>>, PolicyViewHolder> {
    @Nullable
    private final Consumer<String> onClickListener;
    @Nullable
    private final Consumer<String> onLongClickListener;
    @Nullable
    private final BiConsumer<String, Boolean> onCheckedListener;

    private static final DiffUtil.ItemCallback<Pair<String, List<WeeklyHourPolicyEntity>>> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Pair<String, List<WeeklyHourPolicyEntity>>>() {
                @Override
                public boolean areItemsTheSame(@NonNull Pair<String, List<WeeklyHourPolicyEntity>> oldItem,
                                               @NonNull Pair<String, List<WeeklyHourPolicyEntity>> newItem) {
                    // Compare policy names
                    return oldItem.first.equals(newItem.first);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Pair<String, List<WeeklyHourPolicyEntity>> oldItem,
                                                  @NonNull Pair<String, List<WeeklyHourPolicyEntity>> newItem) {
                    if (oldItem.second.isEmpty() || newItem.second.isEmpty()) {
                        // This shouldn't really happen
                        return false;
                    }

                    if (oldItem.second.size() != newItem.second.size()) {
                        return false;
                    }

                    for (int i = 0; i < oldItem.second.size(); i++) {
                        // Each item in each list should line up and be equal
                        if (!oldItem.second.get(i).equals(newItem.second.get(i))) {
                            return false;
                        }
                    }

                    return true;
                }
            };

    public PolicyAdapter(@Nullable Consumer<String> onClickListener,
                         @Nullable Consumer<String> onLongClickListener,
                         @Nullable BiConsumer<String, Boolean> onCheckedListener) {
        super(DIFF_CALLBACK);
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
        List<WeeklyHourPolicyEntity> weeklyPolicies = getItem(position).second;
        holder.setCardDetails(weeklyPolicies);

        if (weeklyPolicies.isEmpty()) {
            // Can't retrieve a name
            return;
        }

        String policyName = weeklyPolicies.get(0).getPolicyName();
        if (null != onClickListener) {
            holder.setOnClickListener(onClickListener, policyName);
        }
        if (null != onLongClickListener) {
            holder.setOnLongClickListener(onLongClickListener, policyName);
        }
        if (null != onCheckedListener) {
            holder.setCheckedListener(onCheckedListener, policyName);
        }
    }
}
