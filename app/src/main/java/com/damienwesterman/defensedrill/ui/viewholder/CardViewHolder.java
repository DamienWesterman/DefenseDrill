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

package com.damienwesterman.defensedrill.ui.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.ui.view.TitleDescCard;

import java.util.function.Consumer;

import lombok.Getter;

/**
 * Custom ViewHolder for use in a RecyclerView. Uses the {@link TitleDescCard} as its view for
 * each element. Allows setting of onClickListener and onLongClickListener.
 */
@Getter
public class CardViewHolder extends RecyclerView.ViewHolder {
    private final TitleDescCard card;

    public CardViewHolder(@NonNull View view) {
        super(view);

        card = view.findViewById(R.id.cardViewHolder);

    }

    public void setOnClickListener(@Nullable Consumer<Long> clickListener, long id) {
        if (null != clickListener) {
            card.setOnClickListener(v -> clickListener.accept(id));
        }
    }

    public void setLongClickListener(@Nullable Consumer<Long> longClickListener, long id) {
        if (null != longClickListener) {
            card.setOnLongClickListener(v -> {
                longClickListener.accept(id);
                return true;
            });
        }
    }
}
