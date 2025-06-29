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
 * Copyright 2024 Damien Westerman
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

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.common.Constants;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.domain.CheckPhoneInternetConnection;
import com.damienwesterman.defensedrill.manager.DefenseDrillNotificationManager;
import com.damienwesterman.defensedrill.manager.SimulatedAttackManager;
import com.damienwesterman.defensedrill.service.CheckServerUpdateService;
import com.damienwesterman.defensedrill.ui.adapter.ViewPagerAdapter;
import com.damienwesterman.defensedrill.ui.common.OnboardingUtils;
import com.damienwesterman.defensedrill.ui.common.UiUtils;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import me.relex.circleindicator.CircleIndicator3;

/**
 * Home screen activity and entry point for the application. Displays the different general
 * functionalities of the app. CRUD operations in the database, Drill generation, and feedback.
 */
@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {
    private static final String TAG = HomeActivity.class.getSimpleName();

    private static boolean isUpdateServiceStarted = false;

    private LinearLayout rootView;
    private Context context;

    @Inject
    CheckPhoneInternetConnection internetConnection;
    @Inject
    SimulatedAttackManager simulatedAttackManager;
    @Inject
    DefenseDrillNotificationManager notificationManager;
    @Inject
    SharedPrefs sharedPrefs;

    // =============================================================================================
    // Activity Creation Methods
    // =============================================================================================
    /**
     * Start the HomeActivity. Clears the activity stack so home is now the top.
     *
     * @param context   Context.
     */
    public static void startActivity(@NonNull Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Start the HomeActivity in the Onboarding state.
     *
     * @param context   Context.
     */
    public static void startOnboardingActivity(@NonNull Context context) {
        continueOnboardingActivity(context, null);
    }

    /**
     * Continue the HomeActivity in the Onboarding state from another activity.
     *
     * @param context           Context.
     * @param previousActivity  Activity to continue the onboarding process from, or null to start
     *                          a new onboarding session.
     */
    public static void continueOnboardingActivity(@NonNull Context context,
                                                  @Nullable Class<?> previousActivity) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.INTENT_EXTRA_START_ONBOARDING, previousActivity);
        context.startActivity(intent);
    }

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Set up toolbar
        Toolbar appToolbar = findViewById(R.id.appToolbar);
        appToolbar.setTitle("Defense Drill Home");
        setSupportActionBar(appToolbar);

        appToolbar.post(() -> {
            appToolbar.getMenu().findItem(R.id.homeButton).setVisible(false);
            appToolbar.getMenu().findItem(R.id.feedbackButton).setVisible(true);
            appToolbar.getMenu().findItem(R.id.helpButton).setVisible(true);
        });

        rootView = findViewById(R.id.activityHome);
        context = this;

        // Have to do this here so service is not started when the app is launched in the background
        if (!isUpdateServiceStarted) {
            CheckServerUpdateService.startService(this);
            isUpdateServiceStarted = true;
        }

        checkForOnboarding(appToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (R.id.feedbackButton == item.getItemId()) {
            sendFeedbackEmail();
            return true;
        } else if (R.id.helpButton == item.getItemId()) {
            startOnboarding(null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // =============================================================================================
    // OnClickListener Methods
    // =============================================================================================
    public void onCardClick(@NonNull View view) {
        int cardId = view.getId();
        if (R.id.generateDrillCard == cardId) {
            CategorySelectActivity.startActivityClearTop(this);
        } else if (R.id.customizeDatabaseCard == cardId) {
            CustomizeDatabaseActivity.startActivity(this);
        } else if (R.id.simulatedAttackSettings == cardId) {
            SimulatedAttackSettingsActivity.startActivity(this);
        } else if (R.id.webDrillOptionsCard == cardId) {
            if (!internetConnection.isNetworkConnected()) {
                UiUtils.displayDismissibleSnackbar(rootView, "No internet connection.");
            } else {
                WebDrillOptionsActivity.startActivity(this);
            }
        } else {
            UiUtils.displayDismissibleSnackbar(rootView, "Unknown option");
        }
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    /**
     * Sends an intent to start an email for the user to send us feedback.
     */
    private void sendFeedbackEmail() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.parse("mailto:" + Uri.encode(Constants.FEEDBACK_RECEIPT_EMAIL)
                + "?subject=Defense%20Drill%20Feedback&body="
                + Uri.encode(getResources().getString(R.string.feedback_email_template)));

        intent.setData(data);
        startActivity(intent);
    }


    // =============================================================================================
    // Onboarding Methods
    // =============================================================================================
    /**
     * Check if we need to start the onboarding process, if so start it appropriately.
     *
     * @param appToolbar    Toolbar.
     */
    private void checkForOnboarding(Toolbar appToolbar) {
        if (!sharedPrefs.isOnboardingComplete()
                || getIntent().hasExtra(Constants.INTENT_EXTRA_START_ONBOARDING)) {
            if (getIntent().hasExtra(Constants.INTENT_EXTRA_START_ONBOARDING)) {
                Serializable serializable = getIntent()
                        .getSerializableExtra(Constants.INTENT_EXTRA_START_ONBOARDING);
                if (serializable instanceof Class) {
                    Class<?> previousClass = (Class<?>) serializable;
                    if (DrillInfoActivity.class == previousClass) {
                        // This step relies on the toolbar being set up, so wait until it is
                        appToolbar.post(() -> startOnboarding(previousClass));
                    } else {
                        startOnboarding(previousClass);
                    }
                } else {
                    Log.e(TAG, "serializable not of type Class, cannot call startOnboarding()");
                }
            } else {
                startOnboarding(null);
            }
        }
    }

    /**
     * Start the Onboarding process that will walk the user through how to use the app.
     * <br><br>
     * Onboarding Process:
     * <ol>
     *     <li>{@link #startOnboardingActivity(Context)}</li>
     *     <li>{@link WebDrillOptionsActivity#startOnboardingActivity(Context)}</li>
     *     <li>{@link #continueOnboardingActivity(Context, Class)} - using {@link WebDrillOptionsActivity}</li>
     *     <li>{@link SimulatedAttackSettingsActivity#startOnboardingActivity(Context)}</li>
     *     <li>{@link #continueOnboardingActivity(Context, Class)} - using {@link SimulatedAttackSettingsActivity}</li>
     *     <li>{@link CategorySelectActivity#startOnboardingActivity(Context)}</li>
     *     <li>{@link SubCategorySelectActivity#startOnboardingActivity(Context)}</li>
     *     <li>{@link DrillInfoActivity#startOnboardingActivity(Context)}</li>
     *     <li>{@link #continueOnboardingActivity(Context, Class)} - using {@link DrillInfoActivity}</li>
     * </ol>
     *
     * @param previousActivity  Previous activity in the onboarding process. May be null if starting
     *                          a new onboarding process.
     */
    private void startOnboarding(@Nullable Class<?> previousActivity) {
        boolean cancelable = sharedPrefs.isOnboardingComplete();
        List<TapTarget> tapTargets;
        Runnable onSequenceFinish;

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        if (null == previousActivity) {
            // Starting of the onboarding
            onSequenceFinish = () -> WebDrillOptionsActivity.startOnboardingActivity(context);
            tapTargets = List.of(
                    OnboardingUtils.createTapTarget(
                            findViewById(R.id.webDrillOptionsCard),
                            "Online Options",
                            getString(R.string.onboarding_web_drill_options_description),
                            cancelable
                    )
            );
        } else if (WebDrillOptionsActivity.class == previousActivity) {
            onSequenceFinish = () -> SimulatedAttackSettingsActivity.startOnboardingActivity(context);
            tapTargets = List.of(
                    OnboardingUtils.createTapTarget(
                            findViewById(R.id.customizeDatabaseCard),
                            "View saved Data",
                            getString(R.string.onboarding_customize_database_description),
                            cancelable
                    ),
                    OnboardingUtils.createTapTarget(
                            findViewById(R.id.simulatedAttackSettings),
                            "Simulated Attacks!",
                            getString(R.string.onboarding_simulated_attacks_description),
                            cancelable
                    )
            );
        } else if (SimulatedAttackSettingsActivity.class == previousActivity) {
            onSequenceFinish = () -> CategorySelectActivity.startOnboardingActivity(context);
            tapTargets = List.of(
                    OnboardingUtils.createTapTarget(
                            findViewById(R.id.generateDrillCard),
                            "Start a Workout",
                            getString(R.string.onboarding_generate_drill_description),
                            cancelable
                    )
            );
        } else if (DrillInfoActivity.class == previousActivity) {
            onSequenceFinish = () -> onboardingDonePopup(() -> {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                sharedPrefs.setOnboardingComplete(true);
            });
            Toolbar toolbar = findViewById(R.id.appToolbar);
            tapTargets = List.of(
                    OnboardingUtils.createToolbarTapTarget(
                            toolbar,
                            R.id.feedbackButton,
                            "Feedback",
                            getString(R.string.onboarding_feedback_description),
                            cancelable
                    ),
                    OnboardingUtils.createToolbarTapTarget(
                            toolbar,
                            R.id.helpButton,
                            "Help",
                            getString(R.string.onboarding_help_description),
                            cancelable
                    )
            );
        } else {
            UiUtils.displayDismissibleSnackbar(rootView, "Something went wrong");
            Log.e(TAG, "Invalid class for previousActivity: " + previousActivity);
            return;
        }

        TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(tapTargets)
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        onSequenceFinish.run();
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {

                    }
                });

        if (null == previousActivity) {
            // Start with an introduction to the terminology
            onboardingTerminologyIntroPopup(cancelable, sequence::start, sequence::cancel);
        } else {
            sequence.start();
        }
    }

    /**
     * Popup help screen that starts the onboarding. Primarily gives the basics of the app, what it
     * is used for, and terminology like Drill and Category.
     *
     * @param cancelable        true if the user should be allowed to cancel the popup.
     * @param onDialogFinish    Runnable for when the user closes the popup properly.
     * @param onCancelCallback  Runnable for if the user cancels the popup. Only applicable is
     *                          cancelable is true.
     */
    public void onboardingTerminologyIntroPopup(boolean cancelable,
                                                @Nullable Runnable onDialogFinish,
                                                @Nullable Runnable onCancelCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_onboarding_terminology_popup, null);
        ViewPager2 viewPager = dialogView.findViewById(R.id.onboardingViewPager);
        CircleIndicator3 indicator = dialogView.findViewById(R.id.indicator);

        List<Pair<String, String>> pages = new ArrayList<>();
        Pair<String, String> tutorialPage =
                new Pair<>("This Tutorial", getString(R.string.onboarding_this_tutorial_description));
        pages.add(tutorialPage);
        Pair<String, String> appUsagePage =
                new Pair<>("What does the app do?", getString(R.string.onboarding_app_usage_description));
        pages.add(appUsagePage);
        Pair<String, String> terminologyPage =
                new Pair<>("Terminology", getString(R.string.onboarding_terminology_description));
        pages.add(terminologyPage);
        Pair<String, String> notificationsPermissionsPage =
                new Pair<>("Notification Permissions", getString(R.string.onboarding_notifications_permission_description));
        if (!notificationManager.areNotificationsEnabled()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && !sharedPrefs.isOnboardingComplete()) {
            // Notifications are denied by default above TIRAMISU
            pages.add(notificationsPermissionsPage);
        }
        Pair<String, String> batteryPermissionsPage =
                new Pair<>("Battery Permissions", getString(R.string.onboarding_battery_permissions_description));
        if (PackageManager.PERMISSION_GRANTED !=
                    checkSelfPermission(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                && !sharedPrefs.isOnboardingComplete()) {
            pages.add(batteryPermissionsPage);
        }
        Pair<String, String> finishedPage =
                new Pair<>("Let's Go!", getString(R.string.onboarding_lets_go_description));
        pages.add(finishedPage);

        viewPager.setAdapter(new ViewPagerAdapter(pages));
        indicator.setViewPager(viewPager);

        builder.setView(dialogView);
        builder.setIcon(R.drawable.ic_launcher_foreground);
        builder.setTitle("Welcome!");
        builder.setCancelable(cancelable);
        builder.setPositiveButton("Show me Around", (dialogInterface, i) -> {
            if (null != onDialogFinish) {
                onDialogFinish.run();
            }
        });
        if (cancelable) {
            builder.setNeutralButton("Exit", (dialogInterface, i) -> {
                if (null != onCancelCallback) {
                    onCancelCallback.run();
                }
            });
        }

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            // Make sure we properly measure the page so it will only show one at a time
            viewPager.post(viewPager::requestLayout);

            // Set up logic to only show the button to continue once the user has scrolled all pages
            Button showMeAroundButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (!cancelable) {
                showMeAroundButton.setVisibility(View.GONE);
            }
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }

                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);

                    if (0 < position) {
                        int lastPage = position - 1;
                        if (notificationsPermissionsPage.equals(pages.get(lastPage))) {
                            // Ask for notification permissions after we describe why
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                requestPermissions(
                                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
                            }
                        } else if (batteryPermissionsPage.equals(pages.get(lastPage))) {
                            requestPermissions(
                                    new String[]{Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS}, 0);
                        }
                    }

                    if (position == (pages.size() - 1)) {
                        // User reached the last page, let them continue
                        showMeAroundButton.setVisibility(View.VISIBLE);
                    }

                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    super.onPageScrollStateChanged(state);
                }
            });
        });

        dialog.show();
    }

    /**
     * Popup to finish off the onboarding process.
     *
     * @param onPopupFinished Runnable for when the popup is closed.
     */
    private void onboardingDonePopup(@Nullable Runnable onPopupFinished) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("That's it!");
        builder.setIcon(R.drawable.ic_launcher_foreground);
        builder.setCancelable(true);
        builder.setMessage(R.string.onboarding_finished_popup_message);
        builder.setPositiveButton("Finish", ((dialogInterface, i) -> {
            if (null != onPopupFinished) {
                onPopupFinished.run();
            }
        }));

        builder.create().show();
    }
}
