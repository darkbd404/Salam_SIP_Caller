<?php
require_once __DIR__ . '/includes/config.php';
$user = require_auth();

$sessionId = clean_input($_GET['session_id'] ?? '');
$callee = clean_input($_GET['callee'] ?? 'Unknown');
$callType = clean_input($_GET['type'] ?? 'AUDIO');
$isIncoming = !empty($_GET['incoming']);
$isIpCall = str_starts_with($callee, IP_PREFIX);

// Look up contact/user name and photo
$users = read_json_data(USER_JSON_FILE);
$contacts = read_json_data(CONTACTS_JSON_FILE);

$peerName = $callee;
$peerPhoto = '';

foreach ($users as $u) {
    if (($u['ipNumber'] ?? '') === $callee || ($u['mobileNumber'] ?? '') === $callee) {
        $peerName = $u['name'];
        $peerPhoto = $u['profilePhoto'] ?? '';
        break;
    }
}
if ($peerName === $callee) {
    foreach ($contacts as $c) {
        if ($c['number'] === $callee) {
            $peerName = $c['name'];
            break;
        }
    }
}
?>
<!DOCTYPE html>
<html lang="bn">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>কল চলছে - <?php echo htmlspecialchars($peerName); ?></title>
  
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Hind+Siliguri:wght@400;500;600;700&display=swap" rel="stylesheet">
  
  <style>
    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      -webkit-tap-highlight-color: transparent;
    }
    body {
      background: #030F0D;
      font-family: 'Hind Siliguri', -apple-system, BlinkMacSystemFont, sans-serif;
      color: #fff;
      min-height: 100vh;
      display: flex;
      justify-content: center;
      align-items: center;
      overflow: hidden;
    }
    .call-container {
      width: 100%;
      max-width: 480px;
      min-height: 100vh;
      background: radial-gradient(circle at 50% 30%, #004D40 0%, #02120F 70%, #010807 100%);
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      align-items: center;
      padding: 36px 24px 44px;
      position: relative;
      box-shadow: 0 0 40px rgba(0,0,0,0.9);
    }
    
    /* Video Area */
    .video-layer {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
      z-index: 1;
      display: <?php echo $callType === 'VIDEO' ? 'block' : 'none'; ?>;
    }
    .remote-video {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .local-video {
      position: absolute;
      top: 24px;
      right: 20px;
      width: 105px;
      height: 145px;
      border-radius: 16px;
      object-fit: cover;
      border: 2px solid #00E676;
      box-shadow: 0 8px 24px rgba(0,0,0,0.6);
      z-index: 5;
    }

    .ui-overlay {
      position: relative;
      z-index: 3;
      width: 100%;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      align-items: center;
      min-height: calc(100vh - 80px);
    }

    /* Top Section */
    .call-badge {
      background: rgba(0, 230, 118, 0.15);
      border: 1px solid rgba(0, 230, 118, 0.4);
      color: #00E676;
      padding: 6px 14px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 700;
      display: inline-flex;
      align-items: center;
      gap: 6px;
      margin-bottom: 12px;
    }
    .caller-title {
      font-size: 26px;
      font-weight: 700;
      letter-spacing: 0.3px;
      margin-bottom: 4px;
      text-shadow: 0 2px 10px rgba(0,0,0,0.5);
    }
    .caller-subtitle {
      font-size: 14px;
      color: #80CBC4;
      font-weight: 600;
      letter-spacing: 1px;
    }
    .call-state-text {
      font-size: 13px;
      color: #00E676;
      margin-top: 8px;
      font-weight: 600;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
    }

    /* Center Avatar with Glowing Audio Rings */
    .avatar-wrapper {
      position: relative;
      width: 150px;
      height: 150px;
      margin: 15px auto;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .ring-pulse-1, .ring-pulse-2 {
      position: absolute;
      inset: -15px;
      border-radius: 50%;
      background: rgba(0, 230, 118, 0.15);
      animation: pulseRing 2.2s infinite ease-out;
      pointer-events: none;
    }
    .ring-pulse-2 {
      inset: -30px;
      animation-delay: 0.7s;
      background: rgba(0, 137, 123, 0.12);
    }
    .avatar-circle {
      width: 140px;
      height: 140px;
      border-radius: 50%;
      background: linear-gradient(135deg, #00897B, #004D40);
      border: 3px solid rgba(0, 230, 118, 0.8);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 54px;
      color: #fff;
      overflow: hidden;
      box-shadow: 0 10px 30px rgba(0, 230, 118, 0.35);
      z-index: 2;
    }
    .avatar-circle img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    /* Call Duration & Audio Visualizer */
    .timer-badge {
      font-size: 24px;
      font-weight: 700;
      color: #fff;
      letter-spacing: 2px;
      margin-top: 8px;
    }
    .audio-waves {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 4px;
      height: 24px;
      margin-top: 6px;
    }
    .wave-bar {
      width: 3px;
      height: 8px;
      background: #00E676;
      border-radius: 2px;
      animation: soundWave 1.2s infinite ease-in-out alternate;
    }
    .wave-bar:nth-child(2) { animation-delay: 0.2s; height: 16px; }
    .wave-bar:nth-child(3) { animation-delay: 0.4s; height: 22px; }
    .wave-bar:nth-child(4) { animation-delay: 0.1s; height: 12px; }
    .wave-bar:nth-child(5) { animation-delay: 0.3s; height: 18px; }

    /* Modern Glassmorphic Action Control Grid */
    .controls-panel {
      width: 100%;
      max-width: 350px;
      background: rgba(11, 28, 25, 0.65);
      backdrop-filter: blur(16px);
      -webkit-backdrop-filter: blur(16px);
      border: 1px solid rgba(77, 182, 172, 0.25);
      border-radius: 24px;
      padding: 16px 14px;
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 10px;
      margin-bottom: 24px;
      box-shadow: 0 8px 30px rgba(0,0,0,0.4);
    }
    .ctrl-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 6px;
      background: transparent;
      border: none;
      color: #fff;
      cursor: pointer;
      outline: none;
    }
    .ctrl-icon-btn {
      width: 52px;
      height: 52px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.1);
      border: 1px solid rgba(255, 255, 255, 0.18);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
      color: #fff;
      transition: all 0.2s ease;
    }
    .ctrl-item.active .ctrl-icon-btn {
      background: #00E676;
      color: #00291B;
      box-shadow: 0 0 16px rgba(0, 230, 118, 0.6);
      border-color: #00E676;
    }
    .ctrl-item.recording .ctrl-icon-btn {
      background: #FF5252;
      color: #fff;
      border-color: #FF5252;
      animation: pulseRing 1s infinite;
    }
    .ctrl-label {
      font-size: 11px;
      font-weight: 600;
      color: #B2DFDB;
    }

    /* End Call Action */
    .end-call-btn {
      width: 74px;
      height: 74px;
      border-radius: 50%;
      background: linear-gradient(135deg, #FF5252, #D50000);
      border: none;
      color: #fff;
      font-size: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      box-shadow: 0 8px 28px rgba(255, 82, 82, 0.55);
      transition: transform 0.15s ease;
      margin: 0 auto;
    }
    .end-call-btn:active {
      transform: scale(0.92);
    }

    @keyframes pulseRing {
      0% { transform: scale(0.95); opacity: 0.8; }
      50% { transform: scale(1.15); opacity: 0.4; }
      100% { transform: scale(1.3); opacity: 0; }
    }
    @keyframes soundWave {
      0% { height: 4px; }
      100% { height: 22px; }
    }
  </style>
</head>
<body>

<!-- Hidden Audio for Remote Stream Audio Output -->
<audio id="remoteAudio" autoplay playsinline></audio>

<div class="call-container">

  <!-- Video Layers (if video call) -->
  <div class="video-layer" id="videoLayer">
    <video id="remoteVideo" class="remote-video" autoplay playsinline></video>
    <video id="localVideo" class="local-video" autoplay playsinline muted></video>
  </div>

  <div class="ui-overlay">
    
    <!-- Top Contact Details -->
    <div style="text-align: center;">
      <div class="call-badge">
        <i class="fas <?php echo $callType === 'VIDEO' ? 'fa-video' : 'fa-phone-volume'; ?>"></i>
        <?php echo $isIpCall ? 'Salam SIP HD Voice (09612)' : 'BTRC GSM Routing (৳ ০.৩০/মিনিট)'; ?>
      </div>
      <h1 class="caller-title"><?php echo htmlspecialchars($peerName); ?></h1>
      <div class="caller-subtitle"><?php echo htmlspecialchars($callee); ?></div>
      <div class="call-state-text" id="callStateText">
        <i class="fas fa-spinner fa-spin"></i> <?php echo $isIncoming ? 'সংযুক্ত হচ্ছে...' : 'রিং হচ্ছে (Ringing)...'; ?>
      </div>
    </div>

    <!-- Center Avatar / Visualizer -->
    <?php if ($callType !== 'VIDEO'): ?>
    <div style="text-align: center;">
      <div class="avatar-wrapper">
        <div class="ring-pulse-1"></div>
        <div class="ring-pulse-2"></div>
        <div class="avatar-circle">
          <?php if (!empty($peerPhoto) && file_exists(__DIR__ . '/' . $peerPhoto)): ?>
            <img src="<?php echo htmlspecialchars($peerPhoto); ?>" alt="Avatar">
          <?php else: ?>
            <i class="fas fa-user"></i>
          <?php endif; ?>
        </div>
      </div>
      
      <div class="timer-badge" id="callTimerDisplay">00:00</div>
      
      <div class="audio-waves" id="audioWaveBars" style="display: none;">
        <div class="wave-bar"></div>
        <div class="wave-bar"></div>
        <div class="wave-bar"></div>
        <div class="wave-bar"></div>
        <div class="wave-bar"></div>
      </div>
      <div style="font-size: 11px; color: #FFD54F; margin-top: 6px;">কল রেট: ৳ ০.৩০ / মিনিট</div>
    </div>
    <?php else: ?>
    <div style="text-align: center;">
      <div class="timer-badge" id="callTimerDisplay" style="background: rgba(0,0,0,0.5); padding: 4px 14px; border-radius: 20px; font-size: 18px;">00:00</div>
    </div>
    <?php endif; ?>

    <!-- Bottom Controls -->
    <div style="width: 100%;">
      <div class="controls-panel">
        
        <!-- Mute -->
        <button class="ctrl-item" id="btnMute" onclick="toggleMute()">
          <div class="ctrl-icon-btn">
            <i class="fas fa-microphone" id="muteIcon"></i>
          </div>
          <span class="ctrl-label" id="muteLabel">মিউট</span>
        </button>

        <!-- Speaker -->
        <button class="ctrl-item" id="btnSpeaker" onclick="toggleSpeaker()">
          <div class="ctrl-icon-btn">
            <i class="fas fa-volume-high"></i>
          </div>
          <span class="ctrl-label">স্পিকার</span>
        </button>

        <!-- Record -->
        <button class="ctrl-item" id="btnRecord" onclick="toggleRecording()">
          <div class="ctrl-icon-btn">
            <i class="fas fa-circle-dot"></i>
          </div>
          <span class="ctrl-label" id="recordLabel">রেকর্ড</span>
        </button>

        <!-- Message -->
        <button class="ctrl-item" onclick="window.open('chat.php?recipient=<?php echo urlencode($callee); ?>', '_blank')">
          <div class="ctrl-icon-btn">
            <i class="fas fa-comment-dots"></i>
          </div>
          <span class="ctrl-label">মেসেজ</span>
        </button>

      </div>

      <!-- End Call Button -->
      <button class="end-call-btn" onclick="endCallAndExit()" title="কল শেষ করুন">
        <i class="fas fa-phone-slash"></i>
      </button>
    </div>

  </div>
</div>

<script>
const sessionId = "<?php echo addslashes($sessionId); ?>";
const callee = "<?php echo addslashes($callee); ?>";
const calleeName = "<?php echo addslashes($peerName); ?>";
const callType = "<?php echo addslashes($callType); ?>";
const isIncoming = <?php echo $isIncoming ? 'true' : 'false'; ?>;
const myIp = "<?php echo addslashes($user['ipNumber']); ?>";
let userBalance = <?php echo (float)($user['balance'] ?? 0); ?>;

let localStream = null;
let peerConnection = null;
let callDurationSec = 0;
let callTimerInterval = null;
let pollSignalInterval = null;
let isCallConnected = false;
let isMuted = false;
let isSpeakerOn = false;
let mediaRecorder = null;
let recordedAudioChunks = [];
let isRecording = false;

// WebRTC STUN Configuration
const rtcConfig = {
  iceServers: [
    { urls: 'stun:stun.l.google.com:19302' },
    { urls: 'stun:stun1.l.google.com:19302' },
    { urls: 'stun:stun2.l.google.com:19302' }
  ]
};

// Outgoing Ringtone Audio Context
let audioCtx = null;
let ringOsc1 = null, ringOsc2 = null;

function playOutgoingRingTone() {
  if (isIncoming) return;
  try {
    audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    if (audioCtx.state === 'suspended') audioCtx.resume();
    ringOsc1 = audioCtx.createOscillator();
    ringOsc2 = audioCtx.createOscillator();
    const gain = audioCtx.createGain();

    ringOsc1.frequency.value = 440;
    ringOsc2.frequency.value = 480;
    gain.gain.setValueAtTime(0.08, audioCtx.currentTime);

    ringOsc1.connect(gain);
    ringOsc2.connect(gain);
    gain.connect(audioCtx.destination);

    ringOsc1.start();
    ringOsc2.start();
  } catch(e){}
}

function stopOutgoingRingTone() {
  if (ringOsc1) {
    try { ringOsc1.stop(); ringOsc2.stop(); } catch(e){}
    ringOsc1 = null;
    ringOsc2 = null;
  }
}

// Initialize WebRTC & Media
async function initWebRTCCall() {
  try {
    // 1. Get User Media (Microphone & Optional Video)
    const constraints = {
      audio: {
        echoCancellation: true,
        noiseSuppression: true,
        autoGainControl: true
      },
      video: callType === 'VIDEO' ? { facingMode: 'user', width: 640, height: 480 } : false
    };

    localStream = await navigator.mediaDevices.getUserMedia(constraints);

    if (callType === 'VIDEO') {
      const localVid = document.getElementById('localVideo');
      if (localVid) localVid.srcObject = localStream;
    }

    // 2. Setup RTCPeerConnection
    peerConnection = new RTCPeerConnection(rtcConfig);

    // Add local tracks to peer connection
    localStream.getTracks().forEach(track => {
      peerConnection.addTrack(track, localStream);
    });

    // Handle remote track (Sound hearing)
    peerConnection.ontrack = (event) => {
      console.log('Received remote media stream track:', event.track.kind);
      const remoteAudio = document.getElementById('remoteAudio');
      if (remoteAudio && event.streams[0]) {
        remoteAudio.srcObject = event.streams[0];
        remoteAudio.play().catch(err => console.log('Audio autoplay retry:', err));
      }
      if (callType === 'VIDEO') {
        const remoteVid = document.getElementById('remoteVideo');
        if (remoteVid && event.streams[0]) {
          remoteVid.srcObject = event.streams[0];
        }
      }
    };

    // Handle ICE Candidates
    peerConnection.onicecandidate = (event) => {
      if (event.candidate && activeSessionId) {
        fetch('api/signal.php', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            action: 'SEND_WEBRTC',
            sessionId: activeSessionId,
            type: isIncoming ? 'callee_candidate' : 'caller_candidate',
            payload: event.candidate
          })
        }).catch(()=>{});
      }
    };

    playOutgoingRingTone();

    // 3. Initiate or Join Signaling Session
    if (!isIncoming) {
      // Caller: Create Offer
      const offer = await peerConnection.createOffer();
      await peerConnection.setLocalDescription(offer);

      const res = await fetch('api/signal.php?action=INITIATE_CALL', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          calleeNumber: callee,
          callType: callType,
          sdpOffer: offer
        })
      });
      const data = await res.json();
      if (!data.success) {
        alert(data.message || 'কল সংযোগ করা যায়নি');
        window.location.href = data.needRecharge ? 'recharge.php' : 'dialer.php';
        return;
      }
      activeSessionId = data.sessionId;
    } else {
      activeSessionId = sessionId;
      // Callee: Fetch offer & Create answer
      setupCalleeAnswer();
    }

    startSignalPolling();

  } catch (err) {
    console.error('Media init error:', err);
    document.getElementById('callStateText').innerText = 'মাইক্রোফোন পারমিশন প্রয়োজন!';
  }
}

