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
import com.damienwesterman.defensedrill.ui.view_holders.AbstractCategoryViewHolder;

import java.util.List;

import com.damienwesterman.defensedrill.R;

/**
 * TODO: Doc comments
 */
public class AbstractCategoryAdapter<T extends AbstractCategoryEntity> extends RecyclerView.Adapter<AbstractCategoryViewHolder> {
    private final List<T> categories;
    CardClickListener clickListener;


    public AbstractCategoryAdapter(List<T> categories, CardClickListener clickListener) {
        this.categories = categories;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public AbstractCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.layout_abstract_category_item_card, parent, false
        );

        return new AbstractCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractCategoryViewHolder holder, int position) {
        holder.getNameView().setText(categories.get(position).getName());
        holder.getDescriptionView().setText(categories.get(position).getDescription());
        holder.setOnClickListener(clickListener, categories.get(position).getId());
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
