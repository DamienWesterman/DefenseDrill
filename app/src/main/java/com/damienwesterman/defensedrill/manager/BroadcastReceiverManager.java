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

package com.damienwesterman.defensedrill.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.damienwesterman.defensedrill.utils.Constants;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * TODO: DOC COMMENTS
 */
@AndroidEntryPoint
public class BroadcastReceiverManager extends BroadcastReceiver {
    @Inject
    SimulatedAttackManager simulatedAttack;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (null == action) {
            return;
        }

        BroadcastReceiver.PendingResult pendingResult = goAsync();
        switch(action) {
            case Intent.ACTION_BOOT_COMPLETED:
                // Fallthrough intentional
            case Constants.INTENT_ACTION_START_SIMULATED_ATTACK_MANAGER:
                new Thread(() -> {
                    simulatedAttack.scheduleSimulatedAttack();
                    pendingResult.finish();
                }).start();
                break;

            case Constants.INTENT_ACTION_STOP_SIMULATED_ATTACK_MANAGER:
                simulatedAttack.stopSimulatedAttacks();
                pendingResult.finish();
                break;

            case Constants.INTENT_ACTION_SIMULATE_ATTACK:
                new Thread(() -> {
                    simulatedAttack.simulateAttack();
                    pendingResult.finish();
                }).start();
                break;

            default:
                // Do nothing
                break;
        }
    }
}