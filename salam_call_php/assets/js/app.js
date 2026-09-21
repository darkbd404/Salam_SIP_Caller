/**
 * Salam SIP Caller - Global App Engine v2.6
 * 24/7 Background Incoming Call & Message Notification Engine, Auto Day/Night Mode
 */

// Auto & Manual Theme Management (Day Mode / Night Mode)
(function initTheme() {
  const savedTheme = localStorage.getItem('salam_theme');
  if (savedTheme) {
    document.documentElement.setAttribute('data-theme', savedTheme);
  } else {
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    document.documentElement.setAttribute('data-theme', prefersDark ? 'dark' : 'light');
  }
  updateThemeIcon();
})();

window.toggleTheme = function() {
  const current = document.documentElement.getAttribute('data-theme') || 'dark';
  const next = current === 'dark' ? 'light' : 'dark';
  document.documentElement.setAttribute('data-theme', next);
  localStorage.setItem('salam_theme', next);
  updateThemeIcon();
};

function updateThemeIcon() {
  const current = document.documentElement.getAttribute('data-theme') || 'dark';
  const icon = document.getElementById('themeIcon');
  if (icon) {
    icon.className = current === 'dark' ? 'fas fa-sun' : 'fas fa-moon';
  }
}

// Register Service Worker for PWA & Background Notifications
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('./sw.js?v=2.6')
      .then(reg => {
        console.log('Salam SIP Service Worker registered:', reg.scope);
      })
      .catch(err => console.log('SW registration error:', err));
  });
}

// Request Notification Permission on first user touch/click
function requestSystemNotificationPermission() {
  if ('Notification' in window && Notification.permission === 'default') {
    Notification.requestPermission().then(perm => {
      console.log('Notification permission status:', perm);
    });
  }
}
document.addEventListener('click', requestSystemNotificationPermission, { once: true });

// PWA Install Prompt
let deferredPrompt = null;
const installBanner = document.getElementById('pwaInstallBanner');
const installBtn = document.getElementById('pwaInstallBtn');

