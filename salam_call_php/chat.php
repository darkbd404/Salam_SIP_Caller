<?php
require_once __DIR__ . '/includes/config.php';
$user = require_auth();

$peerIp = clean_input($_GET['peer'] ?? '');
if (empty($peerIp)) {
    header('Location: messages.php');
    exit;
}

$users = read_json_data(USER_JSON_FILE);
$peerName = $peerIp;
$peerPhoto = '';
foreach ($users as $u) {
    if ($u['ipNumber'] === $peerIp) {
        $peerName = $u['name'];
        $peerPhoto = $u['profilePhoto'] ?? '';
        break;
    }
}

include __DIR__ . '/includes/header.php';
?>

<div style="background: rgba(0,0,0,0.3); border-radius: 14px; border: 1px solid var(--card-border); display: flex; flex-direction: column; height: calc(100vh - 160px);">
  
  <!-- Chat Header -->
  <div style="padding: 12px 16px; border-bottom: 1px solid var(--card-border); display: flex; justify-content: space-between; align-items: center; background: rgba(0, 77, 64, 0.25); border-radius: 14px 14px 0 0;">
    <div style="display: flex; align-items: center; gap: 10px;">
      <a href="messages.php" style="color: var(--text-muted); font-size: 16px; text-decoration: none;"><i class="fas fa-arrow-left"></i></a>
      <div style="width: 36px; height: 36px; border-radius: 50%; background: var(--primary); display: flex; align-items: center; justify-content: center; font-size: 16px; color: #fff; overflow: hidden;">
        <?php if (!empty($peerPhoto) && file_exists(__DIR__ . '/' . $peerPhoto)): ?>
          <img src="<?php echo htmlspecialchars($peerPhoto); ?>" style="width: 100%; height: 100%; object-fit: cover;" alt="Avatar">
        <?php else: ?>
          <i class="fas fa-user"></i>
        <?php endif; ?>
      </div>
      <div>
        <div style="font-size: 14px; font-weight: 700; color: #fff;"><?php echo htmlspecialchars($peerName); ?></div>
        <div style="font-size: 11px; color: var(--primary-light);"><?php echo htmlspecialchars($peerIp); ?></div>
      </div>
    </div>

    <!-- Quick Call Buttons -->
    <div style="display: flex; gap: 8px;">
      <a href="call-active.php?callee=<?php echo urlencode($peerIp); ?>&type=AUDIO" class="btn-action btn-audio" style="width: 34px; height: 34px; font-size: 13px;" title="অডিও কল">
        <i class="fas fa-phone"></i>
      </a>
      <a href="call-active.php?callee=<?php echo urlencode($peerIp); ?>&type=VIDEO" class="btn-action btn-video" style="width: 34px; height: 34px; font-size: 13px;" title="ভিডিও কল">
        <i class="fas fa-video"></i>
      </a>
    </div>
  </div>

  <!-- Messages Scroll Area -->
  <div id="chatBox" style="flex: 1; padding: 14px; overflow-y: auto; display: flex; flex-direction: column; gap: 10px;">
    <div style="text-align: center; font-size: 11px; color: var(--text-muted); margin-bottom: 10px;">
      <i class="fas fa-lock"></i> এন্ড-টু-এন্ড সুরক্ষিত সালাম আইপি বার্তা (প্রতি এসএমএস ৳ ০.১০)
    </div>
  </div>

  <!-- Message Input Area -->
  <div style="padding: 10px 12px; border-top: 1px solid var(--card-border); background: rgba(0,0,0,0.4); border-radius: 0 0 14px 14px; display: flex; gap: 8px; align-items: center;">
    <input type="text" id="msgInput" placeholder="মেসেজ লিখুন..." style="flex: 1; background: rgba(255,255,255,0.08); border: 1px solid var(--card-border); color: #fff; padding: 10px 14px; border-radius: 20px; font-size: 13px;" autocomplete="off">
    <button onclick="sendMessage()" style="width: 42px; height: 42px; border-radius: 50%; background: var(--success); border: none; color: #000; font-size: 16px; cursor: pointer; display: flex; align-items: center; justify-content: center;">
      <i class="fas fa-paper-plane"></i>
    </button>
  </div>

</div>

<script>
const peerIp = "<?php echo addslashes($peerIp); ?>";
const myIp = "<?php echo addslashes($user['ipNumber']); ?>";
let lastMsgCount = 0;

async function fetchMessages() {
  try {
    const res = await fetch(`api/messages.php?action=GET_MESSAGES&peer=${encodeURIComponent(peerIp)}`);
    const data = await res.json();
    if (data.success && data.messages) {
      renderMessages(data.messages);
    }
  } catch(e){}
}

function renderMessages(msgs) {
  const box = document.getElementById('chatBox');
  if (msgs.length === lastMsgCount) return;
  lastMsgCount = msgs.length;

  let html = `<div style="text-align: center; font-size: 11px; color: var(--text-muted); margin-bottom: 10px;"><i class="fas fa-lock"></i> এন্ড-টু-এন্ড সুরক্ষিত সালাম আইপি বার্তা (প্রতি এসএমএস ৳ ০.১০)</div>`;

  msgs.forEach(m => {
    const isMe = m.from === myIp;
    const time = new Date(m.timestamp * 1000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    html += `
      <div style="display: flex; justify-content: ${isMe ? 'flex-end' : 'flex-start'};">
        <div style="max-width: 75%; background: ${isMe ? '#00897B' : 'rgba(255,255,255,0.12)'}; color: #fff; padding: 8px 12px; border-radius: ${isMe ? '14px 14px 2px 14px' : '14px 14px 14px 2px'}; font-size: 13px; line-height: 1.4; word-break: break-word;">
          <div>${escapeHtml(m.text)}</div>
          <div style="font-size: 9px; opacity: 0.7; text-align: right; margin-top: 2px;">${time}</div>
        </div>
      </div>
    `;
  });

  box.innerHTML = html;
  box.scrollTop = box.scrollHeight;
}

async function sendMessage() {
  const input = document.getElementById('msgInput');
  const text = input.value.trim();
  if (!text) return;

  try {
    const res = await fetch('api/messages.php?action=SEND_MESSAGE', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ peer: peerIp, text })
    });
    const data = await res.json();
    if (data.success) {
      input.value = '';
      fetchMessages();
    } else {
      alert(data.message || 'মেসেজ পাঠানো যায়নি');
      if (data.needRecharge) {
        window.location.href = 'recharge.php';
      }
    }
  } catch(e){
    alert('নেটওয়ার্ক ত্রুটি! মেসেজ পাঠানো সম্ভব হয়নি।');
  }
}

document.getElementById('msgInput').addEventListener('keydown', (e) => {
  if (e.key === 'Enter') sendMessage();
});

document.addEventListener('DOMContentLoaded', () => {
  fetchMessages();
  setInterval(fetchMessages, 2000);
});
</script>

<?php include __DIR__ . '/includes/footer.php'; ?>
