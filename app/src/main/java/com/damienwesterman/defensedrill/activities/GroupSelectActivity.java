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
import com.damienwesterman.defensedrill.database.GroupEntity;
import com.damienwesterman.defensedrill.view_models.GroupSelectViewModel;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * TODO doc comments
 */
public class GroupSelectActivity extends AppCompatActivity {
    GroupSelectViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_select);

        viewModel = new ViewModelProvider(this).get(GroupSelectViewModel.class);

        Executors.newSingleThreadExecutor().execute(() -> {
            List<GroupEntity> groups = viewModel.getGroups();
            String groupNames = groups.stream().map(GroupEntity::getName).collect(Collectors.joining("\n"));

            TextView textView = findViewById(R.id.textView);
            runOnUiThread(() -> textView.setText(groupNames));
        });
    }
}