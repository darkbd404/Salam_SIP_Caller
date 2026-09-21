<?php
require_once __DIR__ . '/includes/config.php';
$user = require_auth();

$allCalls = read_json_data(CALLS_JSON_FILE);
$userCalls = [];
$myIp = $user['ipNumber'];

foreach ($allCalls as $c) {
    $caller = $c['callerId'] ?? ($c['callerNumber'] ?? '');
    $callee = $c['calleeNumber'] ?? '';
    if ($caller === $myIp || $callee === $myIp || ($c['userId'] ?? 0) === $user['id']) {
        $userCalls[] = $c;
    }
}
$userCalls = array_reverse($userCalls); // newest first

include __DIR__ . '/includes/header.php';
?>

<div class="card">
  <div class="card-title"><i class="fas fa-clock-rotate-left"></i> বিস্তারিত কল হিস্ট্রি</div>

  <!-- Filter Badges -->
  <div style="display: flex; gap: 6px; overflow-x: auto; margin-bottom: 14px; padding-bottom: 4px;">
    <button onclick="filterCalls('ALL')" class="btn" id="filter-ALL" style="padding: 6px 12px; font-size: 11px; width: auto; background: var(--primary); color: #fff; border-radius: 20px;">সকল (<?php echo count($userCalls); ?>)</button>
    <button onclick="filterCalls('INCOMING')" class="btn" id="filter-INCOMING" style="padding: 6px 12px; font-size: 11px; width: auto; background: rgba(0,230,118,0.15); color: #00E676; border-radius: 20px;">গৃহীত</button>
    <button onclick="filterCalls('OUTGOING')" class="btn" id="filter-OUTGOING" style="padding: 6px 12px; font-size: 11px; width: auto; background: rgba(0,176,255,0.15); color: #00B0FF; border-radius: 20px;">বহির্গামী</button>
    <button onclick="filterCalls('MISSED')" class="btn" id="filter-MISSED" style="padding: 6px 12px; font-size: 11px; width: auto; background: rgba(255,82,82,0.15); color: #FF5252; border-radius: 20px;">মিসড কল</button>
    <button onclick="filterCalls('REJECTED')" class="btn" id="filter-REJECTED" style="padding: 6px 12px; font-size: 11px; width: auto; background: rgba(255,179,0,0.15); color: #FFB300; border-radius: 20px;">বাতিলকৃত</button>
  </div>

  <?php if (empty($userCalls)): ?>
    <div style="text-align: center; padding: 40px 10px; color: var(--text-muted);">
      <i class="fas fa-phone-slash" style="font-size: 36px; margin-bottom: 12px; opacity: 0.4;"></i>
      <p style="font-size: 13px;">এখনও কোনো কল রেকর্ড সংরক্ষিত নেই।</p>
    </div>
  <?php else: ?>
    <div style="display: flex; flex-direction: column; gap: 8px;">
      <?php foreach ($userCalls as $call): 
        $durSec = $call['durationSeconds'] ?? ($call['duration'] ?? 0);
        $durMin = floor($durSec / 60);
        $durSecRem = $durSec % 60;
        $durText = sprintf("%02d:%02d", $durMin, $durSecRem);

        $callerNum = $call['callerId'] ?? ($call['callerNumber'] ?? '');
        $calleeNum = $call['calleeNumber'] ?? '';
        $isMeCaller = ($callerNum === $myIp);

        // Determine Call Type Status
        $rawStatus = strtoupper($call['status'] ?? ($call['type'] ?? 'OUTGOING'));
        
        if ($rawStatus === 'REJECTED') {
          $callCategory = 'REJECTED';
          $iconClass = 'fas fa-phone-slash';
          $badgeText = 'প্রত্যাখ্যাত';
          $badgeClass = 'history-rejected';
          $partnerNum = $isMeCaller ? $calleeNum : $callerNum;
        } elseif ($rawStatus === 'MISSED' || ($durSec == 0 && !$isMeCaller)) {
          $callCategory = 'MISSED';
          $iconClass = 'fas fa-arrow-down-left';
          $badgeText = 'মিসড কল';
          $badgeClass = 'history-missed';
          $partnerNum = $callerNum;
        } elseif ($isMeCaller) {
          $callCategory = 'OUTGOING';
          $iconClass = 'fas fa-arrow-up-right-from-square';
          $badgeText = 'বহির্গামী';
          $badgeClass = 'history-outgoing';
          $partnerNum = $calleeNum;
        } else {
          $callCategory = 'INCOMING';
          $iconClass = 'fas fa-arrow-down-left';
          $badgeText = 'গৃহীত কল';
          $badgeClass = 'history-incoming';
          $partnerNum = $callerNum;
        }

        $timeVal = is_numeric($call['timestamp']) ? (strlen((string)$call['timestamp']) > 10 ? (int)($call['timestamp']/1000) : (int)$call['timestamp']) : time();
      ?>
        <div class="call-history-row" data-category="<?php echo $callCategory; ?>" style="display: flex; justify-content: space-between; align-items: center; background: var(--input-bg); padding: 12px 14px; border-radius: 14px; border: 1px solid var(--card-border);">
          <div style="display: flex; align-items: center; gap: 12px; min-width: 0; flex: 1;">
            <div style="width: 42px; height: 42px; min-width: 42px; border-radius: 50%; background: rgba(0,0,0,0.2); border: 1px solid var(--card-border); display: flex; align-items: center; justify-content: center;">
              <i class="<?php echo $iconClass; ?>" style="font-size: 16px;"></i>
            </div>
            
            <div style="min-width: 0;">
              <div style="display: flex; align-items: center; gap: 6px; margin-bottom: 2px;">
                <span style="font-size: 14px; font-weight: 700; color: var(--text-main); white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                  <?php echo htmlspecialchars($call['calleeName'] ?: $partnerNum); ?>
                </span>
                <span class="history-badge <?php echo $badgeClass; ?>"><?php echo $badgeText; ?></span>
              </div>
              
              <div style="font-size: 11px; color: var(--text-muted); display: flex; flex-wrap: wrap; gap: 6px; align-items: center;">
                <span style="font-family: monospace; color: var(--primary-light);"><?php echo htmlspecialchars($partnerNum); ?></span>
                <span>•</span>
                <span><?php echo $durText; ?> মিনিট</span>
                <?php if (!empty($call['cost']) && $call['cost'] > 0): ?>
                  <span>•</span>
                  <span style="color: #FFD54F; font-weight: 600;">৳ <?php echo number_format($call['cost'], 2); ?></span>
                <?php endif; ?>
                <span>•</span>
                <span><?php echo date('d M, h:i A', $timeVal); ?></span>
              </div>
            </div>
          </div>

          <a href="call-active.php?callee=<?php echo urlencode($partnerNum); ?>&type=<?php echo htmlspecialchars($call['callType'] ?? 'AUDIO'); ?>" class="btn-call" style="width: 38px; height: 38px; font-size: 14px; box-shadow: none; flex-shrink: 0;" title="পুনরায় কল করুন">
            <i class="fas fa-phone"></i>
          </a>
        </div>
      <?php endforeach; ?>
    </div>
  <?php endif; ?>
</div>

<script>
function filterCalls(category) {
  const rows = document.querySelectorAll('.call-history-row');
  const buttons = document.querySelectorAll('[id^="filter-"]');
  
  buttons.forEach(btn => {
    btn.style.background = 'rgba(125,125,125,0.15)';
    btn.style.color = 'var(--text-muted)';
  });
  
  const activeBtn = document.getElementById('filter-' + category);
  if (activeBtn) {
    activeBtn.style.background = 'var(--primary)';
    activeBtn.style.color = '#fff';
  }

  rows.forEach(row => {
    if (category === 'ALL' || row.getAttribute('data-category') === category) {
      row.style.display = 'flex';
    } else {
      row.style.display = 'none';
    }
  });
}
</script>

<?php include __DIR__ . '/includes/footer.php'; ?>
