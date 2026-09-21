/**
 * Salam SIP Caller - Global App Script v2.5
 * WebRTC Signaling, Incoming Call Popup, PWA Installer & FormSubmit AJAX
 */

// Register Service Worker
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('./sw.js?v=2.5')
      .then(reg => console.log('Salam SIP Service Worker registered:', reg.scope))
      .catch(err => console.log('SW registration failed:', err));
  });
}

// PWA Install Prompt Handler
let deferredPrompt = null;
const installBanner = document.getElementById('pwaInstallBanner');
const installBtn = document.getElementById('pwaInstallBtn');

window.addEventListener('beforeinstallprompt', (e) => {
  e.preventDefault();
  deferredPrompt = e;
  if (installBanner) {
    installBanner.style.display = 'flex';
  }
});

if (installBtn) {
  installBtn.addEventListener('click', async () => {
    if (deferredPrompt) {
      deferredPrompt.prompt();
      const { outcome } = await deferredPrompt.userChoice;
      console.log('User response to install prompt:', outcome);
      deferredPrompt = null;
      if (installBanner) installBanner.style.display = 'none';
    }
  });
}

// Global Web Audio Tone Generator
let appAudioCtx = null;
let appRingOsc1 = null;
let appRingOsc2 = null;
let isRingtonePlaying = false;

function startIncomingRingtone() {
  if (isRingtonePlaying) return;
  try {
    appAudioCtx = new (window.AudioContext || window.webkitAudioContext)();
    if (appAudioCtx.state === 'suspended') appAudioCtx.resume();

    appRingOsc1 = appAudioCtx.createOscillator();
    appRingOsc2 = appAudioCtx.createOscillator();
    const gain = appAudioCtx.createGain();

    appRingOsc1.frequency.value = 440;
    appRingOsc2.frequency.value = 480;
    gain.gain.setValueAtTime(0.15, appAudioCtx.currentTime);

    appRingOsc1.connect(gain);
    appRingOsc2.connect(gain);
    gain.connect(appAudioCtx.destination);

    appRingOsc1.start();
    appRingOsc2.start();
    isRingtonePlaying = true;

    // Trigger Vibration if supported
    if ('vibrate' in navigator) {
      navigator.vibrate([400, 300, 400, 300, 400]);
    }
  } catch (e) {
    console.log('Audio Context error:', e);
  }
}

function stopIncomingRingtone() {
  if (appRingOsc1) {
    try {
      appRingOsc1.stop();
      appRingOsc2.stop();
    } catch(e) {}
    appRingOsc1 = null;
    appRingOsc2 = null;
  }
  isRingtonePlaying = false;
  if ('vibrate' in navigator) {
    navigator.vibrate(0);
  }
}

// Global Incoming Call Listener
let incomingCheckInterval = null;
let activeIncomingSession = null;

function initIncomingCallListener(myIp) {
  if (!myIp || window.location.pathname.includes('call-active.php')) return;

  if (incomingCheckInterval) clearInterval(incomingCheckInterval);

  incomingCheckInterval = setInterval(async () => {
    try {
      const res = await fetch(`api/signal.php?action=CHECK_INCOMING&my_ip=${encodeURIComponent(myIp)}`);
      const data = await res.json();

      if (data.status === 'INCOMING_CALL' && data.call) {
        const call = data.call;
        if (activeIncomingSession !== call.sessionId) {
          activeIncomingSession = call.sessionId;
          showIncomingCallModal(call);
        }
      } else {
        if (activeIncomingSession && !document.getElementById('incomingModal')) {
          activeIncomingSession = null;
          stopIncomingRingtone();
        }
      }
    } catch (e) {
      // Background poll silently
    }
  }, 2000);
}

