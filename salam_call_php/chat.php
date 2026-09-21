<?php
require_once __DIR__ . '/includes/config.php';
$user = require_auth();

$peerIp = clean_input($_GET['peer'] ?? ($_GET['recipient'] ?? ($_GET['to'] ?? ($_GET['ip'] ?? ''))));

$users = read_json_data(USER_JSON_FILE);
$contacts = read_json_data(CONTACTS_JSON_FILE);

$peerName = $peerIp;
$peerPhoto = '';

if (!empty($peerIp)) {
    foreach ($users as $u) {
        if (($u['ipNumber'] ?? '') === $peerIp || ($u['mobileNumber'] ?? '') === $peerIp) {
            $peerName = $u['name'];
            $peerPhoto = $u['profilePhoto'] ?? '';
            break;
        }
    }
    if ($peerName === $peerIp) {
        foreach ($contacts as $c) {
            if ($c['number'] === $peerIp) {
                $peerName = $c['name'];
                break;
            }
        }
    }
}

include __DIR__ . '/includes/header.php';
?>

<?php if (empty($peerIp)): ?>
<!-- Start New Chat Dialog if no recipient selected -->
<div class="card">
  <div class="card-title"><i class="fas fa-paper-plane"></i> নতুন বার্তা শুরু করুন</div>
  <form action="chat.php" method="GET">
    <div class="form-group">
      <label class="form-label">প্রাপকের ০9612 আইপি বা মোবাইল নম্বর</label>
      <input type="text" name="peer" class="form-control" placeholder="যেমন: 09612230864" required autofocus>
    </div>
    <button type="submit" class="btn btn-primary"><i class="fas fa-comments"></i> চ্যাট শুরু করুন</button>
  </form>
</div>
<?php else: ?>

<div style="background: var(--card-bg); border-radius: 16px; border: 1px solid var(--card-border); display: flex; flex-direction: column; height: calc(100vh - 165px);">
  
  <!-- Chat Header -->
  <div style="padding: 10px 14px; border-bottom: 1px solid var(--card-border); display: flex; justify-content: space-between; align-items: center; background: var(--input-bg); border-radius: 16px 16px 0 0;">
    <div style="display: flex; align-items: center; gap: 10px; min-width: 0; flex: 1;">
      <a href="messages.php" style="color: var(--text-muted); font-size: 16px; text-decoration: none;"><i class="fas fa-arrow-left"></i></a>
      <div style="width: 36px; height: 36px; min-width: 36px; border-radius: 50%; background: var(--primary); display: flex; align-items: center; justify-content: center; font-size: 14px; color: #fff; overflow: hidden;">
        <?php if (!empty($peerPhoto) && file_exists(__DIR__ . '/' . $peerPhoto)): ?>
          <img src="<?php echo htmlspecialchars($peerPhoto); ?>" style="width: 100%; height: 100%; object-fit: cover;" alt="Avatar">
        <?php else: ?>
          <?php echo mb_substr($peerName, 0, 1, 'UTF-8'); ?>
        <?php endif; ?>
      </div>
      <div style="min-width: 0;">
        <div style="font-size: 14px; font-weight: 700; color: var(--text-main); white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"><?php echo htmlspecialchars($peerName); ?></div>
        <div style="font-size: 11px; color: var(--primary-light);"><?php echo htmlspecialchars($peerIp); ?></div>
      </div>
    </div>

    <!-- Quick Call Actions -->
    <div style="display: flex; gap: 6px; flex-shrink: 0;">
      <a href="call-active.php?callee=<?php echo urlencode($peerIp); ?>&type=AUDIO" class="btn" style="width: 34px; height: 34px; padding: 0; border-radius: 50%; background: #00E676; color: #00291B;" title="অডিও কল">
        <i class="fas fa-phone"></i>
      </a>
      <a href="call-active.php?callee=<?php echo urlencode($peerIp); ?>&type=VIDEO" class="btn" style="width: 34px; height: 34px; padding: 0; border-radius: 50%; background: rgba(0, 230, 118, 0.2); color: #00E676;" title="ভিডিও কল">
        <i class="fas fa-video"></i>
      </a>
    </div>
  </div>

  <!-- Messages Scroll Box -->
  <div id="chatBox" style="flex: 1; padding: 14px; overflow-y: auto; display: flex; flex-direction: column; gap: 8px;">
    <div style="text-align: center; font-size: 11px; color: var(--text-muted); margin-bottom: 6px;">
      <i class="fas fa-shield-halved"></i> এন্ড-টু-এন্ড এনক্রিপ্টেড আইপি বার্তা (বার্তা খরচ ৳ ০.১০)
    </div>
  </div>

  <!-- Message Input Bar -->
  <div style="padding: 10px 12px; border-top: 1px solid var(--card-border); background: var(--input-bg); border-radius: 0 0 16px 16px; display: flex; gap: 8px; align-items: center;">
    <input type="text" id="msgInput" placeholder="মেসেজ লিখুন..." style="flex: 1; background: var(--card-bg); border: 1.5px solid var(--card-border); color: var(--text-main); padding: 10px 14px; border-radius: 20px; font-size: 13px; font-family: inherit;" autocomplete="off">
    <button onclick="sendMessage()" style="width: 40px; height: 40px; min-width: 40px; border-radius: 50%; background: #00E676; border: none; color: #00291B; font-size: 16px; cursor: pointer; display: flex; align-items: center; justify-content: center;">
      <i class="fas fa-paper-plane"></i>
    </button>
  </div>

</div>

<script>
const peerIp = "<?php echo addslashes($peerIp); ?>";
const myIp = "<?php echo addslashes($user['ipNumber']); ?>";
let lastCount = 0;

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
  if (msgs.length === lastCount) return;
  lastCount = msgs.length;

  let html = `<div style="text-align: center; font-size: 11px; color: var(--text-muted); margin-bottom: 6px;"><i class="fas fa-shield-halved"></i> এন্ড-টু-এন্ড এনক্রিপ্টেড আইপি বার্তা (বার্তা খরচ ৳ ০.১০)</div>`;

  msgs.forEach(m => {
    const isMe = (m.from === myIp);
    const time = new Date(m.timestamp * 1000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    html += `
      <div style="display: flex; justify-content: ${isMe ? 'flex-end' : 'flex-start'};">
        <div style="max-width: 78%; background: ${isMe ? 'linear-gradient(135deg, #00897B, #004D40)' : 'var(--input-bg)'}; color: #fff; padding: 8px 12px; border-radius: ${isMe ? '14px 14px 2px 14px' : '14px 14px 14px 2px'}; font-size: 13px; line-height: 1.4; word-break: break-word; border: 1px solid ${isMe ? 'rgba(0,230,118,0.3)' : 'var(--card-border)'};">
          <div>${escapeHtml(m.text)}</div>
          <div style="font-size: 9px; opacity: 0.7; text-align: right; margin-top: 3px;">${time}</div>
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
      body: JSON.stringify({ peer: peerIp, text: text })
    });
    const data = await res.json();
    if (data.success) {
      input.value = '';
      fetchMessages();
    } else {
      alert(data.message || 'মেসেজ পাঠানো সম্ভব হয়নি');
      if (data.needRecharge) {
        window.location.href = 'recharge.php';
      }
    }
  } catch(e) {
    alert('মেসেজ প্রেরণে ত্রুটি হয়েছে');
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

<?php endif; ?>

<?php include __DIR__ . '/includes/footer.php'; ?>
