/**
 * Salam SIP Caller - Dialer & DTMF Tone Generator
 */

const dtmfFrequencies = {
  '1': [697, 1209], '2': [697, 1336], '3': [697, 1477],
  '4': [770, 1209], '5': [770, 1336], '6': [770, 1477],
  '7': [852, 1209], '8': [852, 1336], '9': [852, 1477],
  '*': [941, 1209], '0': [941, 1336], '#': [941, 1477]
};

let dtmfAudioCtx = null;

function playDtmfTone(char) {
  try {
    if (!dtmfAudioCtx) {
      dtmfAudioCtx = new (window.AudioContext || window.webkitAudioContext)();
    }
    if (dtmfAudioCtx.state === 'suspended') {
      dtmfAudioCtx.resume();
    }

    const freqs = dtmfFrequencies[char];
    if (!freqs) return;

    const osc1 = dtmfAudioCtx.createOscillator();
    const osc2 = dtmfAudioCtx.createOscillator();
    const gainNode = dtmfAudioCtx.createGain();

    osc1.frequency.value = freqs[0];
    osc2.frequency.value = freqs[1];

    gainNode.gain.setValueAtTime(0.12, dtmfAudioCtx.currentTime);
    gainNode.gain.exponentialRampToValueAtTime(0.001, dtmfAudioCtx.currentTime + 0.18);

    osc1.connect(gainNode);
    osc2.connect(gainNode);
    gainNode.connect(dtmfAudioCtx.destination);

    osc1.start();
    osc2.start();

    osc1.stop(dtmfAudioCtx.currentTime + 0.2);
    osc2.stop(dtmfAudioCtx.currentTime + 0.2);

    if ('vibrate' in navigator) {
      navigator.vibrate(20);
    }
  } catch (err) {
    console.error('DTMF Sound error:', err);
  }
}

window.appendDigit = function(digit) {
  const input = document.getElementById('dialerNumber');
  if (!input) return;
  
  playDtmfTone(digit);
  input.value += digit;
  updateNumberDetails();
};

window.backspaceDigit = function() {
  const input = document.getElementById('dialerNumber');
  if (!input) return;
  input.value = input.value.slice(0, -1);
  updateNumberDetails();
};

function updateNumberDetails() {
  const input = document.getElementById('dialerNumber');
  const details = document.getElementById('calleeType');
  if (!input || !details) return;

  const val = input.value.trim();
  if (val.startsWith('09612')) {
    details.innerHTML = `<span style="color: #00E676; font-weight: bold;"><i class="fas fa-satellite-dish"></i> সালাম আইপি টু আইপি এইচডি ভয়েস / ভিডিও কল</span>`;
  } else if (val.startsWith('01') && val.length === 11) {
    details.innerHTML = `<span style="color: #4DB6AC;"><i class="fas fa-tower-cell"></i> বিটিআরসি অনুমোদিত জিএসএম রুট (৳ ০.৩০/মিনিট)</span>`;
  } else {
    details.innerHTML = `যেকোনো ০9612 আইপি বা ১১ ডিজিটের মোবাইল নম্বর ডায়াল করুন`;
  }
}