let activeSessionId = sessionId;

async function setupCalleeAnswer() {
  try {
    const res = await fetch(`api/signal.php?action=POLL_CALL&session_id=${encodeURIComponent(activeSessionId)}`);
    const data = await res.json();
    if (data.signal && data.signal.sdpOffer) {
      await peerConnection.setRemoteDescription(new RTCSessionDescription(data.signal.sdpOffer));
      const answer = await peerConnection.createAnswer();
      await peerConnection.setLocalDescription(answer);

      await fetch('api/signal.php', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          action: 'ACCEPT_CALL',
          sessionId: activeSessionId,
          sdpAnswer: answer
        })
      });

      onCallConnectedSuccess();
    }
  } catch (err) {
    console.error('Answer error:', err);
  }
}

function startSignalPolling() {
  pollSignalInterval = setInterval(async () => {
    if (!activeSessionId) return;
    try {
      const res = await fetch(`api/signal.php?action=POLL_CALL&session_id=${encodeURIComponent(activeSessionId)}`);
      const data = await res.json();

      if (!data.success || data.status === 'ENDED' || (data.signal && data.signal.status === 'ENDED')) {
        clearInterval(pollSignalInterval);
        handleCallTerminated('কল শেষ হয়েছে (Call Ended)');
      } else if (data.signal && data.signal.status === 'REJECTED') {
        clearInterval(pollSignalInterval);
        handleCallTerminated('✕ কলটি প্রত্যাখ্যান করা হয়েছে (Declined)');
      } else if (data.signal && data.signal.status === 'CONNECTED') {
        if (!isCallConnected) {
          // If Caller and got SDP Answer from Callee
          if (!isIncoming && data.signal.sdpAnswer && peerConnection && !peerConnection.currentRemoteDescription) {
            await peerConnection.setRemoteDescription(new RTCSessionDescription(data.signal.sdpAnswer));
          }
          onCallConnectedSuccess();
        }
      }

      // Sync ICE Candidates
      if (peerConnection && data.signal) {
        const candidates = isIncoming ? data.signal.callerCandidates : data.signal.calleeCandidates;
        if (Array.isArray(candidates)) {
          for (const cand of candidates) {
            try {
              await peerConnection.addIceCandidate(new RTCIceCandidate(cand));
            } catch(e){}
          }
        }
      }
    } catch (e) {}
  }, 1800);
}

