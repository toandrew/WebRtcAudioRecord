package com.gurutest.webrtc.aec.audio;

import android.content.Context;
import android.util.Log;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.PeerConnectionFactory.InitializationOptions;
import org.webrtc.RtpReceiver;
import org.webrtc.voiceengine.WebRtcAudioRecord;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebRtcAudioManager implements PeerConnection.Observer {
    private static final String TAG = "WebRtcAudioManager";

    PeerConnectionFactory peerConnectionFactory;

    public static final String AUDIO_TRACK_ID = "ARDAMSa0";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private RecordedAudioToFileController saveRecordedAudioToFile = null;

    private static WebRtcAudioManager sInstance = new WebRtcAudioManager();

    private MediaConstraints audioConstraints;

    private AudioTrack audioTrack;

    private MediaStream mediaStream;

    private PeerConnection peerConnection;

    public static WebRtcAudioManager getInstance() {
        return sInstance;
    }

    public void start(Context context) {
        if (saveRecordedAudioToFile == null) {
            init();
        }

        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(context.getApplicationContext()).createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);
        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory();

        audioConstraints = new MediaConstraints();
        AudioSource audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
        audioTrack = peerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        audioTrack.setEnabled(true);

        mediaStream = peerConnectionFactory.createLocalMediaStream("ARDAMS");
        mediaStream.addTrack(audioTrack);

        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        peerConnection = peerConnectionFactory.createPeerConnection(iceServers, this);
        peerConnection.addStream(mediaStream);
    }

    public void stop() {

    }

    private void init() {
        saveRecordedAudioToFile = new RecordedAudioToFileController(executor);

        WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(true);
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

        WebRtcAudioRecord.setOnAudioSamplesReady(new WebRtcAudioRecord.WebRtcAudioRecordSamplesReadyCallback() {
            @Override
            public void onWebRtcAudioRecordSamplesReady(WebRtcAudioRecord.AudioSamples audioSamples) {
                Log.w(TAG, "onWebRtcAudioRecordSamplesReady");
            }
        });
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {

    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {

    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {

    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

    }

    @Override
    public void onAddStream(MediaStream mediaStream) {

    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {

    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {

    }

    @Override
    public void onRenegotiationNeeded() {

    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

    }
}