window.addEventListener('beforeinstallprompt', (e) => {
  e.preventDefault();
  deferredPrompt = e;
  if (installBanner) installBanner.style.display = 'flex';
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

// Web Audio Ringtone Generator
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
    gain.gain.setValueAtTime(0.18, appAudioCtx.currentTime);

    appRingOsc1.connect(gain);
    appRingOsc2.connect(gain);
    gain.connect(appAudioCtx.destination);

    appRingOsc1.start();
    appRingOsc2.start();
    isRingtonePlaying = true;

    if ('vibrate' in navigator) {
      navigator.vibrate([500, 300, 500, 300, 500]);
    }
  } catch (e) {
    console.log('Audio error:', e);
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

// 24/7 Global Background Listener for Incoming Calls & Messages
let backgroundPollInterval = null;
let activeIncomingSession = null;
let notifiedMessageIds = new Set();

function initIncomingCallListener(myIp) {
  if (!myIp || window.location.pathname.includes('call-active.php')) return;

  if (backgroundPollInterval) clearInterval(backgroundPollInterval);

  backgroundPollInterval = setInterval(async () => {
    try {
      const res = await fetch(`api/signal.php?action=CHECK_INCOMING&my_ip=${encodeURIComponent(myIp)}`);
      const data = await res.json();

      // 1. Handle Incoming Call
      if (data.status === 'INCOMING_CALL' && data.call) {
        const call = data.call;
        if (activeIncomingSession !== call.sessionId) {
          activeIncomingSession = call.sessionId;
          showIncomingCallModal(call);

          // Trigger System Notification in Android status bar
          if ('Notification' in window && Notification.permission === 'granted') {
            if (navigator.serviceWorker && navigator.serviceWorker.controller) {
              navigator.serviceWorker.controller.postMessage({
                type: 'CALL_NOTIFICATION',
                title: `📞 ইনকামিং কল: ${call.callerName || call.callerIp}`,
                body: `সালাম আইপি (${call.callerIp}) থেকে কল আসছে। উত্তর দিতে ক্লিক করুন।`
              });
            } else {
              new Notification(`📞 ইনকামিং কল: ${call.callerName || call.callerIp}`, {
                body: `সালাম আইপি (${call.callerIp}) থেকে কল আসছে।`,
                icon: 'assets/icons/icon-192.png',
                vibrate: [300, 100, 300]
              });
            }
          }
        }
      } else {
        if (activeIncomingSession && !document.getElementById('incomingModal')) {
          activeIncomingSession = null;
          stopIncomingRingtone();
        }
      }

      // 2. Handle Unread Messages in Notification Bar
      if (Array.isArray(data.unreadMessages) && data.unreadMessages.length > 0) {
        data.unreadMessages.forEach(msg => {
          if (!notifiedMessageIds.has(msg.id)) {
            notifiedMessageIds.add(msg.id);
            if ('Notification' in window && Notification.permission === 'granted') {
              new Notification(`💬 নতুন বার্তা: ${msg.senderName || msg.senderIp}`, {
                body: msg.text || 'নতুন বার্তা পাঠানো হয়েছে।',
                icon: 'assets/icons/icon-192.png'
              });
            }
          }
        });
      }

    } catch (e) {
      // Silently poll in background
    }
  }, 1800);
}

// Show Fullscreen Incoming Call UI
function showIncomingCallModal(call) {
  startIncomingRingtone();

  let existingModal = document.getElementById('incomingModal');
  if (existingModal) existingModal.remove();

  const isVideo = call.callType === 'VIDEO';
  const modalHtml = `
    <div id="incomingModal" style="position: fixed; inset: 0; background: rgba(1, 15, 13, 0.97); z-index: 999999; display: flex; flex-direction: column; justify-content: space-between; align-items: center; padding: 48px 24px 56px; text-align: center; color: #fff; font-family: 'Hind Siliguri', sans-serif;">
      
      <div>
        <div style="background: rgba(0, 230, 118, 0.18); border: 1px solid #00E676; color: #00E676; padding: 6px 16px; border-radius: 20px; font-size: 13px; display: inline-flex; align-items: center; gap: 6px; margin-bottom: 14px; font-weight: 700;">
          <i class="fas ${isVideo ? 'fa-video' : 'fa-phone-volume'}"></i> ${isVideo ? 'HD ভিডিও কল আসছে...' : 'HD অডিও কল আসছে...'}
        </div>
        <h2 style="font-size: 28px; font-weight: 700; margin: 6px 0;">${escapeHtml(call.callerName || 'সালাম কলার')}</h2>
        <div style="font-size: 16px; color: #80CBC4; font-weight: 600; letter-spacing: 1px;">আইপি: ${escapeHtml(call.callerIp)}</div>
        <div style="font-size: 12px; color: #A7FFEB; margin-top: 4px;">09612 BTRC Official Routing</div>
      </div>

      <div style="position: relative; width: 140px; height: 140px; display: flex; align-items: center; justify-content: center;">
        <div style="position: absolute; inset: -15px; border-radius: 50%; background: rgba(0, 230, 118, 0.2); animation: pulse 1.5s infinite;"></div>
        <div style="width: 130px; height: 130px; border-radius: 50%; background: linear-gradient(135deg, #00897B, #00E676); display: flex; align-items: center; justify-content: center; font-size: 52px; color: #fff; position: relative; z-index: 2; box-shadow: 0 0 30px rgba(0, 230, 118, 0.6); overflow: hidden;">
          ${call.callerPhoto ? `<img src="${call.callerPhoto}" style="width: 100%; height: 100%; object-fit: cover;" alt="Avatar">` : '<i class="fas fa-user"></i>'}
        </div>
      </div>

      <div style="width: 100%; max-width: 320px; display: flex; justify-content: space-around; align-items: center;">
        
        <!-- Reject Button -->
        <button onclick="rejectIncomingCall('${call.sessionId}')" style="width: 76px; height: 76px; border-radius: 50%; background: #FF5252; border: none; color: #fff; font-size: 28px; cursor: pointer; box-shadow: 0 6px 22px rgba(255, 82, 82, 0.6); display: flex; align-items: center; justify-content: center;" title="প্রত্যাখ্যান করুন">
          <i class="fas fa-phone-slash"></i>
        </button>

        <!-- Accept Button -->
        <button onclick="acceptIncomingCall('${call.sessionId}', '${call.callerIp}', '${call.callType}')" style="width: 80px; height: 80px; border-radius: 50%; background: #00E676; border: none; color: #00291B; font-size: 32px; cursor: pointer; box-shadow: 0 6px 26px rgba(0, 230, 118, 0.7); display: flex; align-items: center; justify-content: center;" title="রিসিভ করুন">
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

// Clean FormSubmit OTP helper without redirect loops
window.sendOtpViaFormSubmit = async function(email, name, otpCode, ipNumber, mobile) {
  try {
    const formData = new FormData();
    formData.append('email', email);
    formData.append('_subject', 'Salam SIP Caller - একাউন্ট ওটিপি কোড: ' + otpCode);
    formData.append('_template', 'box');
    formData.append('_captcha', 'false');
    formData.append('Verification_Code', otpCode);
    formData.append('User_Name', name);
    formData.append('Mobile_Number', mobile);
    formData.append('Allocated_IP_Number', ipNumber);

    // Send directly to AJAX endpoint
    fetch('https://formsubmit.co/ajax/' + encodeURIComponent('salam230864@gmail.com'), {
      method: 'POST',
      body: formData
    }).catch(()=>{});

    return true;
  } catch (err) {
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
