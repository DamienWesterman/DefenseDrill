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
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.ui.viewHolder.CheckBoxListViewHolder;

import java.util.List;
import java.util.function.BiConsumer;

import lombok.RequiredArgsConstructor;

/**
 * RecyclerView Adapter class for use with {@link Drill} objects.
 * <br><br>
 * Basically a checklist denoting if the Drill is known or not. Allows setting on the
 * OnCheckedChangeListener via BiConsumer.
 */
@RequiredArgsConstructor
public class UnlockDrillAdapter extends RecyclerView.Adapter<CheckBoxListViewHolder> {
    @NonNull
    private final List<Drill> drills;
    /**
     * Callback for when the check box is checked. Passes in the drill being checked and boolean
     * whether it is checked or not.
     */
    @Nullable
    private final BiConsumer<Drill, Boolean> listener;

    @NonNull
    @Override
    public CheckBoxListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.layout_checked_item, parent, false
        );

        return new CheckBoxListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckBoxListViewHolder holder, int position) {
        Drill drill = drills.get(position);

        holder.setText(drill.getName());
        holder.setChecked(drill.isKnownDrill());
        if (null != listener) {
            holder.setOnCheckedListener(
                    (view, isChecked) -> listener.accept(drill, isChecked));
        }
    }

    @Override
    public void onViewRecycled(@NonNull CheckBoxListViewHolder holder) {
        /*
        Need to do this because of how views are recycled, the old listener is still active when we
        begin to use it for a new drill, causing the onCheckedListener to trigger inaccurately.
         */
        holder.setOnCheckedListener(null);

        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return drills.size();
    }
}
