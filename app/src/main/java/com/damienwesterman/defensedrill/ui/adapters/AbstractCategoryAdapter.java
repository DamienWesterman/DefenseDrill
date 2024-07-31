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

package com.damienwesterman.defensedrill.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.damienwesterman.defensedrill.data.AbstractCategoryEntity;
import com.damienwesterman.defensedrill.ui.utils.CardClickListener;
import com.damienwesterman.defensedrill.ui.utils.LongCardClickListener;
import com.damienwesterman.defensedrill.ui.view_holders.CardViewHolder;

import java.util.List;

import com.damienwesterman.defensedrill.R;

/**
 * RecyclerView Adapter class for use with {@link AbstractCategoryEntity} objects.
 * <br><br>
 * Each item represents one AbstractCategoryEntity, displaying the name and description in a
 * {@link com.damienwesterman.defensedrill.ui.utils.TitleDescCard}. Uses {@link CardViewHolder}.
 * Allows the caller to set an onClickListener and a LongClickListener.
 */
public class AbstractCategoryAdapter extends RecyclerView.Adapter<CardViewHolder> {
    private final List<? extends AbstractCategoryEntity> categories;
    final CardClickListener clickListener;
    final LongCardClickListener longClickListener;

    public AbstractCategoryAdapter(@NonNull List<? extends AbstractCategoryEntity> categories, CardClickListener clickListener,
                                   LongCardClickListener longClickListener) {
        this.categories = categories;
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
