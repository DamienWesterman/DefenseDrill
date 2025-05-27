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

package com.damienwesterman.defensedrill.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.damienwesterman.defensedrill.data.local.AbstractCategoryEntity;
import com.damienwesterman.defensedrill.ui.util.CardClickListener;
import com.damienwesterman.defensedrill.ui.util.CardLongClickListener;
import com.damienwesterman.defensedrill.ui.viewHolder.CardViewHolder;

import java.util.List;

import com.damienwesterman.defensedrill.R;

import lombok.RequiredArgsConstructor;

/**
 * RecyclerView Adapter class for use with {@link AbstractCategoryEntity} objects.
 * <br><br>
 * Each item represents one AbstractCategoryEntity, displaying the name and description in a
 * {@link com.damienwesterman.defensedrill.ui.util.TitleDescCard}. Uses {@link CardViewHolder}.
 * Allows the caller to set an onClickListener and a LongClickListener.
 */
@RequiredArgsConstructor
public class AbstractCategoryAdapter extends RecyclerView.Adapter<CardViewHolder> {
    @NonNull
    private final List<? extends AbstractCategoryEntity> categories;
    @Nullable
    private final CardClickListener clickListener;
    @Nullable
    private final CardLongClickListener longClickListener;

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.layout_card_item, parent, false
        );

        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        holder.getCard().setTitle(categories.get(position).getName());
        holder.getCard().setDescription(categories.get(position).getDescription());
        holder.setOnClickListener(clickListener, categories.get(position).getId());
        holder.setLongClickListener(longClickListener, categories.get(position).getId());
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
