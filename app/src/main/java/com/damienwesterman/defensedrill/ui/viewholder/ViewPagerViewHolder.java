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

package com.damienwesterman.defensedrill.ui.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.damienwesterman.defensedrill.R;

/**
 * Custom ViewHolder for use in a ViewPager2. Has a simple image at the top, a header, then a
 * description. Should be used with layout_view_pager_image_title_description_item.xml.
 */
public class ViewPagerViewHolder extends RecyclerView.ViewHolder {
    private final TextView title;
    private final TextView description;

    public ViewPagerViewHolder(@NonNull View itemView) {
        super(itemView);

        this.title = itemView.findViewById(R.id.pageTitle);
        this.description = itemView.findViewById(R.id.pageDescription);
    }

    public void setTitle(String titleText) {
        this.title.setText(titleText);
    }

    public void setDescription(String descriptionText) {
        this.description.setText(descriptionText);
    }
}
