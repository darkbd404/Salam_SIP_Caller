/**
 * Salam SIP Caller - Global App Engine v2.7
 * 24/7 Background Incoming Call & Message Notification Engine, Auto Day/Night Mode
 */

// Theme Management
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

// Service Worker Registration
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('./sw.js?v=2.7')
      .then(reg => console.log('SW registered:', reg.scope))
      .catch(err => console.log('SW error:', err));
  });
}

function requestSystemNotificationPermission() {
  if ('Notification' in window && Notification.permission === 'default') {
    Notification.requestPermission();
  }
}
document.addEventListener('click', requestSystemNotificationPermission, { once: true });

// Ringtone Generator
let appAudioCtx = null;
let appRingOsc1 = null, appRingOsc2 = null;
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
    gain.gain.setValueAtTime(0.2, appAudioCtx.currentTime);

    appRingOsc1.connect(gain);
    appRingOsc2.connect(gain);
    gain.connect(appAudioCtx.destination);

    appRingOsc1.start();
    appRingOsc2.start();
    isRingtonePlaying = true;

    if ('vibrate' in navigator) {
      navigator.vibrate([500, 300, 500, 300, 500]);
    }
  } catch (e) {}
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
  if ('vibrate' in navigator) navigator.vibrate(0);
}

// Background Listener
let backgroundPollInterval = null;
let handledSessions = new Set();
let notifiedMessageIds = new Set();

function initIncomingCallListener(myIp) {
  if (!myIp || window.location.pathname.includes('call-active.php')) return;

  if (backgroundPollInterval) clearInterval(backgroundPollInterval);

  backgroundPollInterval = setInterval(async () => {
    try {
      const res = await fetch(`api/signal.php?action=CHECK_INCOMING&my_ip=${encodeURIComponent(myIp)}`);
      const data = await res.json();

      if (data.status === 'INCOMING_CALL' && data.call) {
        const call = data.call;
        if (!handledSessions.has(call.sessionId)) {
          showIncomingCallModal(call);

          if ('Notification' in window && Notification.permission === 'granted') {
            new Notification(`📞 ইনকামিং কল: ${call.callerName || call.callerIp}`, {
              body: `সালাম আইপি (${call.callerIp}) থেকে কল আসছে।`,
              icon: 'assets/icons/icon-192.png'
            });
          }
        }
      } else {
        if (!document.getElementById('incomingModal')) {
          stopIncomingRingtone();
        }
      }

      if (Array.isArray(data.unreadMessages) && data.unreadMessages.length > 0) {
        data.unreadMessages.forEach(msg => {
          if (!notifiedMessageIds.has(msg.id)) {
            notifiedMessageIds.add(msg.id);
            if ('Notification' in window && Notification.permission === 'granted') {
              new Notification(`💬 বার্তা: ${msg.fromName || msg.from}`, {
                body: msg.text || 'নতুন বার্তা এসেছে',
                icon: 'assets/icons/icon-192.png'
              });
            }
          }
        });
      }
    } catch (e) {}
  }, 1800);
}

function showIncomingCallModal(call) {
  startIncomingRingtone();

  let existing = document.getElementById('incomingModal');
  if (existing) existing.remove();

  const isVideo = call.callType === 'VIDEO';
  const modalHtml = `
    <div id="incomingModal" style="position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(2, 16, 14, 0.98); z-index: 999999; display: flex; flex-direction: column; justify-content: space-between; align-items: center; padding: 48px 24px 56px; text-align: center; color: #fff; font-family: 'Hind Siliguri', sans-serif;">
      
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
        <button onclick="rejectIncomingCall('${call.sessionId}')" style="width: 76px; height: 76px; border-radius: 50%; background: #FF5252; border: none; color: #fff; font-size: 28px; cursor: pointer; box-shadow: 0 6px 22px rgba(255, 82, 82, 0.6); display: flex; align-items: center; justify-content: center;" title="প্রত্যাখ্যান করুন">
          <i class="fas fa-phone-slash"></i>
        </button>

        <button onclick="acceptIncomingCall('${call.sessionId}', '${call.callerIp}', '${call.callType}')" style="width: 80px; height: 80px; border-radius: 50%; background: #00E676; border: none; color: #00291B; font-size: 32px; cursor: pointer; box-shadow: 0 6px 26px rgba(0, 230, 118, 0.7); display: flex; align-items: center; justify-content: center;" title="রিসিভ করুন">
          <i class="fas ${isVideo ? 'fa-video' : 'fa-phone'}"></i>
        </button>
      </div>
    </div>
  `;

  document.body.insertAdjacentHTML('beforeend', modalHtml);
}

async function acceptIncomingCall(sessionId, callerIp, callType) {
  handledSessions.add(sessionId);
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
  handledSessions.add(sessionId);
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
}

function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text || '';
  return div.innerHTML;
}
