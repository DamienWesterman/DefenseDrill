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

package com.damienwesterman.defensedrill.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.WeeklyHourPolicyEntity;
import com.damienwesterman.defensedrill.ui.view_holders.PolicyViewHolder;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import lombok.RequiredArgsConstructor;

// TODO: Doc comments
@RequiredArgsConstructor
public class PolicyAdapter extends RecyclerView.Adapter<PolicyViewHolder> {
    @NonNull
    private final Map<String, WeeklyHourPolicyEntity> policiesByName;
    @NonNull
    private final Consumer<String> onClickListener;
    @Nonnull
    private final Consumer<String> onLongClickListener;
    @NonNull
    private final BiConsumer<String, Boolean> onCheckedListener;

    // TODO: Implement
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
        // TODO: Properly implement
        holder.setCardDetails(List.of());
        holder.setOnClickListener(onClickListener, "HELLO");
        holder.setOnLongClickListener(onLongClickListener, "HELLO");
        holder.setCheckedListener(onCheckedListener, "HELLO");
    }

    @Override
    public int getItemCount() {
        // TODO: properly implement
        return 5;
    }
}
