<?php
require_once __DIR__ . '/includes/config.php';
$currentUser = get_logged_in_user();
$isAdmin = is_admin_user($currentUser);

$recentCalls = read_json_data(CALLS_JSON_FILE);
$myCalls = [];
if ($currentUser) {
    $myIp = $currentUser['ipNumber'];
    foreach ($recentCalls as $c) {
        if (($c['callerId'] ?? '') === $myIp || ($c['calleeNumber'] ?? '') === $myIp) {
            $myCalls[] = $c;
        }
    }
    $myCalls = array_slice(array_reverse($myCalls), 0, 5);
}

include __DIR__ . '/includes/header.php';
?>

<?php if (!$currentUser): ?>
  <!-- Guest Welcome Hero -->
  <div class="card" style="text-align: center; padding: 30px 18px;">
    <div style="width: 70px; height: 70px; background: rgba(0, 230, 118, 0.15); border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px;">
      <i class="fas fa-phone-volume" style="font-size: 32px; color: var(--success);"></i>
    </div>
    
    <h2 style="font-size: 22px; font-weight: 700; margin-bottom: 6px;">Salam SIP Caller</h2>
    <div style="display: inline-block; background: rgba(0, 230, 118, 0.2); border: 1px solid #00E676; color: #00E676; padding: 4px 12px; border-radius: 20px; font-size: 11px; font-weight: 700; margin-bottom: 12px;">
      ✓ 09612 BTRC Officials Verified Telephony
    </div>
    
    <p style="font-size: 13px; color: var(--text-muted); line-height: 1.6; margin-bottom: 20px;">
      ০9612 সিরিজের নিজস্ব বিটিআরসি অনুমোদিত ফ্রি আইপি নম্বর দিয়ে দেশ-বিদেশে আনলিমিটেড এইচডি অডিও ও ভিডিও কল করুন।
    </p>

    <!-- Welcome Bonus Banner -->
    <div style="background: linear-gradient(135deg, rgba(255, 179, 0, 0.2), rgba(0, 230, 118, 0.15)); border: 1px dashed #FFB300; border-radius: 12px; padding: 12px; margin-bottom: 20px;">
      <div style="color: #FFD54F; font-weight: 800; font-size: 14px;">🎁 নতুন রেজিস্ট্রেশনে ৳ ৫০.০০ ওয়েলকাম বোনাস!</div>
      <div style="font-size: 11px; color: var(--text-muted); margin-top: 2px;">কল রেট: ৩০ পয়সা/মিনিট • মেসেজ: ১০ পয়সা/এসএমএস</div>
    </div>

    <div style="display: flex; flex-direction: column; gap: 10px;">
      <a href="register.php" class="btn btn-primary" style="padding: 12px; font-size: 14px; text-decoration: none;">
        <i class="fas fa-user-plus"></i> নতুন একাউন্ট খুলুন (রেজিস্ট্রেশন)
      </a>
      <a href="login.php" class="btn btn-outline" style="padding: 12px; font-size: 14px; text-decoration: none;">
        <i class="fas fa-right-to-bracket"></i> লগইন করুন
      </a>
    </div>
  </div>

