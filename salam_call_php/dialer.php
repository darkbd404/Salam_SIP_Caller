<?php
require_once __DIR__ . '/includes/config.php';
$user = require_auth();

$initialNumber = clean_input($_GET['number'] ?? '');

include __DIR__ . '/includes/header.php';
?>

<div class="card" style="padding: 14px 10px; margin-bottom: 0;">
  
  <!-- Number Display -->
  <div class="dialer-display">
    <div style="font-size: 11px; color: var(--text-muted); margin-bottom: 4px; display: flex; justify-content: space-between;">
      <span>কলার আইপি: <strong style="color: var(--success);"><?php echo htmlspecialchars($user['ipNumber']); ?></strong></span>
      <span>ব্যালেন্স: <strong style="color: #FFD54F;">৳ <?php echo number_format($user['balance'] ?? 0, 2); ?></strong></span>
    </div>
    
    <input type="tel" id="dialerNumber" class="dialer-number-input" placeholder="নম্বর তুলুন..." autocomplete="off" value="<?php echo htmlspecialchars($initialNumber); ?>">
    
    <div style="font-size: 11px; color: var(--primary-light);" id="calleeType">যেকোনো ০9612 আইপি বা ১১ ডিজিটের মোবাইল নম্বর ডায়াল করুন</div>
  </div>

  <!-- Keypad Grid -->
  <div class="dialer-grid">
    <div class="dial-btn" onclick="appendDigit('1')">
      <span class="dial-digit">1</span>
      <span class="dial-sub">⚬</span>
    </div>
    <div class="dial-btn" onclick="appendDigit('2')">
      <span class="dial-digit">2</span>
      <span class="dial-sub">ABC</span>
    </div>
    <div class="dial-btn" onclick="appendDigit('3')">
      <span class="dial-digit">3</span>
      <span class="dial-sub">DEF</span>
    </div>

    <div class="dial-btn" onclick="appendDigit('4')">
      <span class="dial-digit">4</span>
      <span class="dial-sub">GHI</span>
    </div>
    <div class="dial-btn" onclick="appendDigit('5')">
      <span class="dial-digit">5</span>
      <span class="dial-sub">JKL</span>
    </div>
    <div class="dial-btn" onclick="appendDigit('6')">
      <span class="dial-digit">6</span>
      <span class="dial-sub">MNO</span>
    </div>

    <div class="dial-btn" onclick="appendDigit('7')">
      <span class="dial-digit">7</span>
      <span class="dial-sub">PQRS</span>
    </div>
    <div class="dial-btn" onclick="appendDigit('8')">
      <span class="dial-digit">8</span>
      <span class="dial-sub">TUV</span>
    </div>
    <div class="dial-btn" onclick="appendDigit('9')">
      <span class="dial-digit">9</span>
      <span class="dial-sub">WXYZ</span>
    </div>

    <div class="dial-btn" onclick="appendDigit('*')">
      <span class="dial-digit">*</span>
      <span class="dial-sub">⁺</span>
    </div>
    <div class="dial-btn" onclick="appendDigit('0')">
      <span class="dial-digit">0</span>
      <span class="dial-sub">+</span>
    </div>
    <div class="dial-btn" onclick="appendDigit('#')">
      <span class="dial-digit">#</span>
      <span class="dial-sub">⌗</span>
    </div>
  </div>

  <!-- Action Row with Audio & Video Calling -->
  <div style="display: flex; justify-content: space-around; align-items: center; padding: 10px 20px;">
    
    <!-- Video Call -->
    <button type="button" onclick="initiateCall('VIDEO')" style="width: 52px; height: 52px; border-radius: 50%; background: rgba(0, 230, 118, 0.15); border: 1px solid #00E676; color: #00E676; font-size: 20px; cursor: pointer; display: flex; align-items: center; justify-content: center;" title="এইচডি ভিডিও কল">
      <i class="fas fa-video"></i>
    </button>

    <!-- Main Audio Call Button -->
    <button type="button" class="btn-call" onclick="initiateCall('AUDIO')" title="এইচডি অডিও কল">
      <i class="fas fa-phone"></i>
    </button>

    <!-- Backspace -->
    <button type="button" class="btn-backspace" onclick="backspaceDigit()" title="মুছুন">
      <i class="fas fa-delete-left"></i>
    </button>
  </div>

</div>

<script>
const userBalance = <?php echo (float)($user['balance'] ?? 0); ?>;

function initiateCall(type) {
  const num = document.getElementById('dialerNumber').value.trim();
  if (!num) {
    alert('অনুগ্রহ করে একটি নম্বর ডায়াল করুন');
    return;
  }
  if (userBalance < 0.30) {
    alert('কল করার জন্য পর্যাপ্ত ব্যালেন্স নেই (ন্যূনতম ৳ ০.৩০)। অনুগ্রহ করে রিচার্জ করুন।');
    window.location.href = 'recharge.php';
    return;
  }
  window.location.href = `call-active.php?callee=${encodeURIComponent(num)}&type=${type}`;
}

document.addEventListener('keydown', (e) => {
  const allowed = '0123456789*#';
  if (allowed.includes(e.key)) {
    window.appendDigit(e.key);
  } else if (e.key === 'Backspace') {
    window.backspaceDigit();
  } else if (e.key === 'Enter') {
    initiateCall('AUDIO');
  }
});
</script>

<?php include __DIR__ . '/includes/footer.php'; ?>
