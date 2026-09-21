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
      background: #020C0A;
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
      background: radial-gradient(circle at 50% 25%, #004D40 0%, #031713 65%, #010A08 100%);
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      align-items: center;
      padding: 36px 20px 42px;
      position: relative;
    }
    
    /* Video Layer */
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
      width: 100px;
      height: 140px;
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
      min-height: calc(100vh - 78px);
    }

    .call-badge {
      background: rgba(0, 230, 118, 0.16);
      border: 1px solid rgba(0, 230, 118, 0.45);
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
      margin-bottom: 3px;
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

    /* Avatar & Sound Rings */
    .avatar-wrapper {
      position: relative;
      width: 140px;
      height: 140px;
      margin: 15px auto;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .ring-pulse {
      position: absolute;
      inset: -15px;
      border-radius: 50%;
      background: rgba(0, 230, 118, 0.18);
      animation: pulseRing 2s infinite ease-out;
      pointer-events: none;
    }
    .avatar-circle {
      width: 130px;
      height: 130px;
      border-radius: 50%;
      background: linear-gradient(135deg, #00897B, #004D40);
      border: 3px solid #00E676;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 50px;
      color: #fff;
      overflow: hidden;
      box-shadow: 0 10px 30px rgba(0, 230, 118, 0.4);
      z-index: 2;
    }
    .avatar-circle img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .timer-badge {
      font-size: 26px;
      font-weight: 700;
      color: #fff;
      letter-spacing: 2px;
      margin-top: 8px;
    }
    .audio-waves {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 5px;
      height: 22px;
      margin-top: 6px;
    }
    .wave-bar {
      width: 4px;
      height: 8px;
      background: #00E676;
      border-radius: 3px;
      animation: soundWave 1.2s infinite ease-in-out alternate;
    }
    .wave-bar:nth-child(2) { animation-delay: 0.2s; height: 16px; }
    .wave-bar:nth-child(3) { animation-delay: 0.4s; height: 22px; }
    .wave-bar:nth-child(4) { animation-delay: 0.1s; height: 12px; }
    .wave-bar:nth-child(5) { animation-delay: 0.3s; height: 18px; }

    /* Controls Panel */
    .controls-panel {
      width: 100%;
      max-width: 340px;
      background: rgba(11, 28, 25, 0.7);
      backdrop-filter: blur(16px);
      -webkit-backdrop-filter: blur(16px);
      border: 1px solid rgba(77, 182, 172, 0.25);
      border-radius: 24px;
      padding: 16px 12px;
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 10px;
      margin-bottom: 22px;
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
      width: 50px;
      height: 50px;
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
      border-color: #00E676;
      box-shadow: 0 0 16px rgba(0, 230, 118, 0.6);
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

    .end-call-btn {
      width: 72px;
      height: 72px;
      border-radius: 50%;
      background: linear-gradient(135deg, #FF5252, #D50000);
      border: none;
      color: #fff;
      font-size: 28px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      box-shadow: 0 8px 28px rgba(255, 82, 82, 0.55);
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

<!-- Hidden Audio Element for Two-Way Remote Audio Playback -->
<audio id="remoteAudio" autoplay playsinline></audio>

<div class="call-container" onclick="unlockAudioPlayback()">

  <!-- Video Area -->
  <div class="video-layer" id="videoLayer">
    <video id="remoteVideo" class="remote-video" autoplay playsinline></video>
    <video id="localVideo" class="local-video" autoplay playsinline muted></video>
  </div>

  <div class="ui-overlay">
    
    <!-- Top Details -->
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

    <!-- Center Visualizer -->
    <?php if ($callType !== 'VIDEO'): ?>
    <div style="text-align: center;">
      <div class="avatar-wrapper">
        <div class="ring-pulse"></div>
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

    <!-- Controls -->
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
        <button class="ctrl-item" onclick="window.open('chat.php?peer=<?php echo urlencode($callee); ?>', '_blank')">
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
let activeSessionId = sessionId;

// WebRTC Configuration
const rtcConfig = {
  iceServers: [
    { urls: 'stun:stun.l.google.com:19302' },
    { urls: 'stun:stun1.l.google.com:19302' },
    { urls: 'stun:stun2.l.google.com:19302' }
  ]
};

// Outgoing Ringtone
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

function unlockAudioPlayback() {
  const remoteAudio = document.getElementById('remoteAudio');
  if (remoteAudio && remoteAudio.paused && remoteAudio.srcObject) {
    remoteAudio.play().catch(()=>{});
  }
}

// Initialize WebRTC
async function initWebRTCCall() {
  try {
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

    peerConnection = new RTCPeerConnection(rtcConfig);

    localStream.getTracks().forEach(track => {
      peerConnection.addTrack(track, localStream);
    });

    peerConnection.ontrack = (event) => {
      const remoteAudio = document.getElementById('remoteAudio');
      if (remoteAudio && event.streams[0]) {
        remoteAudio.srcObject = event.streams[0];
        remoteAudio.play().catch(err => console.log('Autoplay retry:', err));
      }
      if (callType === 'VIDEO') {
        const remoteVid = document.getElementById('remoteVideo');
        if (remoteVid && event.streams[0]) {
          remoteVid.srcObject = event.streams[0];
        }
      }
    };

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

    if (!isIncoming) {
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
      setupCalleeAnswer();
    }

    startSignalPolling();

  } catch (err) {
    console.error('Media error:', err);
    document.getElementById('callStateText').innerText = 'মাইক্রোফোন পারমিশন অন করুন!';
  }
}

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
        handleCallTerminated('কল শেষ হয়েছে');
      } else if (data.signal && data.signal.status === 'REJECTED') {
        clearInterval(pollSignalInterval);
        handleCallTerminated('✕ কলটি প্রত্যাখ্যান করা হয়েছে');
      } else if (data.signal && data.signal.status === 'CONNECTED') {
        if (!isCallConnected) {
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
  unlockAudioPlayback();

  document.getElementById('callStateText').innerHTML = '<i class="fas fa-circle-check" style="color: #00E676;"></i> ✓ সংযোগ হয়েছে <span style="color:#00E676; font-weight:700;">(HD Voice)</span>';
  
  const waveBars = document.getElementById('audioWaveBars');
  if (waveBars) waveBars.style.display = 'flex';

  callTimerInterval = setInterval(() => {
    callDurationSec++;
    const m = String(Math.floor(callDurationSec / 60)).padStart(2, '0');
    const s = String(callDurationSec % 60).padStart(2, '0');
    document.getElementById('callTimerDisplay').innerText = `${m}:${s}`;

    if (callDurationSec % 60 === 0) {
      userBalance -= 0.30;
      if (userBalance < 0.30) {
        alert('আপনার ব্যালেন্স শেষ হয়ে গেছে!');
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
    alert('অডিও স্ট্রিম পাওয়া যায়নি');
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
