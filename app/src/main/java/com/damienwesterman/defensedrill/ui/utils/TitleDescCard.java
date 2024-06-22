package com.damienwesterman.defensedrill.ui.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
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
    private CardView cardView;
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

        this.cardView = findViewById(R.id.titleDescCard);
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
        titleView.setText(title);
    }

    public void setDescription(String description) {
        descView.setText(description);
    }
}