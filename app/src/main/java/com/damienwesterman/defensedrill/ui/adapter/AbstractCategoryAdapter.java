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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.damienwesterman.defensedrill.data.local.AbstractCategoryEntity;
import com.damienwesterman.defensedrill.ui.viewholder.CardViewHolder;

import com.damienwesterman.defensedrill.R;

import java.util.function.Consumer;

/**
 * RecyclerView Adapter class for use with {@link AbstractCategoryEntity} objects.
 * <br><br>
 * Each item represents one AbstractCategoryEntity, displaying the name and description in a
 * {@link com.damienwesterman.defensedrill.ui.util.TitleDescCard}. Uses {@link CardViewHolder}.
 * Allows the caller to set an onClickListener and a LongClickListener.
 */
public class AbstractCategoryAdapter extends ListAdapter<AbstractCategoryEntity, CardViewHolder> {
    /** On Click Listener that accepts an ID Long. */
    @Nullable
    private final Consumer<Long> clickListener;
    /** On Long Click Listener that accepts an ID Long. */
    @Nullable
    private final Consumer<Long> longClickListener;

    private static final DiffUtil.ItemCallback<AbstractCategoryEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<AbstractCategoryEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull AbstractCategoryEntity oldItem,
                                               @NonNull AbstractCategoryEntity newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull AbstractCategoryEntity oldItem,
                                                  @NonNull AbstractCategoryEntity newItem) {
                    return oldItem.equals(newItem);
                }
            };

    /**
     * Parameterized constructor.
     *
     * @param clickListener     Click Listener that accepts an AbstractCategory ID in the form of a
     *                          Long. Optional.
     * @param longClickListener Long Click Listener that accepts an AbstractCategory ID in the form
     *                          of a Long. Optional.
     */
    public AbstractCategoryAdapter(@Nullable Consumer<Long> clickListener,
                                   @Nullable Consumer<Long> longClickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

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
        holder.getCard().setTitle(getItem(position).getName());
        holder.getCard().setDescription(getItem(position).getDescription());
        holder.setOnClickListener(clickListener, getItem(position).getId());
        holder.setLongClickListener(longClickListener, getItem(position).getId());
    }
}
