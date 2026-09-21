<?php
require_once __DIR__ . '/includes/config.php';
$user = require_auth();

$msg = '';
$error = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $amount = (float)($_POST['amount'] ?? 0);
    $method = clean_input($_POST['method'] ?? 'bKash');
    $senderNumber = clean_input($_POST['senderNumber'] ?? '');
    $trxId = strtoupper(clean_input($_POST['trxId'] ?? ''));

    if ($amount < 10) {
        $error = 'সর্বনিম্ন রিচার্জ পরিমাণ ৳ ১০.০০';
    } elseif (empty($senderNumber) || empty($trxId)) {
        $error = 'প্রেরক নম্বর ও TrxID সঠিকভাবে লিখুন।';
    } else {
        $recharges = read_json_data(RECHARGE_JSON_FILE);
        
        // Check duplicate TrxID
        $duplicate = false;
        foreach ($recharges as $r) {
            if ($r['trxId'] === $trxId) {
                $duplicate = true;
                break;
            }
        }

        if ($duplicate) {
            $error = 'এই ট্রানজেকশন আইডি (TrxID) ইতিপূর্বে ব্যবহার করা হয়েছে!';
        } else {
            $newRecharge = [
                'id' => 'rec_' . time() . '_' . rand(100, 999),
                'userId' => $user['id'],
                'userName' => $user['name'],
                'userIp' => $user['ipNumber'],
                'amount' => $amount,
                'method' => $method,
                'senderNumber' => $senderNumber,
                'trxId' => $trxId,
                'status' => 'PENDING',
                'timestamp' => time()
            ];

            $recharges[] = $newRecharge;
            write_json_data(RECHARGE_JSON_FILE, $recharges);
            $msg = 'রিচার্জের আবেদন সফলভাবে জমা দেওয়া হয়েছে! অ্যাডমিন যাচাই করার পর আপনার একাউন্টে ব্যালেন্স যুক্ত হবে।';
        }
    }
}

$allRecharges = read_json_data(RECHARGE_JSON_FILE);
$myRecharges = array_filter($allRecharges, fn($r) => ($r['userIp'] ?? '') === $user['ipNumber']);

include __DIR__ . '/includes/header.php';
?>

