package com.example.hans.dashplayer;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;

public class PlayerActivity extends AppCompatActivity {

    SurfaceView videoView;

    SimpleExoPlayer player;

    AspectRatioFrameLayout videoFrame;

    private void setViews() {
        setContentView(R.layout.activity_player);

        videoView = findViewById(R.id.videoView);
        videoFrame = findViewById(R.id.videoFrame);

        videoView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                player.setVideoSurface(surfaceHolder.getSurface());
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                player.clearVideoSurface();
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initPlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(player != null) {
            player.release();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.d(MainActivity.class.toString(), "Configuration changed");
        setViews();
    }

    private void initPlayer() {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        Handler mainHandler = new Handler();

        String userAgent = Util.getUserAgent(this, "test");
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, userAgent, bandwidthMeter);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        String uri = "http://mirror.cessen.com/blender.org/peach/trailer/trailer_iphone.m4v";

        uri = "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd";
        uri = "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd";
        //MediaSource videoSource = new ExtractorMediaSource(Uri.parse(uri), dataSourceFactory, extractorsFactory, null, null);

        DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;

        String wvLicenseUri = "https://proxy.uat.widevine.com/proxy?provider=widevine_test";
        try{

            MediaDrmCallback drmCallback = new HttpMediaDrmCallback(wvLicenseUri,
                    new DefaultHttpDataSourceFactory(userAgent));

            drmSessionManager = DefaultDrmSessionManager.newWidevineInstance(drmCallback, null);
        }
        catch(UnsupportedDrmException e) {

        }

        MediaSource videoSource = new DashMediaSource(Uri.parse(uri), dataSourceFactory, new DefaultDashChunkSource.Factory(dataSourceFactory), mainHandler, null);

        player = ExoPlayerFactory.newSimpleInstance(this, new DefaultRenderersFactory(this), new DefaultTrackSelector(), drmSessionManager);

        player.prepare(videoSource);


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

                        Log.d(MainActivity.class.toString(), "player buffering");
                    }
                    case Player.STATE_READY: {

                        Log.d(MainActivity.class.toString(), "player ready , duration: " + player.getDuration());

                    }
                    case Player.STATE_ENDED: {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                        Log.d(MainActivity.class.toString(), "player ended");

                    }

                }

            }
        });

        player.setPlayWhenReady(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
