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

package com.damienwesterman.defensedrill.ui.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.damienwesterman.defensedrill.R;

/**
 * Custom implementation of {@link CardView}. Has a title line and a description line in a Card.
 * Allows setting of title and description in XML layout file using:
 * <ul>
 *     <li>{@code app:title="string"}</li>
 *     <li>{@code app:description="string"}</li>
 * </ul>
 */
public class TitleDescCard extends CardView {
    private TextView titleView;
    private TextView descView;

    public TitleDescCard(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public TitleDescCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TitleDescCard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void setTitle(@Nullable String title) {
        if (null == title || title.isEmpty()) {
            titleView.setVisibility(GONE);
        } else {
            titleView.setVisibility(VISIBLE);
            titleView.setText(title);
        }
    }

    public void setDescription(@Nullable String description) {
        if (null == description || description.isEmpty()) {
            descView.setVisibility(GONE);
        } else {
            descView.setVisibility(VISIBLE);
            descView.setText(description);
        }
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.layout_title_desc_card, this, true);

        this.titleView = findViewById(R.id.title);
        this.descView = findViewById(R.id.description);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleDescCard);
            String title = typedArray.getString(R.styleable.TitleDescCard_title);
            String description = typedArray.getString(R.styleable.TitleDescCard_description);

            setTitle(title);
            setDescription(description);

            typedArray.recycle();
        }

    }
}