function onCallConnectedSuccess() {
  if (isCallConnected) return;
  isCallConnected = true;
  stopOutgoingRingTone();

  document.getElementById('callStateText').innerHTML = '<i class="fas fa-circle-check" style="color: #00E676;"></i> ✓ সংযোগ হয়েছে <span style="color:#00E676; font-weight:700;">(HD Voice)</span>';
  
  const waveBars = document.getElementById('audioWaveBars');
  if (waveBars) waveBars.style.display = 'flex';

  callTimerInterval = setInterval(() => {
    callDurationSec++;
    const m = String(Math.floor(callDurationSec / 60)).padStart(2, '0');
    const s = String(callDurationSec % 60).padStart(2, '0');
    document.getElementById('callTimerDisplay').innerText = `${m}:${s}`;

    // Deduct balance every 60 seconds
    if (callDurationSec % 60 === 0) {
      userBalance -= 0.30;
      if (userBalance < 0.30) {
        alert('আপনার একাউন্ট ব্যালেন্স শেষ হয়ে গেছে। অনুগ্রহ করে রিচার্জ করুন।');
        endCallAndExit();
      }
    }
  }, 1000);
}

function toggleMute() {
  isMuted = !isMuted;
  const btn = document.getElementById('btnMute');
  const icon = document.getElementById('muteIcon');
  const label = document.getElementById('muteLabel');
  
  btn.classList.toggle('active', isMuted);
  if (isMuted) {
    icon.className = 'fas fa-microphone-slash';
    label.innerText = 'আনমিউট';
  } else {
    icon.className = 'fas fa-microphone';
    label.innerText = 'মিউট';
  }

  if (localStream) {
    localStream.getAudioTracks().forEach(t => t.enabled = !isMuted);
  }
}

