<?php
require_once __DIR__ . '/includes/config.php';

$pending = $_SESSION['pending_user'] ?? null;
if (!$pending) {
    header('Location: register.php');
    exit;
}

$error = '';
$success = '';

// Send Direct PHP Mail if mail server is enabled
if (!isset($_SESSION['otp_mail_sent'])) {
    $to = $pending['email'];
    $subject = "Salam SIP Caller - আপনার একাউন্ট ওটিপি: " . $pending['otpCode'];
    $message = "সালাম {$pending['name']},\n\nSalam SIP Caller-এ আপনার একাউন্ট সক্রিয় করতে ৬ ডিজিটের ওটিপি কোডটি ব্যবহার করুন:\n\n👉 OTP কোড: {$pending['otpCode']}\n\nআপনার নির্ধারিত সালাম আইপি: {$pending['ipNumber']}\n\nধন্যবাদ,\nSalam SIP Caller Team";
    $headers = "From: noreply@salamcaller.com\r\n" .
               "Reply-To: salam230864@gmail.com\r\n" .
               "X-Mailer: PHP/" . phpversion();
    @mail($to, $subject, $message, $headers);
    $_SESSION['otp_mail_sent'] = true;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $enteredOtp = clean_input($_POST['otp'] ?? '');

    // Strictly check against the generated OTP
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
        unset($_SESSION['otp_mail_sent']);
        $_SESSION['user_id'] = $newUser['id'];
        $_SESSION['user_name'] = $newUser['name'];
        $_SESSION['user_role'] = $newUser['role'];

        header('Location: index.php?registered=1');
        exit;
    } else {
        $error = 'ভুল ওটিপি কোড! অনুগ্রহ করে আপনার জিমেইল ইনবক্সে পাঠানো ৬ ডিজিটের সঠিক কোডটি দিন।';
    }
}

include __DIR__ . '/includes/header.php';
?>

<div class="card">
  <div style="text-align: center; margin-bottom: 20px;">
    <div style="width: 52px; height: 52px; background: rgba(0, 230, 118, 0.15); border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 10px; border: 1px solid #00E676;">
      <i class="fas fa-envelope-open-text" style="font-size: 22px; color: var(--success);"></i>
    </div>
    <h2 style="font-size: 20px; font-weight: 700;">জিমেইল ওটিপি ভেরিফিকেশন</h2>
    <p style="font-size: 13px; color: var(--text-muted); margin-top: 4px;">
      একটি ৬ ডিজিটের ভেরিফিকেশন কোড আপনার জিমেইল <strong style="color: var(--primary-light);"><?php echo htmlspecialchars($pending['email']); ?></strong> এ পাঠানো হয়েছে।
    </p>
  </div>

  <?php if ($error): ?>
    <div class="alert alert-danger"><i class="fas fa-circle-exclamation"></i> <?php echo $error; ?></div>
  <?php endif; ?>

  <!-- Direct Fast Verification Box -->
  <div style="background: rgba(0, 230, 118, 0.1); border: 1px dashed var(--success); border-radius: 12px; padding: 12px; margin-bottom: 16px; text-align: center;">
    <div style="font-size: 11px; color: var(--text-muted);">সুপার ফাস্ট ডিরেক্ট ওটিপি (Fast Direct Verification):</div>
    <div style="font-size: 22px; font-weight: 800; color: #00E676; letter-spacing: 4px; margin: 4px 0;">
      <?php echo htmlspecialchars($pending['otpCode']); ?>
    </div>
    <button type="button" onclick="autoFillOtp('<?php echo $pending['otpCode']; ?>')" style="background: var(--primary); color: #fff; border: none; padding: 4px 12px; border-radius: 6px; font-size: 11px; font-weight: 700; cursor: pointer;">
      <i class="fas fa-paste"></i> এক ক্লিকে কোড বসান (Auto Fill)
    </button>
  </div>

  <form action="verify-otp.php" method="POST">
    <div class="form-group" style="text-align: center;">
      <label class="form-label" style="text-align: center;">৬ ডিজিটের ওটিপি লিখুন</label>
      <input type="text" name="otp" id="otpInput" class="form-control" maxlength="6" style="text-align: center; font-size: 26px; font-weight: 800; letter-spacing: 8px;" placeholder="••••••" required autofocus>
    </div>

    <button type="submit" class="btn btn-primary" style="margin-top: 8px;">
      <i class="fas fa-circle-check"></i> ভেরিফাই করে একাউন্টে প্রবেশ করুন
    </button>
  </form>

  <div style="text-align: center; margin-top: 18px;">
    <p style="font-size: 11px; color: var(--text-muted);">
      ইনবক্সে মেইল না পেলে জিমেইলের <b>Spam (স্প্যাম)</b> ফোল্ডারও চেক করতে পারেন।
    </p>
  </div>
</div>

<script>
function autoFillOtp(code) {
  const input = document.getElementById('otpInput');
  if (input) {
    input.value = code;
    input.focus();
  }
}
</script>

<?php include __DIR__ . '/includes/footer.php'; ?>
