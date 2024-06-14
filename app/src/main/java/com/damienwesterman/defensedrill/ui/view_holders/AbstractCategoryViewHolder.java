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

package com.damienwesterman.defensedrill.ui.view_holders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.ui.utils.CardClickListener;

/**
 * TODO: Doc comments
 */
public class AbstractCategoryViewHolder extends RecyclerView.ViewHolder {
    private final TextView nameView;
    private final TextView descriptionView;

    public AbstractCategoryViewHolder(@NonNull View view) {
        super(view);

        nameView = view.findViewById(R.id.abstractCategoryName);
        descriptionView = view.findViewById(R.id.abstractCategoryDescription);

    }

    public TextView getNameView() {
        return nameView;
    }

    public TextView getDescriptionView() {
        return descriptionView;
    }

    public void setOnClickListener(CardClickListener clickListener, long id) {
        CardView card = super.itemView.findViewById(R.id.abstractCategoryCard);
        card.setOnClickListener(v -> clickListener.onCardClick(id));
    }
}
