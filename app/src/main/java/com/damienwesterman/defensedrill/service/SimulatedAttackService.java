package com.damienwesterman.defensedrill.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * TODO: Doc comments
 */
public class SimulatedAttackService extends Service {
    private static boolean active = false; // TODO: remove
    private static boolean started = false; // TODO: remove
    private static boolean stopService = false; // TODO: remove
    final Handler handler = new Handler(); // TODO: Remove
    // TODO: Make a temporary way to start and stop the service (set a toggle method in the feedback thing)
    // TODO: Make a broadcast receiver and a static method to register it (for internal intents)
    // TODO: Make a background thread that every like 1 minute (if activated) sends an alert
    // TODO: Research proper background scheduling per android

    // =============================================================================================
    // Service Creation Methods
    // =============================================================================================
    // TODO: remove ALL
    public static void startService(Context context) {
        Intent intent = new Intent(context, SimulatedAttackService.class);
        context.startService(intent);
    }

    public static void stopService() {
        stopService = true;
    }

    public static void toggle(Context context) {
        if (!started) {
            startService(context);
            started = true;
        }
        active = !active;
    }

    // =============================================================================================
    // Android Service Methods
    // =============================================================================================
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // TODO: Startup
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                simulatedAttackLoop();
                if (!stopService) {
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    @Override
    public void onDestroy() {
        // TODO: Cleanup
        super.onDestroy();
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    private void simulatedAttackLoop() {
        if (active) {
            Log.i("DxTag", Thread.currentThread().getName());
        }
    }
}