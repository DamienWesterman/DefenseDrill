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

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.ui.view.TitleDescCard;
import com.damienwesterman.defensedrill.ui.viewholder.CardViewHolder;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * RecyclerView Adapter class for use with {@link Drill} objects.
 * <br><br>
 * Each item represents one Drill, displaying the name and last drilled date in a
 * {@link TitleDescCard}. Uses {@link CardViewHolder}.
 * Allows the caller to set an onClickListener and a LongClickListener.
 */
public class DrillAdapter extends ListAdapter<Drill, CardViewHolder> {
    @Nullable
    private final Consumer<Long> clickListener;
    @Nullable
    private final Consumer<Long> longClickListener;

    private static final DiffUtil.ItemCallback<Drill> DIFF_CALLBACK = new DiffUtil.ItemCallback<Drill>() {
        @Override
        public boolean areItemsTheSame(@NonNull Drill oldItem, @NonNull Drill newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Drill oldItem, @NonNull Drill newItem) {
            return oldItem.equals(newItem);
        }
    };

    /**
     * Parameterized Constructor.
     *
     * @param clickListener     Click Listener that accepts a Drill ID in the form of a Long.
     *                          Optional.
     * @param longClickListener Long Click Listener that accepts a Drill ID in the form of a Long.
     *                          Optional.
     */
    public DrillAdapter(@Nullable Consumer<Long> clickListener,
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

        long lastDrilledLong = getItem(position).getLastDrilled();
        String lastDrilled;
        if (0 < lastDrilledLong) {
            Date drilledDate = new Date(lastDrilledLong);
            DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
            lastDrilled = "Last Drilled: " + dateFormatter.format(drilledDate);
        } else {
            lastDrilled = "Last Drilled: -";
        }

        holder.getCard().setDescription(lastDrilled);
        holder.setOnClickListener(clickListener, getItem(position).getId());
        holder.setLongClickListener(longClickListener, getItem(position).getId());
    }
}
