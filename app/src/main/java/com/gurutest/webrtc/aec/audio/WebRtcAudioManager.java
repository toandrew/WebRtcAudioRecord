package com.gurutest.webrtc.aec.audio;

import android.content.Context;
import android.util.Log;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.voiceengine.WebRtcAudioRecord;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebRtcAudioManager {
    private static final String TAG = "WebRtcAudioManager";

    PeerConnectionFactory peerConnectionFactory;

    public static final String AUDIO_TRACK_ID = "ARDAMSa0";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private RecordedAudioToFileController saveRecordedAudioToFile = null;

    private static WebRtcAudioManager sInstance = new WebRtcAudioManager();

    private AudioTrack audioTrack;

    private PeerConnection peerConnectionLocal;
    private PeerConnection peerConnectionRemote;

    private MediaStream mediaStreamLocal;
    private MediaStream mediaStreamRemote;

    public static WebRtcAudioManager getInstance() {
        return sInstance;
    }

    public void start(Context context) {
        WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(true);
        WebRtcAudioUtils.setDefaultSampleRateHz(16000);
        WebRtcAudioRecord.setErrorCallback(new WebRtcAudioRecord.WebRtcAudioRecordErrorCallback() {
            @Override
            public void onWebRtcAudioRecordInitError(String s) {
                Log.w(TAG, "onWebRtcAudioRecordInitError");
            }

            @Override
            public void onWebRtcAudioRecordStartError(WebRtcAudioRecord.AudioRecordStartErrorCode audioRecordStartErrorCode, String s) {
                Log.w(TAG, "onWebRtcAudioRecordStartError");
            }

            @Override
            public void onWebRtcAudioRecordError(String s) {
                Log.w(TAG, "onWebRtcAudioRecordError");
            }
        });

        saveRecordedAudioToFile = new RecordedAudioToFileController(executor);
        saveRecordedAudioToFile.start();

        // create PeerConnectionFactory
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions
                .builder(context.getApplicationContext())
                .createInitializationOptions());
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .createPeerConnectionFactory();

        MediaConstraints audioConstraints = new MediaConstraints();
        AudioSource audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
        audioTrack = peerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        audioTrack.setEnabled(true);

        mediaStreamLocal = peerConnectionFactory.createLocalMediaStream("mediaStreamLocal");
        mediaStreamLocal.addTrack(audioTrack);

        mediaStreamRemote = peerConnectionFactory.createLocalMediaStream("mediaStreamRemote");

        call(mediaStreamLocal, mediaStreamRemote);
    }

    public void stop() {
        saveRecordedAudioToFile.stop();
    }

    private void call(MediaStream localMediaStream, MediaStream remoteMediaStream) {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        peerConnectionLocal = peerConnectionFactory.createPeerConnection(iceServers, new PeerConnectionAdapter("localconnection") {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                peerConnectionRemote.addIceCandidate(iceCandidate);
                Log.w(TAG, "peerConnectionLocal: onIceCandidate");
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                Log.w(TAG, "peerConnectionLocal: onAddStream");
            }
        });

        peerConnectionRemote = peerConnectionFactory.createPeerConnection(iceServers, new PeerConnectionAdapter("remoteconnection") {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                peerConnectionLocal.addIceCandidate(iceCandidate);
                Log.w(TAG, "peerConnectionRemote: onIceCandidate");
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);

                Log.w(TAG, "peerConnectionRemote: onAddStream");
            }
        });

        peerConnectionLocal.addStream(localMediaStream);
        peerConnectionLocal.setAudioPlayout(false);
        peerConnectionLocal.createOffer(new SdpAdapter("local offer sdp") {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                Log.w(TAG, "peerConnectionLocal: createOffer: onCreateSuccess");

                // todo crashed here
                peerConnectionLocal.setLocalDescription(new SdpAdapter("local set local"), sessionDescription);
                peerConnectionRemote.setRemoteDescription(new SdpAdapter("remote set remote"), sessionDescription);
                peerConnectionRemote.createAnswer(new SdpAdapter("remote answer sdp") {
                    @Override
                    public void onCreateSuccess(SessionDescription sdp) {
                        super.onCreateSuccess(sdp);
                        Log.w(TAG, "peerConnectionRemote: createAnswer: onCreateSuccess");
                        peerConnectionRemote.setLocalDescription(new SdpAdapter("remote set local"), sdp);
                        peerConnectionLocal.setRemoteDescription(new SdpAdapter("local set remote"), sdp);
                    }
                }, new MediaConstraints());
            }
        }, new MediaConstraints());
    }
}