function toggleSpeaker() {
  isSpeakerOn = !isSpeakerOn;
  document.getElementById('btnSpeaker').classList.toggle('active', isSpeakerOn);
  const audio = document.getElementById('remoteAudio');
  if (audio && typeof audio.setSinkId === 'function') {
    // Speaker toggle supported on modern browsers
    try {
      audio.setSinkId(isSpeakerOn ? 'speaker' : 'default');
    } catch(e){}
  }
}

function toggleRecording() {
  if (!isRecording) {
    startCallRecording();
  } else {
    stopCallRecording();
  }
}

function startCallRecording() {
  if (!localStream) {
    alert('রেকর্ড করার জন্য অডিও স্ট্রিম নেই!');
    return;
  }
  recordedAudioChunks = [];
  try {
    mediaRecorder = new MediaRecorder(localStream);
    mediaRecorder.ondataavailable = (e) => {
      if (e.data.size > 0) recordedAudioChunks.push(e.data);
    };
    mediaRecorder.onstop = saveCallRecordingFile;
    mediaRecorder.start();
    isRecording = true;
    document.getElementById('btnRecord').classList.add('recording');
    document.getElementById('recordLabel').innerText = 'রেকর্ডিং...';
  } catch (err) {
    console.error('Recorder error:', err);
  }
}

