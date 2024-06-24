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
 * TODO: document your custom view class.
 */
public class TitleDescCard extends CardView {
    private final static String DEFAULT_TEXT = "";
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

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.layout_title_desc_card, this, true);

        this.titleView = findViewById(R.id.title);
        this.descView = findViewById(R.id.description);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleDescCard);
            String title = typedArray.getString(R.styleable.TitleDescCard_title);
            String description = typedArray.getString(R.styleable.TitleDescCard_description);

            setTitle(title != null ? title : DEFAULT_TEXT);
            setDescription(description != null ? description : DEFAULT_TEXT);

            typedArray.recycle();
        }

    }

    public void setTitle(String title) {
        if (null == title || title.isEmpty()) {
            titleView.setVisibility(GONE);
        } else {
            titleView.setVisibility(VISIBLE);
            titleView.setText(title);
        }
    }

    public void setDescription(String description) {
        if (null == description || description.isEmpty()) {
            descView.setVisibility(GONE);
        } else {
            descView.setVisibility(VISIBLE);
            descView.setText(description);
        }
    }
}