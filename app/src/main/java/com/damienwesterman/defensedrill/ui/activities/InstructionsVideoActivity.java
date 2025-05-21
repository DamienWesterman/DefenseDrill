package com.damienwesterman.defensedrill.ui.activities;

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
import com.damienwesterman.defensedrill.utils.Constants;

import java.util.Map;

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
        } else {
            Log.e(TAG, "No videoId extra");
            finish();
            return;
        }

        // TODO: properly implement and pull logic into appropriate classes
        DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(Map.of(
                "Content-Type", "application/json",
                "Cookie", "jwt=" + sharedPrefs.getJwt()
            ));

        MediaItem mediaItem = new MediaItem.Builder()
            .setUri("https://defensedrillweb.duckdns.org/videos/" + videoId + "/stream")
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
        if (player != null) {
            player.release();
            player = null;
        }
    }
}