function stopCallRecording() {
  if (mediaRecorder && isRecording) {
    mediaRecorder.stop();
    isRecording = false;
    document.getElementById('btnRecord').classList.remove('recording');
    document.getElementById('recordLabel').innerText = 'রেকর্ড';
  }
}

function saveCallRecordingFile() {
  const blob = new Blob(recordedAudioChunks, { type: 'audio/webm' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `salam_call_${callee}_${Date.now()}.webm`;
  document.body.appendChild(a);
  a.click();
  setTimeout(() => {
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }, 100);
}

function handleCallTerminated(msg) {
  stopOutgoingRingTone();
  if (callTimerInterval) clearInterval(callTimerInterval);
  if (isRecording) stopCallRecording();
  if (localStream) localStream.getTracks().forEach(t => t.stop());
  if (peerConnection) peerConnection.close();

  document.getElementById('callStateText').innerText = msg;
  setTimeout(() => {
    window.location.href = 'history.php';
  }, 1200);
}

function endCallAndExit() {
  stopOutgoingRingTone();
  if (callTimerInterval) clearInterval(callTimerInterval);
  if (isRecording) stopCallRecording();
  if (localStream) localStream.getTracks().forEach(t => t.stop());
  if (peerConnection) peerConnection.close();

  fetch('api/signal.php', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      action: 'END_CALL',
      sessionId: activeSessionId,
      duration: callDurationSec,
      status: callDurationSec > 0 ? 'COMPLETED' : 'MISSED'
    })
  }).finally(() => {
    window.location.href = 'history.php';
  });
}

document.addEventListener('DOMContentLoaded', initWebRTCCall);
</script>

</body>
</html>
