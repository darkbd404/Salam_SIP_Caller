<?php
require_once __DIR__ . '/includes/config.php';
$user = require_auth();

$allMessages = read_json_data(MESSAGES_JSON_FILE);
$users = read_json_data(USER_JSON_FILE);

// Group messages by peer
$myIp = $user['ipNumber'];
$conversations = [];

foreach ($allMessages as $msg) {
    $peerIp = ($msg['from'] === $myIp) ? $msg['to'] : (($msg['to'] === $myIp) ? $msg['from'] : null);
    if (!$peerIp) continue;

    if (!isset($conversations[$peerIp]) || $msg['timestamp'] > $conversations[$peerIp]['timestamp']) {
        $peerName = $peerIp;
        $peerPhoto = '';
        foreach ($users as $u) {
            if ($u['ipNumber'] === $peerIp) {
                $peerName = $u['name'];
                $peerPhoto = $u['profilePhoto'] ?? '';
                break;
            }
        }
        $conversations[$peerIp] = [
            'peerIp' => $peerIp,
            'peerName' => $peerName,
            'peerPhoto' => $peerPhoto,
            'lastMessage' => $msg['text'],
            'timestamp' => $msg['timestamp']
        ];
    }
}

usort($conversations, function($a, $b) {
    return ($b['timestamp'] ?? 0) - ($a['timestamp'] ?? 0);
});

include __DIR__ . '/includes/header.php';
?>

<div class="card">
  <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
    <div>
      <div class="card-title" style="margin-bottom: 0;"><i class="fas fa-comment-dots"></i> সালাম আইপি মেসেঞ্জার</div>
      <div style="font-size: 11px; color: var(--text-muted);">বার্তা খরচ: ৳ ০.১০ / এসএমএস</div>
    </div>
    <a href="chat.php" class="btn btn-outline" style="width: auto; padding: 6px 12px; font-size: 11px;">
      <i class="fas fa-pen-to-square"></i> নতুন মেসেজ
    </a>
  </div>

  <!-- Quick Start Chat Box -->
  <form action="chat.php" method="GET" style="display: flex; gap: 8px; margin-bottom: 16px;">
    <input type="text" name="peer" class="form-control" placeholder="যেকোনো 09612 আইপি বা মোবাইল লিখে চ্যাট..." style="font-size: 13px; padding: 8px 12px;" required>
    <button type="submit" class="btn btn-primary" style="width: auto; padding: 8px 16px; font-size: 12px; white-space: nowrap;">
      <i class="fas fa-paper-plane"></i> পাঠান
    </button>
  </form>

  <?php if (empty($conversations)): ?>
    <div style="text-align: center; padding: 30px 10px; color: var(--text-muted);">
      <i class="fas fa-comments" style="font-size: 36px; margin-bottom: 12px; opacity: 0.4;"></i>
      <p style="font-size: 14px;">এখনও কোনো বার্তা আদান-প্রদান হয়নি।</p>
      <p style="font-size: 12px; margin-top: 4px;">উপরে নম্বর লিখে অথবা কন্টাক্টস থেকে যেকোনো গ্রাহককে মেসেজ পাঠান।</p>
      <a href="contacts.php" class="btn btn-primary" style="margin-top: 14px; width: auto; display: inline-block;">
        <i class="fas fa-address-book"></i> কন্টাক্টস লিস্ট দেখুন
      </a>
    </div>
  <?php else: ?>
    <div style="display: flex; flex-direction: column; gap: 8px;">
      <?php foreach ($conversations as $c): ?>
        <a href="chat.php?peer=<?php echo urlencode($c['peerIp']); ?>" style="text-decoration: none; color: inherit;">
          <div class="contact-item-card" style="align-items: center;">
            <div class="contact-thumb-avatar">
              <?php if (!empty($c['peerPhoto']) && file_exists(__DIR__ . '/' . $c['peerPhoto'])): ?>
                <img src="<?php echo htmlspecialchars($c['peerPhoto']); ?>" alt="Avatar">
              <?php else: ?>
                <?php echo mb_substr($c['peerName'], 0, 1, 'UTF-8'); ?>
              <?php endif; ?>
            </div>
            
            <div style="flex: 1; min-width: 0;">
              <div style="display: flex; justify-content: space-between; align-items: baseline;">
                <div style="font-size: 14px; font-weight: 700; color: var(--text-main); white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"><?php echo htmlspecialchars($c['peerName']); ?></div>
                <div style="font-size: 10px; color: var(--text-muted);"><?php echo date('h:i A', $c['timestamp']); ?></div>
              </div>
              <div style="font-size: 11px; color: var(--primary-light); font-weight: 600;"><?php echo htmlspecialchars($c['peerIp']); ?></div>
              <div style="font-size: 12px; color: var(--text-muted); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-top: 2px;">
                <?php echo htmlspecialchars($c['lastMessage']); ?>
              </div>
            </div>

            <i class="fas fa-chevron-right" style="color: var(--text-muted); font-size: 12px; margin-left: 8px;"></i>
          </div>
        </a>
      <?php endforeach; ?>
    </div>
  <?php endif; ?>
</div>

<?php include __DIR__ . '/includes/footer.php'; ?>
