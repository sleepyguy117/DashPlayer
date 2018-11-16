package com.example.hans.dashplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;

public class PlayerActivity2 extends AppCompatActivity {

    SurfaceView videoView;

    private SimpleExoPlayer player;
    private boolean bound = false;

    AspectRatioFrameLayout videoFrame;

    private void setViews() {
        setContentView(R.layout.activity_player);

        videoView = findViewById(R.id.videoView);
        videoFrame = findViewById(R.id.videoFrame);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent(this, BackgroundPlaybackService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        // If the Service is not started, it would get destroyed as soon as the Activity unbinds.
        startService(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (player != null) {
            player.setVideoSurfaceView(null);
        }
        unbindService(mConnection);
        this.player = null;
        this.bound = false;
    }

    private void initPlayer() {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

        String userAgent = Util.getUserAgent(this, "test");
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, userAgent, bandwidthMeter);
        String uri = "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd";

        uri = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd";
        player.addVideoListener(new VideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

                float ratio = width / height;
                videoFrame.setAspectRatio(ratio);
            }
        });

        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                switch (playbackState) {

                    case Player.STATE_BUFFERING: {

                        Log.d("TAG", "player buffering");
                    }
                    case Player.STATE_READY: {

                        Log.d("TAG", "player ready");

                    }
                    case Player.STATE_ENDED: {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                        Log.d("TAG", "player ended");
                    }
                }
            }
        });


        if (player.getPlaybackState() == Player.STATE_IDLE || player.getPlaybackState() == Player.STATE_ENDED) {
            MediaSource videoSource = new DashMediaSource(Uri.parse(uri), dataSourceFactory, new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);
            player.prepare(videoSource);
            player.setPlayWhenReady(true);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BackgroundPlaybackService.BackgroundBinder binder = (BackgroundPlaybackService.BackgroundBinder) service;
            player = binder.getPlayer();
            initPlayer();
            player.setVideoSurfaceView(videoView);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };
}
