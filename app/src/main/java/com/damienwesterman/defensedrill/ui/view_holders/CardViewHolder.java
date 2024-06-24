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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.ui.utils.CardClickListener;
import com.damienwesterman.defensedrill.ui.utils.TitleDescCard;

/**
 * TODO: Doc comments
 */
public class CardViewHolder extends RecyclerView.ViewHolder {
    private final TitleDescCard card;

    public CardViewHolder(@NonNull View view) {
        super(view);

        card = view.findViewById(R.id.cardViewHolder);

    }

    public TitleDescCard getCard() {
        return card;
    }

    public void setOnClickListener(CardClickListener clickListener, long id) {
        card.setOnClickListener(v -> clickListener.onCardClick(id));
    }
}
