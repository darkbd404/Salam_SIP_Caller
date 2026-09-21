<?php
require_once __DIR__ . '/includes/config.php';
$user = require_auth();
$isAdmin = is_admin_user($user);

$msg = '';
$error = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_FILES['profilePhoto'])) {
    if ($_FILES['profilePhoto']['error'] === UPLOAD_ERR_OK) {
        $ext = pathinfo($_FILES['profilePhoto']['name'], PATHINFO_EXTENSION);
        $filename = 'profile_' . time() . '_' . rand(100, 999) . '.' . $ext;
        $target = UPLOADS_DIR . '/' . $filename;
        if (move_uploaded_file($_FILES['profilePhoto']['tmp_name'], $target)) {
            $users = read_json_data(USER_JSON_FILE);
            foreach ($users as &$u) {
                if ($u['id'] == $user['id']) {
                    $u['profilePhoto'] = 'uploads/' . $filename;
                    break;
                }
            }
            write_json_data(USER_JSON_FILE, $users);
            $user = get_logged_in_user();
            $msg = 'প্রোফাইল ছবি সফলভাবে আপডেট করা হয়েছে!';
        }
    }
}

include __DIR__ . '/includes/header.php';
?>

<div class="card">
  <div style="text-align: center; margin-bottom: 20px;">
    
    <!-- Profile Photo with Direct Upload -->
    <div style="position: relative; width: 96px; height: 96px; margin: 0 auto 12px;">
      <div style="width: 100%; height: 100%; border-radius: 50%; background: linear-gradient(135deg, var(--primary), var(--primary-light)); display: flex; align-items: center; justify-content: center; font-size: 38px; color: #fff; overflow: hidden; border: 3px solid #00E676; box-shadow: 0 4px 15px rgba(0, 230, 118, 0.3);">
        <?php if (!empty($user['profilePhoto']) && file_exists(__DIR__ . '/' . $user['profilePhoto'])): ?>
          <img src="<?php echo htmlspecialchars($user['profilePhoto']); ?>" style="width: 100%; height: 100%; object-fit: cover;" alt="Profile Photo">
        <?php else: ?>
          <i class="fas fa-user"></i>
        <?php endif; ?>
      </div>

      <form action="profile.php" method="POST" enctype="multipart/form-data" id="photoForm">
        <label for="photoInput" style="position: absolute; bottom: 0; right: 0; width: 30px; height: 30px; border-radius: 50%; background: #00E676; color: #000; display: flex; align-items: center; justify-content: center; font-size: 13px; cursor: pointer; border: 2px solid #06110F;">
          <i class="fas fa-camera"></i>
        </label>
        <input type="file" name="profilePhoto" id="photoInput" accept="image/*" style="display: none;" onchange="document.getElementById('photoForm').submit()">
      </form>
    </div>

    <h2 style="font-size: 20px; font-weight: 700;"><?php echo htmlspecialchars($user['name']); ?></h2>
    
    <div style="display: flex; justify-content: center; gap: 8px; margin-top: 6px;">
      <span class="badge-ip" style="font-size: 13px; padding: 4px 12px;">
        <i class="fas fa-signal"></i> <?php echo htmlspecialchars($user['ipNumber']); ?>
      </span>
      <span style="background: rgba(0, 230, 118, 0.2); border: 1px solid #00E676; color: #00E676; font-size: 11px; padding: 4px 8px; border-radius: 20px; font-weight: 700;">
        ✓ BTRC KYC VERIFIED
      </span>
    </div>
  </div>

  <?php if ($msg): ?>
    <div class="alert alert-success"><i class="fas fa-circle-check"></i> <?php echo $msg; ?></div>
  <?php endif; ?>

  <!-- Balance Card -->
  <div style="background: rgba(0,0,0,0.3); border: 1px solid var(--card-border); border-radius: 12px; padding: 14px; margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center;">
    <div>
      <div style="font-size: 11px; color: var(--text-muted);">মোট ব্যালেন্স</div>
      <div style="font-size: 22px; font-weight: 700; color: #FFD54F;">৳ <?php echo number_format($user['balance'] ?? 0, 2); ?></div>
    </div>
    <a href="recharge.php" class="btn btn-primary" style="width: auto; padding: 8px 16px; font-size: 12px;">
      <i class="fas fa-plus"></i> রিচার্জ করুন
    </a>
  </div>

  <!-- Admin Panel Button (ONLY for Master Admin) -->
  <?php if ($isAdmin): ?>
    <div style="margin-bottom: 16px;">
      <a href="admin.php" class="btn" style="background: linear-gradient(135deg, #FFB300, #FF8F00); color: #000; font-weight: 800; padding: 12px; border-radius: 12px; display: flex; align-items: center; justify-content: center; gap: 8px; text-decoration: none;">
        <i class="fas fa-shield-halved" style="font-size: 18px;"></i> মাস্টার অ্যাডমিন কন্ট্রোল প্যানেল
      </a>
    </div>
  <?php endif; ?>

  <!-- Details List -->
  <div style="background: rgba(0,0,0,0.25); border: 1px solid var(--card-border); border-radius: 12px; padding: 12px; font-size: 13px; line-height: 1.8; margin-bottom: 20px;">
    <div style="display: flex; justify-content: space-between; border-bottom: 1px solid rgba(255,255,255,0.05); padding: 4px 0;">
      <span style="color: var(--text-muted);">মোবাইল নম্বর:</span>
      <strong><?php echo htmlspecialchars($user['mobileNumber']); ?></strong>
    </div>
    <div style="display: flex; justify-content: space-between; border-bottom: 1px solid rgba(255,255,255,0.05); padding: 4px 0;">
      <span style="color: var(--text-muted);">জিমেইল:</span>
      <strong><?php echo htmlspecialchars($user['email']); ?></strong>
    </div>
    <div style="display: flex; justify-content: space-between; border-bottom: 1px solid rgba(255,255,255,0.05); padding: 4px 0;">
      <span style="color: var(--text-muted);">জাতীয় পরিচয়পত্র (NID):</span>
      <strong><?php echo htmlspecialchars($user['nidNumber']); ?></strong>
    </div>
    <div style="display: flex; justify-content: space-between; padding: 4px 0;">
      <span style="color: var(--text-muted);">রেজিস্ট্রেশন তারিখ:</span>
      <strong><?php echo date('d M Y', $user['createdAt'] ?? time()); ?></strong>
    </div>
  </div>

  <a href="logout.php" class="btn" style="background: rgba(255, 82, 82, 0.15); border: 1px solid rgba(255, 82, 82, 0.4); color: #FF5252; text-decoration: none; text-align: center; display: block;">
    <i class="fas fa-right-from-bracket"></i> লগআউট
  </a>
</div>

<?php include __DIR__ . '/includes/footer.php'; ?>
