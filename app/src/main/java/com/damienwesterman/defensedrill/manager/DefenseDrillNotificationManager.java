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

package com.damienwesterman.defensedrill.manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.ui.activities.DrillInfoActivity;
import com.damienwesterman.defensedrill.ui.activities.WebDrillOptionsActivity;
import com.damienwesterman.defensedrill.utils.Constants;

import lombok.RequiredArgsConstructor;

/**
 * Manager for Defense Drill App Notifications.
 */
@RequiredArgsConstructor
public class DefenseDrillNotificationManager {
    // TODO: https://developer.android.com/develop/background-work/services/alarms/schedule#inexact-after-specific-time
    // TODO: What if there is no self defense category?
    // TODO: Create a screen to allow the user to modify these notifications/feature (time of day, day(s) of the week, how many times per hour/how many drills per x hours, turn each one on/off like an alarm). User should be able to activate or deactivate the notifications? At every change we should check to see if there is a "Self Defense" Category
    // TODO: Allow the user to define how often, and at what hours to have the alerts (maybe have like a list that the user can add and delete time frames to!)
    // TODO: Modify the background service to follow these custom times
    // TODO: If there are no times, then kill the background service
    // TODO: When the user modifies the background service custom times, re-start the background service
    // TODO: Start this service on phone startup? Only if the user has notifications enabled and has selected to receive simulated attacks
    // TODO: Change and make sure that the background service starts up on boot up (make a separate broadcast receiver)
    // TODO: Have some kind of popup if the DrillInfoActivity receives an intent of self defense attack. Explain to come up with 3 solutions: technical solution, creative solution, and preventative solution (give an option to disable this popup) (also if a simulated attack, show a help button or something somewhere?)
    private static final String CHANNEL_ID_DATABASE_UPDATE_AVAILABLE = "database_update_available";
    private static final String CHANNEL_ID_SIMULATED_ATTACKS = "simulated_attacks";
    private static final String CHANNEL_DESCRIPTION_DATABASE_UPDATE_AVAILABLE =
            "Database Update Available";
    private static final String CHANNEL_DESCRIPTION_SIMULATED_ATTACKS =
            "Simulated Attacks";
    private static final int NOTIFICATION_ID_DATABASE_UPDATE_AVAILABLE = 1;
    private static final int NOTIFICATION_ID_SIMULATED_ATTACKS = 2;

    private final Context context;
    private final NotificationManager systemNotificationManager;

    private boolean initSuccess = false;

    /**
     * Initialize the manager.
     */
    public void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            systemNotificationManager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_ID_DATABASE_UPDATE_AVAILABLE,
                    CHANNEL_DESCRIPTION_DATABASE_UPDATE_AVAILABLE,
                    NotificationManager.IMPORTANCE_HIGH
            ));
            systemNotificationManager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_ID_SIMULATED_ATTACKS,
                    CHANNEL_DESCRIPTION_SIMULATED_ATTACKS,
                    NotificationManager.IMPORTANCE_HIGH
            ));
            initSuccess = true;
        }
    }

    /**
     * Create a notification that a database update is available. If clicked, will bring the user to
     * the {@link WebDrillOptionsActivity} activity.
     */
    public void notifyDatabaseUpdateAvailable() {
        if (!initSuccess || !systemNotificationManager.areNotificationsEnabled()) {
            return;
        }

        Intent intent = new Intent(context, WebDrillOptionsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0, /* Request Code for sender. Irrelevant */
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification =
            new NotificationCompat.Builder(context, CHANNEL_ID_DATABASE_UPDATE_AVAILABLE)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Database Update Available!")
                .setContentText("Click here to download new drills.")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true) /* Remove notification after it is clicked */
                .build();

        systemNotificationManager.notify(NOTIFICATION_ID_DATABASE_UPDATE_AVAILABLE, notification);
    }

    /**
     * Clear the notification for database update available.
     */
    public void removeDatabaseUpdateAvailableNotification() {
        if (!initSuccess || !systemNotificationManager.areNotificationsEnabled()) {
            return;
        }

        systemNotificationManager.cancel(NOTIFICATION_ID_DATABASE_UPDATE_AVAILABLE);
    }

    /**
     * Create a notification for a simulated self defense attack. If clicked, will bring the user to
     * that Drill's {@link DrillInfoActivity} activity.
     *
     * @param drill Simulated Drill attack
     */
    public void notifySimulatedAttack(@NonNull Drill drill) {
        if (!initSuccess || !systemNotificationManager.areNotificationsEnabled()) {
            return;
        }

        Intent intent = new Intent(context, DrillInfoActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_DRILL_ID, drill.getId());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(drill.getName()); // This is to avoid conflict with existing pendingIntents
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0, /* Request Code for sender. Irrelevant */
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT
        );

        String contentText =
                "You have to defend yourself from the following attack:\n" + drill.getName();
        Notification notification =
            new NotificationCompat.Builder(context, CHANNEL_ID_SIMULATED_ATTACKS)
                .setSmallIcon(R.drawable.danger_alert_icon)
                .setContentTitle("Simulated Attack!")
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true) /* Remove notification after it is clicked */
                .build();

        systemNotificationManager.notify(NOTIFICATION_ID_SIMULATED_ATTACKS, notification);
    }
}
