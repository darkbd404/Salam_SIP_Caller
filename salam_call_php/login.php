<?php
require_once __DIR__ . '/includes/config.php';

$error = '';
$success = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $identifier = clean_input($_POST['identifier'] ?? '');
    $password = clean_input($_POST['password'] ?? '');

    if (empty($identifier) || empty($password)) {
        $error = 'অনুগ্রহ করে আইপি নম্বর/মোবাইল/ইমেইল এবং পাসওয়ার্ড প্রদান করুন।';
    } else {
        $users = read_json_data(USER_JSON_FILE);
        $foundUser = null;

        foreach ($users as $u) {
            $mobileMatch = ($u['mobileNumber'] ?? '') === $identifier;
            $ipMatch = ($u['ipNumber'] ?? '') === $identifier;
            $emailMatch = strtolower($u['email'] ?? '') === strtolower($identifier);

            if ($mobileMatch || $ipMatch || $emailMatch) {
                if (($u['password'] ?? '') === $password) {
                    $foundUser = $u;
                    break;
                }
            }
        }

        if ($foundUser) {
            if (!empty($foundUser['isBlockedByAdmin'])) {
                $error = 'আপনার একাউন্টটি সাময়িকভাবে স্থগিত রয়েছে। অ্যাডমিনের সাথে যোগাযোগ করুন।';
            } else {
                $_SESSION['user_id'] = $foundUser['id'];
                $_SESSION['user_name'] = $foundUser['name'];
                $_SESSION['user_role'] = $foundUser['role'] ?? 'USER';
                header('Location: index.php');
                exit;
            }
        } else {
            $error = 'ভুল তথ্য! সঠিক আইপি নম্বর, মোবাইল অথবা পাসওয়ার্ড লিখুন।';
        }
    }
}

include __DIR__ . '/includes/header.php';
?>

<div class="card">
  <div style="text-align: center; margin-bottom: 20px;">
    <div style="width: 50px; height: 50px; background: rgba(0, 137, 123, 0.15); border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 10px;">
      <i class="fas fa-lock" style="font-size: 22px; color: var(--primary-light);"></i>
    </div>
    <h2 style="font-size: 20px; font-weight: 700;">সালাম কল লগইন</h2>
    <p style="font-size: 12px; color: var(--text-muted);">আপনার একাউন্টে প্রবেশ করুন</p>
  </div>

  <?php if ($error): ?>
    <div class="alert alert-danger"><i class="fas fa-circle-exclamation"></i> <?php echo $error; ?></div>
  <?php endif; ?>

  <form action="login.php" method="POST">
    <div class="form-group">
      <label class="form-label">আইপি নম্বর / মোবাইল / জিমেইল</label>
      <div style="position: relative;">
        <input type="text" name="identifier" class="form-control" placeholder="যেমন: 09612... বা 017..." required autofocus>
      </div>
    </div>

    <div class="form-group">
      <label class="form-label">পাসওয়ার্ড</label>
      <input type="password" name="password" class="form-control" placeholder="••••••••" required>
    </div>

    <button type="submit" class="btn btn-primary" style="margin-top: 10px;">
      <i class="fas fa-right-to-bracket"></i> লগইন করুন
    </button>
  </form>

  <div style="text-align: center; margin-top: 20px; font-size: 13px; color: var(--text-muted);">
    একাউন্ট নেই? <a href="register.php" style="color: var(--primary-light); font-weight: 700; text-decoration: none;">নতুন একাউন্ট খুলুন</a>
  </div>
</div>

<?php include __DIR__ . '/includes/footer.php'; ?>