// Show Fullscreen Incoming Call UI
function showIncomingCallModal(call) {
  startIncomingRingtone();

  let existingModal = document.getElementById('incomingModal');
  if (existingModal) existingModal.remove();

  const isVideo = call.callType === 'VIDEO';
  const modalHtml = `
    <div id="incomingModal" style="position: fixed; inset: 0; background: rgba(0, 20, 18, 0.96); z-index: 999999; display: flex; flex-direction: column; justify-content: space-between; align-items: center; padding: 40px 20px 50px; text-align: center; color: #fff; font-family: 'Hind Siliguri', sans-serif;">
      
      <div>
        <div style="background: rgba(0, 230, 118, 0.15); border: 1px solid #00E676; color: #00E676; padding: 6px 14px; border-radius: 20px; font-size: 13px; display: inline-block; margin-bottom: 15px; font-weight: 700;">
          <i class="fas ${isVideo ? 'fa-video' : 'fa-phone-volume'}"></i> ${isVideo ? 'HD ভিডিও কল আসছে...' : 'HD অডিও কল আসছে...'}
        </div>
        <h2 style="font-size: 26px; font-weight: 700; margin: 8px 0;">${escapeHtml(call.callerName || 'সালাম কলার')}</h2>
        <div style="font-size: 16px; color: #4DB6AC; font-weight: 600; letter-spacing: 1px;">আইপি: ${escapeHtml(call.callerIp)}</div>
        <div style="font-size: 12px; color: #A7FFEB; margin-top: 4px;">09612 BTRC Verified Routing</div>
      </div>

      <div>
        <div style="width: 120px; height: 120px; border-radius: 50%; background: linear-gradient(135deg, #00897B, #00E676); display: flex; align-items: center; justify-content: center; font-size: 48px; color: #fff; margin: 0 auto; box-shadow: 0 0 25px rgba(0, 230, 118, 0.6); animation: pulse 1.5s infinite;">
          <i class="fas fa-user"></i>
        </div>
      </div>

      <div style="width: 100%; max-width: 320px; display: flex; justify-content: space-around; align-items: center;">
        
        <!-- Reject Button -->
        <button onclick="rejectIncomingCall('${call.sessionId}')" style="width: 72px; height: 72px; border-radius: 50%; background: #FF5252; border: none; color: #fff; font-size: 26px; cursor: pointer; box-shadow: 0 4px 20px rgba(255, 82, 82, 0.5); display: flex; align-items: center; justify-content: center;">
          <i class="fas fa-phone-slash"></i>
        </button>

        <!-- Accept Button -->
        <button onclick="acceptIncomingCall('${call.sessionId}', '${call.callerIp}', '${call.callType}')" style="width: 76px; height: 76px; border-radius: 50%; background: #00E676; border: none; color: #000; font-size: 30px; cursor: pointer; box-shadow: 0 4px 25px rgba(0, 230, 118, 0.6); display: flex; align-items: center; justify-content: center;">
          <i class="fas ${isVideo ? 'fa-video' : 'fa-phone'}"></i>
        </button>

      </div>
    </div>
  `;

  document.body.insertAdjacentHTML('beforeend', modalHtml);
}

async function acceptIncomingCall(sessionId, callerIp, callType) {
  stopIncomingRingtone();
  try {
    await fetch('api/signal.php', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'ACCEPT_CALL', sessionId })
    });
  } catch(e){}

  const modal = document.getElementById('incomingModal');
  if (modal) modal.remove();

  window.location.href = `call-active.php?session_id=${sessionId}&callee=${encodeURIComponent(callerIp)}&type=${callType}&incoming=1`;
}

async function rejectIncomingCall(sessionId) {
  stopIncomingRingtone();
  try {
    await fetch('api/signal.php', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'REJECT_CALL', sessionId })
    });
  } catch(e){}

  const modal = document.getElementById('incomingModal');
  if (modal) modal.remove();
  activeIncomingSession = null;
}

// FormSubmit AJAX OTP Dispatcher (Secret OTP directly to user Gmail)
window.sendOtpViaFormSubmit = async function(email, name, otpCode, ipNumber, mobile) {
  try {
    const formData = new FormData();
    formData.append('email', email);
    formData.append('_subject', 'Salam SIP Caller - আপনার একাউন্ট ভেরিফিকেশন ওটিপি');
    formData.append('_template', 'table');
    formData.append('_captcha', 'false');
    formData.append('_cc', 'salam230864@gmail.com');
    formData.append('Verification_Code', otpCode);
    formData.append('User_Name', name);
    formData.append('Mobile_Number', mobile);
    formData.append('Allocated_IP_Number', ipNumber);
    formData.append('System_Notice', 'সালাম এসআইপি কলার সিস্টেমে রেজিস্ট্রেশন সম্পন্ন করতে এই ৬ ডিজিটের ওটিপি কোডটি ব্যবহার করুন।');

    await fetch('https://formsubmit.co/ajax/' + encodeURIComponent(email), {
      method: 'POST',
      body: formData
    });
    return true;
  } catch (err) {
    console.error('FormSubmit error:', err);
    return false;
  }
};

// Image Preview Helper
window.previewNidImage = function(input, previewId, textId) {
  if (input.files && input.files[0]) {
    const reader = new FileReader();
    reader.onload = function(e) {
      const preview = document.getElementById(previewId);
      if (preview) {
        preview.src = e.target.result;
        preview.style.display = 'block';
      }
      const textElem = document.getElementById(textId);
      if (textElem) {
        textElem.innerText = '✓ সিলেক্ট হয়েছে';
        textElem.style.color = '#00E676';
      }
    };
    reader.readAsDataURL(input.files[0]);
  }
};

function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text || '';
  return div.innerHTML;
}
