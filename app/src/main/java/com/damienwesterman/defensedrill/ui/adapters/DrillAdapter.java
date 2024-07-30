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

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.Drill;
import com.damienwesterman.defensedrill.ui.utils.CardClickListener;
import com.damienwesterman.defensedrill.ui.utils.LongCardClickListener;
import com.damienwesterman.defensedrill.ui.view_holders.CardViewHolder;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView Adapter class for use with {@link Drill} objects.
 * <br><br>
 * Each item represents one Drill, displaying the name and last drilled date in a
 * {@link com.damienwesterman.defensedrill.ui.utils.TitleDescCard}. Uses {@link CardViewHolder}.
 * Allows the caller to set an onClickListener and a LongClickListener.
 */
public class DrillAdapter extends RecyclerView.Adapter<CardViewHolder> {
    private final List<Drill> drills;
    CardClickListener clickListener;
    LongCardClickListener longClickListener;

    public DrillAdapter(@NonNull List<Drill> drills, CardClickListener clickListener,
                        LongCardClickListener longClickListener) {
        this.drills = drills;
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
        holder.getCard().setTitle(drills.get(position).getName());
        Date drilledDate = new Date(drills.get(position).getLastDrilled());
        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        String lastDrilled = "Last Drilled: " + dateFormatter.format(drilledDate);
        holder.getCard().setDescription(lastDrilled);
        holder.setOnClickListener(clickListener, drills.get(position).getId());
        holder.setLongClickListener(longClickListener, drills.get(position).getId());
    }

    @Override
    public int getItemCount() {
        return drills.size();
    }
}
