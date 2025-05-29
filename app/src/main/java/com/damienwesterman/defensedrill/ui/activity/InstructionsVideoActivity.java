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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.ui.PlayerView;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.remote.ApiRepo;
import com.damienwesterman.defensedrill.util.Constants;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity that only plays video.
 */
@AndroidEntryPoint
public class InstructionsVideoActivity extends AppCompatActivity {
    private final static String TAG = InstructionsVideoActivity.class.getSimpleName();

    @Inject
    SharedPrefs sharedPrefs;

    private ExoPlayer player;

    // =============================================================================================
    // Activity Creation Methods
    // =============================================================================================
    /**
     * Start InstructionsVideoActivity by an instruction's Video ID.
     *
     * @param context   Context.
     * @param videoId   Video ID.
     */
    public static void startActivity(@NonNull Context context, @NonNull String videoId) {
        Intent intent = new Intent(context, InstructionsVideoActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_VIDEO_ID, videoId);
        context.startActivity(intent);
    }

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions_video);

        String videoId;
        if (getIntent().hasExtra(Constants.INTENT_EXTRA_VIDEO_ID)) {
            videoId = getIntent().getStringExtra(Constants.INTENT_EXTRA_VIDEO_ID);
            if (null == videoId || videoId.isEmpty()) {
                Log.e(TAG, "No videoId extra");
                finish();
                return;
            }
        } else {
            Log.e(TAG, "No videoId extra");
            finish();
            return;
        }

        DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(ApiRepo.getHeaders(sharedPrefs.getJwt()));

        MediaItem mediaItem = new MediaItem.Builder()
            .setUri(ApiRepo.createVideoUri(videoId))
            .build();

        player = new ExoPlayer.Builder(this)
            .setMediaSourceFactory(new DefaultMediaSourceFactory(dataSourceFactory))
            .build();

        PlayerView playerView = findViewById(R.id.exoPlayerView);
        playerView.setPlayer(player);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != player) {
            player.release();
            player = null;
        }
    }
}
