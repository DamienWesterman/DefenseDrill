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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
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
import com.damienwesterman.defensedrill.manager.SimulatedAttackManager;
import com.damienwesterman.defensedrill.service.CheckServerUpdateService;
import com.damienwesterman.defensedrill.ui.adapter.ViewPagerAdapter;
import com.damienwesterman.defensedrill.ui.common.UiUtils;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import kotlin.Triple;
import me.relex.circleindicator.CircleIndicator3;

/**
 * Home screen activity and entry point for the application. Displays the different general
 * functionalities of the app. CRUD operations in the database, Drill generation, and feedback.
 */
@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {
    private static final String TAG = HomeActivity.class.getSimpleName();
    // TODO: On first startup, go through help screen. THEN prompt the user for notifications:
        // https://developer.android.com/develop/ui/views/notifications/notification-permission#best-practices
        // https://developer.android.com/training/permissions/requesting#request-permission
    // TODO: THEN prompt the user for unrestricted background usage:
        // https://stackoverflow.com/a/54852199

    // TODO: Maybe start this process with a popup the then requests the permissions before starting the TapTargetView sequence stuff
        // TODO: Or maybe a sequence of popups that explain what a Drill is as well
    // TODO: Set cancelable by checking sharedPrefs (if this is the first onboarding, then not cancelable
    // TODO: Make sure to set battery option permissions if the user checks the simulated attacks notifications on
    // TODO: Maybe make some dummy data or something? i don't know how to do this if we go through the generate drill process
    // TODO: Sequence:
        // TODO: Home screen - First let's get some drills to work with
            // -> To DownloadDrills Activity
        // TODO: DownloadDrills Activity
            // -> Download Drills and log in (if you want)
            // -> Home
        // TODO: Home
            // Highlight Customize drills activity (can view all the drills, categories, and sub-categories)
            // Highlight Simulated Attack Settings
            // -> Simulated Attack Settings
        // TODO: Simulated Attack Settings:
            // highlight slider (explain what these are)
            // Check the slider, then highlight the create button
            // -> Home
        // TODO: home -> Generate Drill
            // Then go through the sequence, selecting random each time, then get to drill info
        // TODO: Drill Info
            // TODO: Create some way to have this populate with default info
            // Explain everything about this screen
            // TODO: Return home and SET THAT WE HAVE COMPLETED ONBOARDING

    private static boolean isUpdateServiceStarted = false;

    private LinearLayout rootView;
    private Context context;

    @Inject
    CheckPhoneInternetConnection internetConnection;
    @Inject
    SimulatedAttackManager simulatedAttackManager;
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
        Toolbar appToolbar = findViewById(R.id.appToolbar);
        appToolbar.setTitle("Defense Drill Home");
        setSupportActionBar(appToolbar);

        rootView = findViewById(R.id.activityHome);
        context = this;

        // Have to do this here so service is not started when the app is launched in the background
        if (!isUpdateServiceStarted) {
            CheckServerUpdateService.startService(this);
            isUpdateServiceStarted = true;
        }

        if (!sharedPrefs.isOnboardingComplete()
                || getIntent().hasExtra(Constants.INTENT_EXTRA_START_ONBOARDING)) {
            if (getIntent().hasExtra(Constants.INTENT_EXTRA_START_ONBOARDING)) {
                Serializable serializable = getIntent()
                        .getSerializableExtra(Constants.INTENT_EXTRA_START_ONBOARDING);
                if (serializable instanceof Class) {
                    startOnboarding((Class<?>) serializable);
                } else {
                    Log.e(TAG, "serializable not of type Class, cannot call startOnboarding()");
                }
            } else {
                startOnboarding(null);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
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
        } else if (R.id.feedbackCard == cardId) {
            sendFeedbackEmail();
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
        // TODO: Doc comments - Write the entire process here
        // TODO: Also make sure to write the before/after steps inside each other activity

        boolean cancelable = sharedPrefs.isOnboardingComplete();
        // How to:
//        TapTarget drillCardTapTarget = TapTarget.forView(findViewById(R.id.generateDrillCard),
//                        "TITLE", "DESCRIPTION")
//                .outerCircleColor(R.color.drill_green_variant)
//                .tintTarget(false)
//                .cancelable(false);
//        TapTarget customizeDatabaseTapTarget = TapTarget.forView(findViewById(R.id.customizeDatabaseCard),
//                        "TITLE 2", "DESCRIPTION 2")
//                .outerCircleColor(R.color.drill_green_variant)
//                .tintTarget(false)
//                .cancelable(false);
//
//        TapTargetSequence sequence = new TapTargetSequence(this)
//                .targets(drillCardTapTarget, customizeDatabaseTapTarget)
//                .listener(new TapTargetSequence.Listener() {
//                    @Override
//                    public void onSequenceFinish() {
//                        WebDrillOptionsActivity.startActivity(context);
//                    }
//
//                    @Override
//                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
//
//                    }
//
//                    @Override
//                    public void onSequenceCanceled(TapTarget lastTarget) {
//
//                    }
//                });
//        sequence.start();
//        sequence.cancel();
        onboardingTerminologyIntroPopup(cancelable, () -> {
            // TODO: sequence.cancel();
        });
    }

    // TODO: Doc comments
    public void onboardingTerminologyIntroPopup(boolean cancelable,
                                                @Nullable Runnable onCancelCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_onboarding_terminology_popup, null);
        ViewPager2 viewPager = dialogView.findViewById(R.id.onboardingViewPager);
        CircleIndicator3 indicator = dialogView.findViewById(R.id.indicator);

        viewPager.setAdapter(new ViewPagerAdapter(List.of(
                new Pair<>("Title 1", "Description 1"),
                new Pair<>("Awesome 2", "Like super awesome"),
                new Pair<>("Finish 3", "Alright cool we're done here. Alright cool we're done here. Alright cool we're done here. Alright cool we're done here. Alright cool we're done here. Alright cool we're done here. Alright cool we're done here. Alright cool we're done here. Alright cool we're done here. Alright cool we're done here. Alright cool we're done here. Alright cool we're done here. Alright cool we're done here. Alright cool we're done here. Alright cool we're done here. Alright cool we're done here. Alright cool we're done here. Alright cool we're done here. Alright cool we're done here. Alright cool we're done here. ")
        )));
        indicator.setViewPager(viewPager);
        // TODO: TELL USER TO SWIPE THROUGH

        builder.setView(dialogView);
        builder.setIcon(R.drawable.ic_launcher_foreground);
        builder.setTitle("Welcome!");
        builder.setCancelable(cancelable);
        builder.setPositiveButton("Show me Around", null);
        if (cancelable) {
            builder.setNeutralButton("Exit", (dialogInterface, i) -> {
                if (null != onCancelCallback) {
                    onCancelCallback.run();
                }
            });
        }

        AlertDialog dialog = builder.create();

        // Make sure we properly measure the page so it will only show one at a time
        dialog.setOnShowListener(dialogInterface ->
                        viewPager.post(viewPager::requestLayout));

        dialog.show();
    }
}
