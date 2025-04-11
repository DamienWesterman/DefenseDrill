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

import androidx.core.app.NotificationCompat;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.ui.activities.WebDrillOptionsActivity;

import lombok.RequiredArgsConstructor;

/**
 * Manager for Defense Drill App Notifications.
 */
@RequiredArgsConstructor
public class DefenseDrillNotificationManager {
    // TODO: Create a notification for the attack simulation
    // TODO: Make sure that it can click and bring to drill details screen
    // TODO: Only have one notification at a time, so use update if there already is one
    // TODO: Make a background service that does the drill generation periodically?
    // TODO: Start this service on phone startup? Only if the user has notifications enabled and has selected to receive simulated attacks
    // TODO: What if there is no self defense category?
    // TODO: Create a screen to allow the user to modify these notifications/feature
    // TODO: Allow the user to define how often, and at what hours to have the alerts (maybe have like a list that the user can add and delete time frames to!)
    // TODO: Modify the background service to follow these custom times
    private static final String CHANNEL_ID_DATABASE_UPDATE_AVAILABLE = "database_update_available";
    private static final String CHANNEL_DESCRIPTION_DATABASE_UPDATE_AVAILABLE =
            "Database Update Available";
    private static final int NOTIFICATION_ID_DATABASE_UPDATE_AVAILABLE = 1;

    private final Context context;
    private final NotificationManager systemNotificationManager;

    private boolean initSuccess = false;

    /**
     * Initialize the manager.
     */
    public void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID_DATABASE_UPDATE_AVAILABLE,
                    CHANNEL_DESCRIPTION_DATABASE_UPDATE_AVAILABLE,
                    NotificationManager.IMPORTANCE_HIGH
            );

            systemNotificationManager.createNotificationChannel(channel);
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
}
