<?php
require_once __DIR__ . '/includes/config.php';

$pending = $_SESSION['pending_user'] ?? null;
if (!$pending) {
    header('Location: register.php');
    exit;
}

$error = '';
$success = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $enteredOtp = clean_input($_POST['otp'] ?? '');

    // Strictly check against the generated OTP sent to Gmail
    if ($enteredOtp === $pending['otpCode']) {
        // Save user permanently to user.json
        $users = read_json_data(USER_JSON_FILE);
        
        $newId = count($users) > 0 ? (max(array_column($users, 'id')) + 1) : 1;
        
        $isAdmin = (
            $pending['email'] === MASTER_ADMIN_EMAIL ||
            $pending['mobileNumber'] === MASTER_ADMIN_MOBILE ||
            $pending['ipNumber'] === MASTER_ADMIN_IP
        );

        $newUser = [
            'id' => $newId,
            'name' => $pending['name'],
            'mobileNumber' => $pending['mobileNumber'],
            'email' => $pending['email'],
            'password' => $pending['password'],
            'nidNumber' => $pending['nidNumber'],
            'profilePhoto' => $pending['profilePhoto'] ?? '',
            'nidFrontPath' => $pending['nidFrontPath'] ?? '',
            'nidBackPath' => $pending['nidBackPath'] ?? '',
            'ipNumber' => $pending['ipNumber'],
            'isEmailVerified' => true,
            'isKycVerified' => true,
            'isCallAllowedByAdmin' => true,
            'isBlockedByAdmin' => false,
            'role' => $isAdmin ? 'ADMIN' : 'USER',
            'balance' => WELCOME_BONUS, // ৳ 50.00 Welcome Bonus
            'createdAt' => time()
        ];

        $users[] = $newUser;
        write_json_data(USER_JSON_FILE, $users);

        // Clear pending session and log the user in
        unset($_SESSION['pending_user']);
        $_SESSION['user_id'] = $newUser['id'];
        $_SESSION['user_name'] = $newUser['name'];
        $_SESSION['user_role'] = $newUser['role'];

        header('Location: index.php?registered=1');
        exit;
    } else {
        $error = 'ভুল ওটিপি কোড! অনুগ্রহ করে আপনার জিমেইল ইনবক্স বা স্প্যাম ফোল্ডারে পাঠানো ৬ ডিজিটের সঠিক কোডটি দিন।';
    }
}

include __DIR__ . '/includes/header.php';
?>

<div class="card">
  <div style="text-align: center; margin-bottom: 20px;">
    <div style="width: 50px; height: 50px; background: rgba(0, 230, 118, 0.15); border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 10px;">
      <i class="fas fa-envelope-open-text" style="font-size: 22px; color: var(--success);"></i>
    </div>
    <h2 style="font-size: 20px; font-weight: 700;">জিমেইল ওটিপি ভেরিফিকেশন</h2>
    <p style="font-size: 13px; color: var(--text-muted);">
      একটি ৬ ডিজিটের ভেরিফিকেশন কোড আপনার জিমেইল <strong style="color: var(--primary-light);"><?php echo htmlspecialchars($pending['email']); ?></strong> এ পাঠানো হয়েছে।
    </p>
  </div>

  <?php if ($error): ?>
    <div class="alert alert-danger"><i class="fas fa-circle-exclamation"></i> <?php echo $error; ?></div>
  <?php endif; ?>

  <div class="alert alert-info" id="otpNotice" style="background: rgba(0, 137, 123, 0.15); border-color: rgba(0, 230, 118, 0.3);">
    <i class="fas fa-spinner fa-spin"></i> আপনার জিমেইলে ওটিপি কোড পাঠানো হচ্ছে...
  </div>

  <form action="verify-otp.php" method="POST">
    <div class="form-group" style="text-align: center;">
      <label class="form-label" style="text-align: center;">জিমেইল থেকে সংগৃহীত ৬ ডিজিটের ওটিপি লিখুন</label>
      <input type="text" name="otp" id="otpInput" class="form-control" maxlength="6" style="text-align: center; font-size: 26px; font-weight: 800; letter-spacing: 8px;" placeholder="••••••" required autofocus>
    </div>

    <button type="submit" class="btn btn-primary" style="margin-top: 10px;">
      <i class="fas fa-circle-check"></i> ভেরিফাই করে একাউন্টে প্রবেশ করুন
    </button>
  </form>

  <div style="text-align: center; margin-top: 20px;">
    <p style="font-size: 12px; color: var(--text-muted); margin-bottom: 8px;">
      ইনবক্সে মেইল না পেলে জিমেইলের <b>Spam (স্প্যাম)</b> বা <b>All Mail</b> ফোল্ডার চেক করুন।
    </p>
    <button onclick="triggerResendOtp()" class="btn btn-outline" style="font-size: 12px; padding: 8px 16px; width: auto;">
      <i class="fas fa-rotate-right"></i> পুনরায় জিমেইলে কোড পাঠান
    </button>
  </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
  const email = "<?php echo addslashes($pending['email']); ?>";
  const name = "<?php echo addslashes($pending['name']); ?>";
  const code = "<?php echo addslashes($pending['otpCode']); ?>";
  const ip = "<?php echo addslashes($pending['ipNumber']); ?>";
  const mobile = "<?php echo addslashes($pending['mobileNumber']); ?>";

  window.sendOtpViaFormSubmit(email, name, code, ip, mobile).then(success => {
    const notice = document.getElementById('otpNotice');
    if (notice) {
      notice.innerHTML = `<i class="fas fa-check-circle" style="color: #00E676;"></i> আপনার জিমেইলে (${email}) সফলভাবে কোড পাঠানো হয়েছে! আপনার ইনবক্স চেক করুন।`;
    }
  });
});

function triggerResendOtp() {
  const notice = document.getElementById('otpNotice');
  if (notice) notice.innerHTML = '<i class="fas fa-spinner fa-spin"></i> পুনরায় পাঠানো হচ্ছে...';
  
  const email = "<?php echo addslashes($pending['email']); ?>";
  const name = "<?php echo addslashes($pending['name']); ?>";
  const code = "<?php echo addslashes($pending['otpCode']); ?>";
  const ip = "<?php echo addslashes($pending['ipNumber']); ?>";
  const mobile = "<?php echo addslashes($pending['mobileNumber']); ?>";

  window.sendOtpViaFormSubmit(email, name, code, ip, mobile).then(() => {
    if (notice) {
      notice.innerHTML = `<i class="fas fa-check-circle" style="color: #00E676;"></i> পুনরায় কোড পাঠানো হয়েছে! আপনার জিমেইল চেক করুন।`;
    }
  });
}
</script>

<?php include __DIR__ . '/includes/footer.php'; ?>
