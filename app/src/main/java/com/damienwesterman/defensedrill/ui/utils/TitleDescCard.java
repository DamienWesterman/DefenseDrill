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

package com.damienwesterman.defensedrill.ui.utils;

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

    /**
     * Helper method to allow the setting of title and description from XML.
     *
     * @param context   Context.
     * @param attrs     AttributeSet.
     */
    private void init(Context context, AttributeSet attrs) {
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