<div class="card">
  <div class="card-title"><i class="fas fa-wallet"></i> ব্যালেন্স ও রিচার্জ পোর্টাল</div>
  
  <div style="background: linear-gradient(135deg, rgba(0, 137, 123, 0.4), rgba(0, 230, 118, 0.1)); border: 1px solid #00E676; border-radius: 14px; padding: 16px; margin-bottom: 20px; text-align: center;">
    <div style="font-size: 12px; color: var(--text-muted);">বর্তমান ব্যালেন্স</div>
    <div style="font-size: 28px; font-weight: 800; color: #FFD54F; margin: 4px 0;">
      ৳ <?php echo number_format($user['balance'] ?? 0, 2); ?>
    </div>
    <div style="font-size: 11px; color: #A7FFEB;">
      কল রেট: ৳ ০.৩০ / মিনিট • মেসেজ: ৳ ০.১০ / এসএমএস
    </div>
  </div>

  <?php if ($msg): ?>
    <div class="alert alert-success"><i class="fas fa-circle-check"></i> <?php echo $msg; ?></div>
  <?php endif; ?>
  <?php if ($error): ?>
    <div class="alert alert-danger"><i class="fas fa-circle-exclamation"></i> <?php echo $error; ?></div>
  <?php endif; ?>

  <!-- Merchant / Payment Numbers -->
  <div style="background: rgba(0,0,0,0.3); border: 1px solid var(--card-border); border-radius: 12px; padding: 14px; margin-bottom: 20px;">
    <div style="font-size: 13px; font-weight: 700; color: #fff; margin-bottom: 8px;">
      <i class="fas fa-circle-info" style="color: var(--primary-light);"></i> টাকা পাঠানোর নম্বর (Send Money / Personal):
    </div>
    <div style="display: flex; flex-direction: column; gap: 8px; font-size: 13px;">
      <div style="display: flex; justify-content: space-between; align-items: center; background: rgba(255,255,255,0.05); padding: 8px 12px; border-radius: 8px;">
        <span><strong style="color: #E2136E;">bKash</strong>: 01620230864</span>
        <button type="button" onclick="navigator.clipboard.writeText('01620230864'); alert('বিকাশ নম্বর কপি হয়েছে!');" class="btn btn-outline" style="padding: 4px 8px; font-size: 10px; width: auto;">কপি</button>
      </div>
      <div style="display: flex; justify-content: space-between; align-items: center; background: rgba(255,255,255,0.05); padding: 8px 12px; border-radius: 8px;">
        <span><strong style="color: #F7941D;">Nagad</strong>: 01620230864</span>
        <button type="button" onclick="navigator.clipboard.writeText('01620230864'); alert('নগদ নম্বর কপি হয়েছে!');" class="btn btn-outline" style="padding: 4px 8px; font-size: 10px; width: auto;">কপি</button>
      </div>
      <div style="display: flex; justify-content: space-between; align-items: center; background: rgba(255,255,255,0.05); padding: 8px 12px; border-radius: 8px;">
        <span><strong style="color: #8C3494;">Rocket</strong>: 01620230864</span>
        <button type="button" onclick="navigator.clipboard.writeText('01620230864'); alert('রকেট নম্বর কপি হয়েছে!');" class="btn btn-outline" style="padding: 4px 8px; font-size: 10px; width: auto;">কপি</button>
      </div>
    </div>
  </div>

  <!-- Recharge Form -->
  <form action="recharge.php" method="POST">
    <div class="form-group">
      <label class="form-label">পেমেন্ট মেথড নির্বাচন করুন</label>
      <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px;">
        <label style="background: rgba(255,255,255,0.08); padding: 10px; border-radius: 8px; text-align: center; cursor: pointer; border: 1px solid var(--card-border);">
          <input type="radio" name="method" value="bKash" checked> <br><b>bKash</b>
        </label>
        <label style="background: rgba(255,255,255,0.08); padding: 10px; border-radius: 8px; text-align: center; cursor: pointer; border: 1px solid var(--card-border);">
          <input type="radio" name="method" value="Nagad"> <br><b>Nagad</b>
        </label>
        <label style="background: rgba(255,255,255,0.08); padding: 10px; border-radius: 8px; text-align: center; cursor: pointer; border: 1px solid var(--card-border);">
          <input type="radio" name="method" value="Rocket"> <br><b>Rocket</b>
        </label>
      </div>
    </div>

    <div class="form-group">
      <label class="form-label">রিচার্জের পরিমাণ (টাকা)</label>
      <input type="number" name="amount" class="form-control" placeholder="যেমন: 50, 100, 200" required min="10">
    </div>

    <div class="form-group">
      <label class="form-label">যে নম্বর থেকে টাকা পাঠিয়েছেন (Sender Number)</label>
      <input type="tel" name="senderNumber" class="form-control" placeholder="01XXXXXXXXX" required>
    </div>

    <div class="form-group">
      <label class="form-label">ট্রানজেকশন আইডি (TrxID)</label>
      <input type="text" name="trxId" class="form-control" placeholder="যেমন: 9J3K8L2P" required style="text-transform: uppercase;">
    </div>

    <button type="submit" class="btn btn-primary" style="margin-top: 10px;">
      <i class="fas fa-paper-plane"></i> রিচার্জ রিকুয়েস্ট জমা দিন
    </button>
  </form>

  <!-- My Recent Recharges -->
  <div style="margin-top: 30px;">
    <div style="font-size: 14px; font-weight: 700; margin-bottom: 10px; color: #fff;">
      <i class="fas fa-clock-rotate-left"></i> আমার পূর্ববর্তী রিচার্জ রিকুয়েস্ট
    </div>

    <?php if (empty($myRecharges)): ?>
      <div style="text-align: center; padding: 15px; font-size: 12px; color: var(--text-muted);">
        কোনো রিচার্জ রিকুয়েস্টের রেকর্ড পাওয়া যায়নি।
      </div>
    <?php else: ?>
      <div style="display: flex; flex-direction: column; gap: 8px;">
        <?php foreach (array_reverse($myRecharges) as $mr): ?>
          <div style="background: rgba(0,0,0,0.3); border: 1px solid var(--card-border); padding: 10px 14px; border-radius: 10px; display: flex; justify-content: space-between; align-items: center;">
            <div>
              <div style="font-size: 14px; font-weight: 700; color: #FFD54F;">৳ <?php echo number_format($mr['amount'], 2); ?> (<?php echo htmlspecialchars($mr['method']); ?>)</div>
              <div style="font-size: 11px; color: var(--text-muted);">TrxID: <?php echo htmlspecialchars($mr['trxId']); ?></div>
              <div style="font-size: 10px; color: var(--text-muted);"><?php echo date('d M, h:i A', $mr['timestamp']); ?></div>
            </div>
            <span style="font-size: 10px; padding: 3px 8px; border-radius: 4px; font-weight: bold; background: <?php echo $mr['status'] === 'APPROVED' ? '#00E676' : ($mr['status'] === 'REJECTED' ? '#FF5252' : '#FFB300'); ?>; color: #000;">
              <?php echo $mr['status']; ?>
            </span>
          </div>
        <?php endforeach; ?>
      </div>
    <?php endif; ?>
  </div>

</div>

<?php include __DIR__ . '/includes/footer.php'; ?>
