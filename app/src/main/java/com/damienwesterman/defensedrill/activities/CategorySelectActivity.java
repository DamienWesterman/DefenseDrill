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

package com.damienwesterman.defensedrill.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.database.CategoryEntity;
import com.damienwesterman.defensedrill.view_models.CategorySelectViewModel;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * TODO doc comments
 */
public class CategorySelectActivity extends AppCompatActivity {
    CategorySelectViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_select);

        viewModel = new ViewModelProvider(this).get(CategorySelectViewModel.class);

        // TODO: implement RecyclerView with Cards
        Executors.newSingleThreadExecutor().execute(() -> {
            List<CategoryEntity> categories = viewModel.getCategories();
            String categoryNames = categories.stream().map(CategoryEntity::getName).collect(Collectors.joining("\n"));

            TextView textView = findViewById(R.id.textView);
            runOnUiThread(() -> textView.setText(categoryNames));
        });
    }
}