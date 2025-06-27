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
/*
 * Copyright 2025 Damien Westerman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.damienwesterman.defensedrill.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.common.Constants;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.domain.CheckPhoneInternetConnection;
import com.damienwesterman.defensedrill.ui.common.CommonPopups;
import com.damienwesterman.defensedrill.common.OperationCompleteCallback;
import com.damienwesterman.defensedrill.ui.common.OnboardingUtils;
import com.damienwesterman.defensedrill.ui.common.UiUtils;
import com.damienwesterman.defensedrill.ui.viewmodel.WebDrillApiViewModel;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Screen to allow user to select from network related actions such as logging in or out to the
 * server, downloading, and updating drills from the server.
 */
@AndroidEntryPoint
public class WebDrillOptionsActivity extends AppCompatActivity {
    private LinearLayout rootView;
    @Inject
    SharedPrefs sharedPrefs;
    @Inject
    CommonPopups commonPopups;
    @Inject
    CheckPhoneInternetConnection internetConnection;

    private WebDrillApiViewModel viewModel;
    private Context context;

    // =============================================================================================
    // Activity Creation Methods
    // =============================================================================================
    /**
     * Start the WebDrillOptionsActivity.
     *
     * @param context   Context.
     */
    public static void startActivity(@NonNull Context context) {
        Intent intent = new Intent(context, WebDrillOptionsActivity.class);
        context.startActivity(intent);
    }

    /**
     * Create an intent designed to launch the WebDrillOptionsActivity.
     *
     * @param context   Context.
     * @return          Intent that can be used to launch WebDrillOptionsActivity.
     */
    public static Intent createIntentToStartActivity(@NonNull Context context) {
        return new Intent(context, WebDrillOptionsActivity.class);
    }

