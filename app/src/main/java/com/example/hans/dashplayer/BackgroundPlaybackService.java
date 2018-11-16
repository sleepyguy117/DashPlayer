package com.example.hans.dashplayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class BackgroundPlaybackService extends Service {
    private static final String NOTIFICATION_CHANNEL_ID = "com.example.hans.dashplayer";
    private static final int NOTIFICATION_ID = 1;

    // Binder given to clients
    private final IBinder binder = new BackgroundBinder();
    private int bound = 0;

    private SimpleExoPlayer player;

    public class BackgroundBinder extends Binder {
        SimpleExoPlayer getPlayer() {
            // Return this instance of BitmovinPlayer so clients can use the player instance
            return BackgroundPlaybackService.this.player;
        }
    }

    private void initPlayer() {

        String userAgent = Util.getUserAgent(this, "test");
        DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;

        String wvLicenseUri = "https://proxy.uat.widevine.com/proxy?provider=widevine_test";
        try {

            MediaDrmCallback drmCallback = new HttpMediaDrmCallback(wvLicenseUri,
                    new DefaultHttpDataSourceFactory(userAgent));

            drmSessionManager = DefaultDrmSessionManager.newWidevineInstance(drmCallback, null);
        } catch (UnsupportedDrmException e) {

        }
        player = ExoPlayerFactory.newSimpleInstance(this, new DefaultRenderersFactory(this), new DefaultTrackSelector(), drmSessionManager);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("TAG", "service onCreate");
        final Context context = this;

        initPlayer();
    }

    @Override
    public void onDestroy() {
        Log.d("TAG", "service onDestroy");
        this.player.release();
        this.player = null;

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        this.bound++;
        Log.d("TAG", "service onBind... bound count = " + bound);
        return this.binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        this.bound--;
        Log.d("TAG", "service onUnbind... bound count = " + bound);
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("TAG", "service onStartCommand");
        return START_STICKY;
    }
}
