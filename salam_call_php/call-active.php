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
  <link rel="stylesheet" href="assets/css/style.css">
  <style>
    body {
      background: #040D0C;
      overflow: hidden;
    }
    .call-screen {
      min-height: 100vh;
      background: radial-gradient(circle at center, #00382E 0%, #040D0C 100%);
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      align-items: center;
      padding: 30px 20px 40px;
      text-align: center;
      position: relative;
    }
    .video-container {
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
      background: #000;
    }
    .local-video {
      position: absolute;
      top: 20px;
      right: 20px;
      width: 100px;
      height: 140px;
      border-radius: 12px;
      object-fit: cover;
      border: 2px solid #00E676;
      box-shadow: 0 4px 15px rgba(0,0,0,0.6);
      z-index: 3;
    }
    .call-overlay {
      position: relative;
      z-index: 2;
      width: 100%;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      align-items: center;
      min-height: calc(100vh - 70px);
    }
    .avatar-pulse {
      width: 120px;
      height: 120px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--primary), var(--primary-light));
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 46px;
      color: #fff;
      box-shadow: 0 0 0 0 rgba(0, 230, 118, 0.7);
      animation: pulse 2s infinite;
      margin: 15px auto;
      overflow: hidden;
    }
    .call-controls {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 12px;
      width: 100%;
      max-width: 340px;
      margin-bottom: 20px;
    }
    .control-btn {
      aspect-ratio: 1;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.12);
      border: 1px solid rgba(255, 255, 255, 0.2);
      color: #fff;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 4px;
      cursor: pointer;
      font-size: 16px;
      transition: all 0.2s;
    }
    .control-btn.active { background: #00E676; color: #000; font-weight: bold; }
    .control-btn.recording { background: #FF5252; color: #fff; animation: pulse 1s infinite; }
    .btn-end-call {
      width: 70px;
      height: 70px;
      border-radius: 50%;
      background: #FF5252;
      border: none;
      color: #fff;
      font-size: 28px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      box-shadow: 0 6px 25px rgba(255, 82, 82, 0.5);
    }
  </style>
</head>
<body>

<div class="app-container" style="padding: 0; max-width: 500px;">
  <div class="call-screen">

    <!-- Video Streams Container (For Video Calls) -->
    <div class="video-container" id="videoWrapper">
      <video id="remoteVideo" class="remote-video" autoplay playsinline></video>
      <video id="localVideo" class="local-video" autoplay playsinline muted></video>
    </div>

    <!-- Call Overlay -->
    <div class="call-overlay">
      
      <!-- Top Info -->
      <div>
        <span class="badge-ip" style="background: rgba(0, 230, 118, 0.2); border-color: #00E676; margin-bottom: 10px; display: inline-block;">
          <i class="fas <?php echo $callType === 'VIDEO' ? 'fa-video' : 'fa-phone-volume'; ?>"></i> 
          <?php echo $isIpCall ? 'Salam SIP HD Voice/Video' : 'BTRC GSM Routing (৳ ০.৩০/মিনিট)'; ?>
        </span>
        <h2 style="font-size: 24px; font-weight: 700; margin-top: 6px;"><?php echo htmlspecialchars($peerName); ?></h2>
        <div style="font-size: 14px; color: var(--primary-light); letter-spacing: 1px; font-weight: 600;"><?php echo htmlspecialchars($callee); ?></div>
        <div id="callStatus" style="font-size: 14px; color: #00E676; margin-top: 8px; font-weight: 600;">
          <?php echo $isIncoming ? '✓ সংযোগ হচ্ছে...' : 'রিং হচ্ছে (Ringing)...'; ?>
        </div>
      </div>

      <!-- Center Avatar for Audio Mode -->
      <?php if ($callType !== 'VIDEO'): ?>
      <div>
        <div class="avatar-pulse">
          <?php if (!empty($peerPhoto) && file_exists(__DIR__ . '/' . $peerPhoto)): ?>
            <img src="<?php echo htmlspecialchars($peerPhoto); ?>" style="width: 100%; height: 100%; object-fit: cover;" alt="Peer">
          <?php else: ?>
            <i class="fas fa-user"></i>
          <?php endif; ?>
        </div>
        <div id="timerDisplay" style="font-size: 22px; font-weight: 700; color: #fff; letter-spacing: 2px;">00:00</div>
        <div style="font-size: 11px; color: #FFD54F; margin-top: 4px;">কল রেট: ৳ ০.৩০ / মিনিট</div>
      </div>
      <?php else: ?>
      <div>
        <div id="timerDisplay" style="font-size: 20px; font-weight: 700; color: #fff; background: rgba(0,0,0,0.5); padding: 4px 12px; border-radius: 20px;">00:00</div>
      </div>
      <?php endif; ?>

      <!-- Controls -->
      <div>
        <div class="call-controls">
          <button class="control-btn" id="btnMute" onclick="toggleMute()">
            <i class="fas fa-microphone-slash"></i>
            <span style="font-size: 10px;">মিউট</span>
          </button>
          
          <button class="control-btn" id="btnSpeaker" onclick="toggleSpeaker()">
            <i class="fas fa-volume-high"></i>
            <span style="font-size: 10px;">স্পিকার</span>
          </button>
          
          <!-- Call Record Button -->
          <button class="control-btn" id="btnRecord" onclick="toggleRecording()">
            <i class="fas fa-circle-dot"></i>
            <span style="font-size: 10px;" id="recordText">রেকর্ড</span>
          </button>

          <button class="control-btn" onclick="location.href='messages.php'">
            <i class="fas fa-comment"></i>
            <span style="font-size: 10px;">মেসেজ</span>
          </button>
        </div>

        <!-- End Call Button -->
        <button class="btn-end-call" onclick="endCall()" style="margin: 0 auto;" title="কল কাটুন">
          <i class="fas fa-phone-slash"></i>
        </button>
      </div>

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

let callStartTime = null;
let timerInterval = null;
let callDurationSec = 0;
let isMuted = false;
let isSpeaker = false;
let mediaStream = null;
let mediaRecorder = null;
let recordedChunks = [];
let isRecording = false;

// Audio Tone for outgoing ring
const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
let ringOsc1 = null, ringOsc2 = null;

function playRingTone() {
  if (isIncoming) return;
  try {
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

function stopRingTone() {
  if (ringOsc1) {
    try { ringOsc1.stop(); ringOsc2.stop(); } catch(e){}
    ringOsc1 = null;
    ringOsc2 = null;
  }
}

// Request Media Permissions (Audio / Video)
async function setupLocalMedia() {
  try {
    const constraints = {
      audio: true,
      video: callType === 'VIDEO' ? { facingMode: 'user', width: { ideal: 640 }, height: { ideal: 480 } } : false
    };
    mediaStream = await navigator.mediaDevices.getUserMedia(constraints);
    
    if (callType === 'VIDEO') {
      const localVid = document.getElementById('localVideo');
      if (localVid) localVid.srcObject = mediaStream;
      
      const remoteVid = document.getElementById('remoteVideo');
      if (remoteVid) remoteVid.srcObject = mediaStream; // Mirror for HD preview
    }
  } catch (err) {
    console.warn('Microphone/Camera permission not granted:', err);
  }
}

// Initiate or Connect Call
async function initCall() {
  await setupLocalMedia();
  playRingTone();

  if (isIncoming) {
    startConnectedTimer();
  } else {
    // If not incoming, initiate signal if not exists
    if (!sessionId) {
      const res = await fetch('api/signal.php?action=INITIATE_CALL', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ calleeNumber: callee, callType })
      });
      const data = await res.json();
      if (!data.success) {
        alert(data.message || 'কল সংযোগ ব্যর্থ হয়েছে');
        if (data.needRecharge) {
          window.location.href = 'recharge.php';
        } else {
          window.location.href = 'dialer.php';
        }
        return;
      }
    }
  }

  // Poll call state
  startSignalingPoll();
}

let pollInterval = null;
function startSignalingPoll() {
  pollInterval = setInterval(async () => {
    if (!sessionId) return;
    try {
      const res = await fetch(`api/signal.php?action=POLL_CALL&session_id=${encodeURIComponent(sessionId)}`);
      const data = await res.json();
      
      if (!data.success || data.status === 'ENDED' || (data.signal && data.signal.status === 'ENDED')) {
        clearInterval(pollInterval);
        handleCallEndedLocally();
      } else if (data.signal && data.signal.status === 'CONNECTED' && !callStartTime) {
        stopRingTone();
        startConnectedTimer();
      } else if (data.signal && data.signal.status === 'REJECTED') {
        clearInterval(pollInterval);
        stopRingTone();
        document.getElementById('callStatus').innerText = '✕ কল কেটে দেওয়া হয়েছে (Declined)';
        setTimeout(() => { window.location.href = 'history.php'; }, 2000);
      }
    } catch(e){}
  }, 2000);
}

function startConnectedTimer() {
  stopRingTone();
  document.getElementById('callStatus').innerHTML = '✓ সংযুক্ত <span style="color:#00E676; font-weight:bold;">(Connected HD)</span>';
  callStartTime = Date.now();
  
  timerInterval = setInterval(() => {
    callDurationSec++;
    const m = String(Math.floor(callDurationSec / 60)).padStart(2, '0');
    const s = String(callDurationSec % 60).padStart(2, '0');
    document.getElementById('timerDisplay').innerText = `${m}:${s}`;

    // Deduct balance check every 60s
    if (callDurationSec % 60 === 0) {
      userBalance -= 0.30;
      if (userBalance < 0.30) {
        alert('ব্যালেন্স শেষ! কলটি সমাপ্ত হচ্ছে। অনুগ্রহ করে রিচার্জ করুন।');
        endCall();
      }
    }
  }, 1000);
}

function toggleMute() {
  isMuted = !isMuted;
  document.getElementById('btnMute').classList.toggle('active', isMuted);
  if (mediaStream) {
    mediaStream.getAudioTracks().forEach(t => t.enabled = !isMuted);
  }
}

function toggleSpeaker() {
  isSpeaker = !isSpeaker;
  document.getElementById('btnSpeaker').classList.toggle('active', isSpeaker);
}

// Call Recording Feature
function toggleRecording() {
  if (!isRecording) {
    startRecording();
  } else {
    stopRecording();
  }
}

function startRecording() {
  if (!mediaStream) {
    alert('রেকর্ড করার জন্য অডিও/ভিডিও স্ট্রিম পাওয়া যায়নি');
    return;
  }
  recordedChunks = [];
  try {
    mediaRecorder = new MediaRecorder(mediaStream);
    mediaRecorder.ondataavailable = (e) => {
      if (e.data.size > 0) recordedChunks.push(e.data);
    };
    mediaRecorder.onstop = saveRecordingToFile;
    mediaRecorder.start();
    isRecording = true;
    document.getElementById('btnRecord').classList.add('recording');
    document.getElementById('recordText').innerText = 'রেকর্ডিং...';
  } catch (err) {
    console.error('Recording error:', err);
  }
}

function stopRecording() {
  if (mediaRecorder && isRecording) {
    mediaRecorder.stop();
    isRecording = false;
    document.getElementById('btnRecord').classList.remove('recording');
    document.getElementById('recordText').innerText = 'রেকর্ড';
  }
}

function saveRecordingToFile() {
  const blob = new Blob(recordedChunks, { type: callType === 'VIDEO' ? 'video/webm' : 'audio/webm' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `salam_call_record_${callee}_${Date.now()}.webm`;
  document.body.appendChild(a);
  a.click();
  setTimeout(() => {
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }, 100);
}

function handleCallEndedLocally() {
  stopRingTone();
  if (timerInterval) clearInterval(timerInterval);
  if (isRecording) stopRecording();
  if (mediaStream) {
    mediaStream.getTracks().forEach(t => t.stop());
  }
  document.getElementById('callStatus').innerText = 'কল সমাপ্ত (Call Ended)';
  setTimeout(() => {
    window.location.href = 'history.php';
  }, 1200);
}

function endCall() {
  stopRingTone();
  if (timerInterval) clearInterval(timerInterval);
  if (isRecording) stopRecording();
  if (mediaStream) {
    mediaStream.getTracks().forEach(t => t.stop());
  }

  fetch('api/signal.php', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      action: 'END_CALL',
      sessionId: sessionId,
      duration: callDurationSec
    })
  }).finally(() => {
    window.location.href = 'history.php';
  });
}

document.addEventListener('DOMContentLoaded', initCall);
</script>

</body>
</html>
