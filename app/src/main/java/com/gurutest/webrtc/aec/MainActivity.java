package com.gurutest.webrtc.aec;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.gurutest.webrtc.aec.audio.WebRtcAudioManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        WebRtcAudioManager.getInstance().stop();
    }

    private void init() {
        WebRtcAudioManager.getInstance().start(this);
    }
}
