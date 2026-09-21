<?php
require_once __DIR__ . '/includes/config.php';
$user = require_auth();

$allCalls = read_json_data(CALLS_JSON_FILE);
$userCalls = [];
foreach ($allCalls as $c) {
    if (($c['userId'] ?? 0) === $user['id'] || ($c['callerNumber'] ?? '') === $user['ipNumber']) {
        $userCalls[] = $c;
    }
}
$userCalls = array_reverse($userCalls); // newest first

include __DIR__ . '/includes/header.php';
?>

<div class="card">
  <div class="card-title"><i class="fas fa-clock-rotate-left"></i> সাম্প্রতিক কল হিস্ট্রি</div>

  <?php if (empty($userCalls)): ?>
    <div style="text-align: center; padding: 40px 10px; color: var(--text-muted);">
      <i class="fas fa-phone-slash" style="font-size: 32px; margin-bottom: 10px; opacity: 0.5;"></i>
      <p style="font-size: 13px;">কোনো সাম্প্রতিক কল রেকর্ড নেই।</p>
    </div>
  <?php else: ?>
    <div style="display: flex; flex-direction: column; gap: 10px;">
      <?php foreach ($userCalls as $call): 
        $durMin = floor(($call['durationSeconds'] ?? 0) / 60);
        $durSec = ($call['durationSeconds'] ?? 0) % 60;
        $durText = sprintf("%02d:%02d", $durMin, $durSec);
      ?>
        <div style="display: flex; justify-content: space-between; align-items: center; background: rgba(0,0,0,0.25); padding: 12px 14px; border-radius: 12px; border: 1px solid rgba(77, 182, 172, 0.15);">
          <div style="display: flex; align-items: center; gap: 12px;">
            <div style="width: 38px; height: 38px; border-radius: 50%; background: rgba(0, 230, 118, 0.12); display: flex; align-items: center; justify-content: center;">
              <i class="fas fa-arrow-up-right-from-square" style="color: var(--success); font-size: 14px;"></i>
            </div>
            <div>
              <div style="font-size: 14px; font-weight: 700; color: #fff;"><?php echo htmlspecialchars($call['calleeName'] ?: $call['calleeNumber']); ?></div>
              <div style="font-size: 11px; color: var(--text-muted); display: flex; gap: 8px;">
                <span><?php echo htmlspecialchars($call['calleeNumber']); ?></span>
                <span>•</span>
                <span><?php echo $durText; ?> মিনিট</span>
                <span>•</span>
                <span><?php echo date('d M, h:i A', $call['timestamp']); ?></span>
              </div>
            </div>
          </div>
          <a href="call-active.php?callee=<?php echo urlencode($call['calleeNumber']); ?>&type=AUDIO" class="btn-call" style="width: 38px; height: 38px; font-size: 14px; box-shadow: none;">
            <i class="fas fa-phone"></i>
          </a>
        </div>
      <?php endforeach; ?>
    </div>
  <?php endif; ?>
</div>

<?php include __DIR__ . '/includes/footer.php'; ?>
