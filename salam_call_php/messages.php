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

// Sort conversations by most recent
usort($conversations, fn($a, $b) => $b['timestamp'] <=> $a['timestamp']);

include __DIR__ . '/includes/header.php';
?>

<div class="card">
  <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
    <div>
      <div class="card-title" style="margin-bottom: 0;"><i class="fas fa-comment-dots"></i> সালাম আইপি মেসেঞ্জার</div>
      <div style="font-size: 11px; color: var(--text-muted);">বার্তা খরচ: ৳ ০.১০ / এসএমএস</div>
    </div>
    <a href="contacts.php" class="btn btn-outline" style="width: auto; padding: 6px 12px; font-size: 11px;">
      <i class="fas fa-plus"></i> নতুন মেসেজ
    </a>
  </div>

  <?php if (empty($conversations)): ?>
    <div style="text-align: center; padding: 40px 10px; color: var(--text-muted);">
      <i class="fas fa-comments" style="font-size: 40px; margin-bottom: 12px; opacity: 0.4;"></i>
      <p style="font-size: 14px;">এখনও কোনো বার্তা আদান-প্রদান হয়নি।</p>
      <p style="font-size: 12px;">কন্টাক্টস থেকে যেকোনো ০9612 আইপি নাম্বারে সরাসরি মেসেজ পাঠান।</p>
      <a href="contacts.php" class="btn btn-primary" style="margin-top: 15px; width: auto; display: inline-block;">
        <i class="fas fa-address-book"></i> কন্টাক্টস লিস্ট দেখুন
      </a>
    </div>
  <?php else: ?>
    <div style="display: flex; flex-direction: column; gap: 10px;">
      <?php foreach ($conversations as $c): ?>
        <a href="chat.php?peer=<?php echo urlencode($c['peerIp']); ?>" style="text-decoration: none; color: inherit;">
          <div class="contact-card" style="align-items: center;">
            <div class="contact-avatar" style="overflow: hidden; width: 44px; height: 44px;">
              <?php if (!empty($c['peerPhoto']) && file_exists(__DIR__ . '/' . $c['peerPhoto'])): ?>
                <img src="<?php echo htmlspecialchars($c['peerPhoto']); ?>" style="width: 100%; height: 100%; object-fit: cover;" alt="Avatar">
              <?php else: ?>
                <i class="fas fa-user"></i>
              <?php endif; ?>
            </div>
            
            <div style="flex: 1; min-width: 0;">
              <div style="display: flex; justify-content: space-between; align-items: baseline;">
                <div style="font-size: 14px; font-weight: 700;"><?php echo htmlspecialchars($c['peerName']); ?></div>
                <div style="font-size: 10px; color: var(--text-muted);"><?php echo date('h:i A', $c['timestamp']); ?></div>
              </div>
              <div style="font-size: 11px; color: var(--primary-light); font-weight: 600;"><?php echo htmlspecialchars($c['peerIp']); ?></div>
              <div style="font-size: 12px; color: var(--text-muted); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-top: 2px;">
                <?php echo htmlspecialchars($c['lastMessage']); ?>
              </div>
            </div>

            <i class="fas fa-chevron-right" style="color: var(--text-muted); font-size: 12px;"></i>
          </div>
        </a>
      <?php endforeach; ?>
    </div>
  <?php endif; ?>
</div>

<?php include __DIR__ . '/includes/footer.php'; ?>