    /**
     * Start the WebDrillOptionsActivity in the Onboarding state.
     *
     * @param context   Context.
     */
    public static void startOnboardingActivity(@NonNull Context context) {
        Intent intent = new Intent(context, WebDrillOptionsActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_START_ONBOARDING, "");
        context.startActivity(intent);
    }

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_drill_options);

        // Modify Toolbar
        Toolbar appToolbar = findViewById(R.id.appToolbar);
        appToolbar.setTitle("Web Drill Options");
        setSupportActionBar(appToolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rootView = findViewById(R.id.activityWebDrillOptions);
        context = this;

        viewModel = new ViewModelProvider(this).get(WebDrillApiViewModel.class);

        if (getIntent().hasExtra(Constants.INTENT_EXTRA_START_ONBOARDING)) {
            /*
            We need to wait for the toolbar to be finished loading before calling onboarding, as we
            want to access one of the buttons.
             */
            appToolbar.post(this::startOnboarding);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (R.id.homeButton == item.getItemId()) {
            HomeActivity.startActivity(this);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // =============================================================================================
    // OnClickListener Methods
    // =============================================================================================
    public void onCardClick(View view) {
        int cardId = view.getId();
        if (R.id.downloadFromDatabaseCard == cardId) {
            handleDownloadDrills();
        } else if (R.id.loginCard == cardId) {
            commonPopups.displayLoginPopup(new OperationCompleteCallback() {
                @Override
                public void onSuccess() {
                    // On Login Success
                    UiUtils.displayDismissibleSnackbar(rootView, "Login Successful!");
                }

                @Override
                public void onFailure(@NonNull String error) {
                    // On Login Failure
                    UiUtils.displayDismissibleSnackbar(rootView, error);
                }
            });
        } else if (R.id.logoutCard == cardId) {
            logoutPopup();
        } else {
            UiUtils.displayDismissibleSnackbar(rootView, "Unknown option");
        }
    }

    // =============================================================================================
    // Popup / AlertDialog Methods
    // =============================================================================================
    /**
     * Display the popup for a user to log out.
     */
    private void logoutPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setIcon(R.drawable.warning_icon);
        builder.setMessage("Are you sure you want to log out?");
        builder.setPositiveButton("Log Out", (dialog, position) -> {
            new Thread(() -> sharedPrefs.setJwt("")).start();
            UiUtils.displayDismissibleSnackbar(rootView, "Logout Successful!");
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    /**
     * Display the loading screen popup while downloading drills.
     */
    private void loadAllDrillsPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_loading_popup, null);

        TextView loadingText = dialogView.findViewById(R.id.pleaseWaitLabel);
        ProgressBar progressBar = dialogView.findViewById(R.id.loadingProgressBar);
        TextView errorMessage = dialogView.findViewById(R.id.loadingErrorMessage);

        builder.setView(dialogView);
        builder.setTitle("Downloading Drills");
        builder.setIcon(R.drawable.import_icon);
        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> viewModel.stopDownload());

        AlertDialog alert = builder.create();

        viewModel.downloadDb(
            newDrills -> {
                alert.dismiss();
                selectKnownDrillsPopup(newDrills);
            },
            error -> {
                loadingText.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                errorMessage.setText(error);
                errorMessage.setVisibility(View.VISIBLE);
            }
        );

        alert.show();
    }

    /**
     * Display the popup to allow users to mark which of the new drills they know and update the
     * database.
     *
     * @param newDrills List of drills downloaded from the server that were new.
     */
    private void selectKnownDrillsPopup(@NonNull List<Drill> newDrills) {
        if (newDrills.isEmpty()) {
            UiUtils.displayDismissibleSnackbar(rootView, "Download Successful!");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // The following two should line up completely in index positions, along with newDrills
        final String[] drillNames = newDrills.stream()
            .map(Drill::getName)
            .toArray(String[]::new);
        // They all start false
        final boolean[] checkedKnownDrills = new boolean[drillNames.length];

        builder.setTitle("New Drills! Select the ones you know:");
        builder.setIcon(R.drawable.save_icon);
        builder.setMultiChoiceItems(drillNames, checkedKnownDrills,
                ((dialogInterface, position, isChecked) -> checkedKnownDrills[position] = isChecked));
        builder.setCancelable(false);
        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Check All", null);
        builder.setNeutralButton("Clear All", null);

        AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {
            alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                // "Save"
                List<Drill> knownDrills = new ArrayList<>(checkedKnownDrills.length);
                for (int i = 0; i < checkedKnownDrills.length; i++) {
                    if (checkedKnownDrills[i]) {
                        knownDrills.add(newDrills.get(i));
                    }
                }

                viewModel.markDrillsAsKnown(knownDrills, new OperationCompleteCallback() {
                    @Override
                    public void onSuccess() {
                        alert.dismiss();
                        runOnUiThread(() -> howToUnlockDrillsPopup());
                    }

                    @Override
                    public void onFailure(@NonNull String error) {
                        alert.dismiss();
                        UiUtils.displayDismissibleSnackbar(rootView, error);
                    }
                });
            });

            alert.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(view -> {
                // "Check All"
                Arrays.fill(checkedKnownDrills, true);

                ListView listView = alert.getListView();
                for (int i = 0; i < listView.getCount(); i++) {
                    listView.setItemChecked(i, true);
                }
                // Do not dismiss
            });

            alert.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(view -> {
                // "Clear All"
                Arrays.fill(checkedKnownDrills, false);

                ListView listView = alert.getListView();
                for (int i = 0; i < listView.getCount(); i++) {
                    listView.setItemChecked(i, false);
                }
                // Do not dismiss
            });
        });

        alert.show();
    }

    /**
     * Popup that informs the user how to go back and unlock drills.
     */
    private void howToUnlockDrillsPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Download Successful!");
        builder.setIcon(R.drawable.checkmark_icon);
        builder.setMessage(R.string.unlock_drills_how_to_popup_message);
        builder.setPositiveButton("Done", null);

        builder.create().show();
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    /**
     * Perform some checks before setting up the download all drills operation.
     */
    private void handleDownloadDrills() {
        if (!internetConnection.isNetworkConnected()) {
            UiUtils.displayDismissibleSnackbar(rootView, "No internet connection");
            return;
        }

        if (sharedPrefs.getJwt().isEmpty()) {
            commonPopups.displayLoginPopup(new OperationCompleteCallback() {
                @Override
                public void onSuccess() {
                    loadAllDrillsPopup();
                    UiUtils.displayDismissibleSnackbar(rootView, "Login Successful!");
                }

                @Override
                public void onFailure(@NonNull String error) {
                    UiUtils.displayDismissibleSnackbar(rootView, error);
                }
            });
        } else {
            loadAllDrillsPopup();
        }
    }

    // =============================================================================================
    // Onboarding Methods
    // =============================================================================================
    /**
     * Start the onboarding process for this activity, walking the user through the screen and
     * explaining how it works. Preceded by
     * {@link HomeActivity#startOnboardingActivity(Context)}, proceeded by
     * {@link HomeActivity#continueOnboardingActivity(Context, Class)} from
     * WebDrillOptionsActivity.
     */
    private void startOnboarding() {
        boolean cancelable = sharedPrefs.isOnboardingComplete();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        String downloadDescription;
        if (internetConnection.isNetworkConnected()) {
            downloadDescription = getString(R.string.onboarding_download_drills_description);
        } else {
            downloadDescription = getString(R.string.onboarding_download_drills_no_internet_description);
        }

        TapTarget downloadTapTarget = OnboardingUtils.createTapTarget(
                findViewById(R.id.downloadFromDatabaseCard),
                "Download Drills",
                downloadDescription,
                cancelable
        );
        TapTarget logoutTapTarget = OnboardingUtils.createTapTarget(
                findViewById(R.id.logoutCard),
                "Logout",
                getString(R.string.onboarding_logout_description),
                cancelable
        );
        TapTarget homeTapTarget = OnboardingUtils.createToolbarHomeTapTarget(context,
                findViewById(R.id.appToolbar),
                cancelable);

        TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(downloadTapTarget, logoutTapTarget, homeTapTarget)
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        HomeActivity.continueOnboardingActivity(context, WebDrillOptionsActivity.class);
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        if (lastTarget == downloadTapTarget && internetConnection.isNetworkConnected()) {
                            /*
                            This will technically still continue the TapTargetSequence, but the
                            popup will be in the foreground and the user won't be able to continue
                            the TapTargetSequence until the download popups are done.
                             */
                            handleDownloadDrills();
                        }
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {

                    }
                });

        activityUsePopup(cancelable, sequence::start, sequence::cancel);
    }

    /**
     * Explain the purpose of this activity.
     *
     * @param cancelable        true if this popup can be canceled by the user.
     * @param onDialogFinish    Runnable once the user has finished the popup.
     * @param onDialogCancel    Runnable if the user cancels the popup.
     */
    private void activityUsePopup(boolean cancelable,
                                  @Nullable Runnable onDialogFinish,
                                  @Nullable Runnable onDialogCancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Online Drills");
        builder.setIcon(R.drawable.ic_launcher_foreground);
        builder.setCancelable(cancelable);
        builder.setMessage(R.string.onboarding_web_drill_options_activity_description);
        builder.setPositiveButton("Continue", (dialogInterface, i) -> {
            if (null != onDialogFinish) {
                onDialogFinish.run();
            }
        });
        if (cancelable) {
            builder.setNeutralButton("Exit", ((dialogInterface, i) -> {
                if (null != onDialogCancel) {
                    onDialogCancel.run();
                }
            }));
        }

        builder.create().show();
    }
}