<?php else: ?>
  
  <!-- Logged in User Dashboard -->
  <div class="card" style="padding: 18px 14px;">
    
    <!-- User Quick Info -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
      <div style="display: flex; align-items: center; gap: 12px;">
        <div style="width: 48px; height: 48px; border-radius: 50%; background: linear-gradient(135deg, var(--primary), var(--primary-light)); display: flex; align-items: center; justify-content: center; font-size: 20px; color: #fff; overflow: hidden; border: 2px solid #00E676;">
          <?php if (!empty($currentUser['profilePhoto']) && file_exists(__DIR__ . '/' . $currentUser['profilePhoto'])): ?>
            <img src="<?php echo htmlspecialchars($currentUser['profilePhoto']); ?>" style="width: 100%; height: 100%; object-fit: cover;" alt="Profile">
          <?php else: ?>
            <i class="fas fa-user"></i>
          <?php endif; ?>
        </div>
        <div>
          <div style="font-size: 16px; font-weight: 700; color: #fff;"><?php echo htmlspecialchars($currentUser['name']); ?></div>
          <div style="font-size: 12px; color: var(--primary-light); font-weight: 600;">আইপি: <?php echo htmlspecialchars($currentUser['ipNumber']); ?></div>
        </div>
      </div>

      <span style="font-size: 10px; background: rgba(0,230,118,0.2); border: 1px solid #00E676; color: #00E676; padding: 4px 8px; border-radius: 12px; font-weight: 700;">
        ✓ BTRC KYC
      </span>
    </div>

    <!-- Balance & Recharge Card -->
    <div style="background: linear-gradient(135deg, #004D40 0%, #00251F 100%); border: 1px solid rgba(0, 230, 118, 0.4); border-radius: 14px; padding: 16px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
      <div>
        <div style="font-size: 11px; color: #A7FFEB;">বর্তমান একাউন্ট ব্যালেন্স</div>
        <div style="font-size: 26px; font-weight: 800; color: #FFD54F;">৳ <?php echo number_format($currentUser['balance'] ?? 0, 2); ?></div>
        <div style="font-size: 10px; color: var(--text-muted); margin-top: 2px;">কল: ৳ ০.৩০/মিনিট • এসএমএস: ৳ ০.১০</div>
      </div>
      <a href="recharge.php" class="btn btn-primary" style="width: auto; padding: 8px 16px; font-size: 12px; text-decoration: none; box-shadow: 0 4px 15px rgba(0, 230, 118, 0.4);">
        <i class="fas fa-wallet"></i> রিচার্জ
      </a>
    </div>

    <!-- Master Admin Access Card (ONLY for Admin) -->
    <?php if ($isAdmin): ?>
      <a href="admin.php" style="text-decoration: none; margin-bottom: 20px; display: block;">
        <div style="background: linear-gradient(135deg, rgba(255, 179, 0, 0.25), rgba(255, 143, 0, 0.1)); border: 1px solid #FFB300; border-radius: 12px; padding: 12px 16px; display: flex; justify-content: space-between; align-items: center;">
          <div style="display: flex; align-items: center; gap: 10px;">
            <i class="fas fa-shield-halved" style="font-size: 22px; color: #FFB300;"></i>
            <div>
              <div style="font-size: 13px; font-weight: 700; color: #FFD54F;">মাস্টার অ্যাডমিন কন্ট্রোল প্যানেল</div>
              <div style="font-size: 11px; color: var(--text-muted);">ইউজার তথ্য এডিট, রিচার্জ অনুমোদন ও একাউন্ট নিয়ন্ত্রণ</div>
            </div>
          </div>
          <i class="fas fa-chevron-right" style="color: #FFB300;"></i>
        </div>
      </a>
    <?php endif; ?>

    <!-- Quick Feature Grid -->
    <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-bottom: 24px;">
      
      <a href="dialer.php" style="text-decoration: none; color: inherit;">
        <div style="background: rgba(255,255,255,0.06); border: 1px solid var(--card-border); border-radius: 12px; padding: 14px 8px; text-align: center;">
          <i class="fas fa-calculator" style="font-size: 22px; color: var(--primary-light); margin-bottom: 6px;"></i>
          <div style="font-size: 12px; font-weight: 700;">ডায়াল প্যাড</div>
        </div>
      </a>

      <a href="contacts.php" style="text-decoration: none; color: inherit;">
        <div style="background: rgba(255,255,255,0.06); border: 1px solid var(--card-border); border-radius: 12px; padding: 14px 8px; text-align: center;">
          <i class="fas fa-address-book" style="font-size: 22px; color: #00E676; margin-bottom: 6px;"></i>
          <div style="font-size: 12px; font-weight: 700;">আইপি কন্টাক্ট</div>
        </div>
      </a>

      <a href="messages.php" style="text-decoration: none; color: inherit;">
        <div style="background: rgba(255,255,255,0.06); border: 1px solid var(--card-border); border-radius: 12px; padding: 14px 8px; text-align: center;">
          <i class="fas fa-comment-dots" style="font-size: 22px; color: #FFB300; margin-bottom: 6px;"></i>
          <div style="font-size: 12px; font-weight: 700;">মেসেজ (১০প)</div>
        </div>
      </a>

    </div>

    <!-- Recent Call Logs -->
    <div>
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
        <div style="font-size: 14px; font-weight: 700; color: #fff;"><i class="fas fa-clock-rotate-left"></i> সাম্প্রতিক কল</div>
        <a href="history.php" style="font-size: 11px; color: var(--primary-light); text-decoration: none;">সব দেখুন</a>
      </div>

      <?php if (empty($myCalls)): ?>
        <div style="text-align: center; padding: 20px; color: var(--text-muted); font-size: 12px;">
          এখনও কোনো কল হিস্ট্রি নেই। ডায়াল প্যাড থেকে কল করুন।
        </div>
      <?php else: ?>
        <div style="display: flex; flex-direction: column; gap: 8px;">
          <?php foreach ($myCalls as $mc): ?>
            <div style="background: rgba(0,0,0,0.3); border: 1px solid var(--card-border); border-radius: 10px; padding: 10px 12px; display: flex; justify-content: space-between; align-items: center;">
              <div>
                <div style="font-size: 13px; font-weight: 700; color: #fff;"><?php echo htmlspecialchars($mc['calleeName'] ?? $mc['calleeNumber']); ?></div>
                <div style="font-size: 11px; color: var(--text-muted);"><?php echo htmlspecialchars($mc['calleeNumber']); ?> • <?php echo ($mc['duration'] ?? 0); ?> সে. (৳ <?php echo number_format($mc['cost'] ?? 0, 2); ?>)</div>
              </div>
              <a href="call-active.php?callee=<?php echo urlencode($mc['calleeNumber']); ?>&type=AUDIO" class="btn-action btn-audio" style="width: 32px; height: 32px; font-size: 12px;">
                <i class="fas fa-phone"></i>
              </a>
            </div>
          <?php endforeach; ?>
        </div>
      <?php endif; ?>
    </div>

  </div>

<?php endif; ?>

<?php include __DIR__ . '/includes/footer.php'; ?>
