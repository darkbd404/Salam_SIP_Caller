<?php
require_once __DIR__ . '/includes/config.php';

$error = '';
$success = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $name = clean_input($_POST['name'] ?? '');
    $mobile = clean_input($_POST['mobile'] ?? '');
    $email = clean_input($_POST['email'] ?? '');
    $password = clean_input($_POST['password'] ?? '');
    $nidNumber = clean_input($_POST['nidNumber'] ?? '');
    $customDigits = clean_input($_POST['customDigits'] ?? '');

    // Validation
    if (empty($name) || empty($mobile) || empty($email) || empty($password) || empty($nidNumber)) {
        $error = 'অনুগ্রহ করে সকল তথ্য সঠিকভাবে পূরণ করুন।';
    } elseif (strlen($mobile) < 11) {
        $error = 'সঠিক ১১ ডিজিটের মোবাইল নম্বর লিখুন।';
    } elseif (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $error = 'সঠিক জিমেইল / ইমেইল অ্যাড্রেস লিখুন।';
    } else {
        // Build IP Number
        $filteredDigits = preg_replace('/[^0-9]/', '', $customDigits);
        if (strlen($filteredDigits) < 6) {
            $filteredDigits = str_pad($filteredDigits, 6, '8', STR_PAD_RIGHT);
        } else {
            $filteredDigits = substr($filteredDigits, 0, 6);
        }
        $fullIpNumber = IP_PREFIX . $filteredDigits;

        // Check duplicate mobile, IP, or NID number
        $users = read_json_data(USER_JSON_FILE);
        $isDuplicate = false;
        foreach ($users as $u) {
            if (($u['nidNumber'] ?? '') === $nidNumber) {
                $error = 'এই জাতীয় পরিচয়পত্র (NID) নম্বর দিয়ে ইতিপূর্বে একটি একাউন্ট তৈরি করা হয়েছে। একই NID দিয়ে একাধিক একাউন্ট তৈরি করা যাবে না।';
                $isDuplicate = true;
                break;
            }
            if (($u['mobileNumber'] ?? '') === $mobile) {
                $error = 'এই মোবাইল নম্বর দিয়ে ইতিমধ্যে একাউন্ট রয়েছে।';
                $isDuplicate = true;
                break;
            }
            if (($u['ipNumber'] ?? '') === $fullIpNumber) {
                $error = 'এই আইপি নম্বরটি ইতিমধ্যে নিবন্ধিত। অনুগ্রহ করে অন্য ৬ ডিজিট নির্বাচন করুন।';
                $isDuplicate = true;
                break;
            }
        }

        if (!$isDuplicate) {
            // Handle Profile Photo upload
            $profilePhotoPath = '';
            if (isset($_FILES['profilePhoto']) && $_FILES['profilePhoto']['error'] === UPLOAD_ERR_OK) {
                $ext = pathinfo($_FILES['profilePhoto']['name'], PATHINFO_EXTENSION);
                $filename = 'profile_' . time() . '_' . rand(100, 999) . '.' . $ext;
                $target = UPLOADS_DIR . '/' . $filename;
                if (move_uploaded_file($_FILES['profilePhoto']['tmp_name'], $target)) {
                    $profilePhotoPath = 'uploads/' . $filename;
                }
            }

            // Handle NID Image uploads
            $nidFrontPath = '';
            $nidBackPath = '';

            if (isset($_FILES['nidFront']) && $_FILES['nidFront']['error'] === UPLOAD_ERR_OK) {
                $ext = pathinfo($_FILES['nidFront']['name'], PATHINFO_EXTENSION);
                $filename = 'nid_front_' . time() . '_' . rand(100, 999) . '.' . $ext;
                $target = UPLOADS_DIR . '/' . $filename;
                if (move_uploaded_file($_FILES['nidFront']['tmp_name'], $target)) {
                    $nidFrontPath = 'uploads/' . $filename;
                }
            }

            if (isset($_FILES['nidBack']) && $_FILES['nidBack']['error'] === UPLOAD_ERR_OK) {
                $ext = pathinfo($_FILES['nidBack']['name'], PATHINFO_EXTENSION);
                $filename = 'nid_back_' . time() . '_' . rand(100, 999) . '.' . $ext;
                $target = UPLOADS_DIR . '/' . $filename;
                if (move_uploaded_file($_FILES['nidBack']['tmp_name'], $target)) {
                    $nidBackPath = 'uploads/' . $filename;
                }
            }

            // Generate 6-digit OTP
            $otpCode = (string)rand(100000, 999999);

            // Store pending registration in Session
            $_SESSION['pending_user'] = [
                'name' => $name,
                'mobileNumber' => $mobile,
                'email' => $email,
                'password' => $password,
                'nidNumber' => $nidNumber,
                'profilePhoto' => $profilePhotoPath,
                'nidFrontPath' => $nidFrontPath,
                'nidBackPath' => $nidBackPath,
                'ipNumber' => $fullIpNumber,
                'otpCode' => $otpCode,
                'createdAt' => time()
            ];

            header('Location: verify-otp.php');
            exit;
        }
    }
}

include __DIR__ . '/includes/header.php';
?>

<div class="card">
  <div style="text-align: center; margin-bottom: 20px;">
    <div style="width: 50px; height: 50px; background: rgba(0, 230, 118, 0.15); border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 10px;">
      <i class="fas fa-user-plus" style="font-size: 22px; color: var(--success);"></i>
    </div>
    <h2 style="font-size: 20px; font-weight: 700;">Salam SIP Caller রেজিস্ট্রেশন</h2>
    <div style="font-size: 12px; color: #00E676; font-weight: 600;">🎉 রেজিষ্ট্রেশন করলেই পাচ্ছেন ৳ ৫০.০০ ওয়েলকাম বোনাস!</div>
    <p style="font-size: 11px; color: var(--text-muted); margin-top: 2px;">০9612 BTRC Officials Verified IP Telephony</p>
  </div>

  <?php if ($error): ?>
    <div class="alert alert-danger"><i class="fas fa-circle-exclamation"></i> <?php echo $error; ?></div>
  <?php endif; ?>

  <form action="register.php" method="POST" enctype="multipart/form-data" id="regForm">
    
    <!-- Profile Photo Upload -->
    <div class="form-group" style="text-align: center; margin-bottom: 18px;">
      <label class="form-label" style="text-align: center;">প্রোফাইল ছবি (Profile Photo)</label>
      <div style="width: 80px; height: 80px; border-radius: 50%; background: rgba(255,255,255,0.08); border: 2px dashed #00E676; margin: 0 auto; display: flex; align-items: center; justify-content: center; cursor: pointer; position: relative; overflow: hidden;" onclick="document.getElementById('profilePhotoInput').click()">
        <img id="profilePreview" style="width: 100%; height: 100%; object-fit: cover; display: none;" alt="Profile">
        <i class="fas fa-camera" id="profileIcon" style="font-size: 24px; color: #4DB6AC;"></i>
      </div>
      <input type="file" name="profilePhoto" id="profilePhotoInput" accept="image/*" style="display: none;" onchange="previewNidImage(this, 'profilePreview', 'profileIcon')">
      <span style="font-size: 11px; color: var(--text-muted); margin-top: 4px; display: block;">ছবি যোগ করতে গোল চিহ্নে ক্লিক করুন</span>
    </div>

    <!-- 1. Full Name -->
    <div class="form-group">
      <label class="form-label">আপনার পূর্ণ নাম (Full Name)</label>
      <input type="text" name="name" class="form-control" placeholder="যেমন: মো: রফিকুল ইসলাম" required value="<?php echo htmlspecialchars($_POST['name'] ?? ''); ?>">
    </div>

    <!-- 2. Mobile Number -->
    <div class="form-group">
      <label class="form-label">মোবাইল নম্বর (11 Digits)</label>
      <input type="tel" name="mobile" class="form-control" placeholder="01XXXXXXXXX" required value="<?php echo htmlspecialchars($_POST['mobile'] ?? ''); ?>">
    </div>

    <!-- 3. Gmail -->
    <div class="form-group">
      <label class="form-label">জিমেইল / ইমেইল (যেখানে ভেরিফিকেশন কোড যাবে)</label>
      <input type="email" name="email" id="userEmail" class="form-control" placeholder="yourname@gmail.com" required value="<?php echo htmlspecialchars($_POST['email'] ?? ''); ?>">
    </div>

    <!-- 4. Custom IP Digit Selection -->
    <div class="form-group">
      <label class="form-label">আপনার পছন্দের আইপি নম্বর (শেষ ৬ ডিজিট)</label>
      <div class="ip-input-group">
        <span class="ip-prefix-badge">09612</span>
        <input type="text" name="customDigits" id="ipDigits" class="ip-input-field" maxlength="6" placeholder="888999" value="<?php echo htmlspecialchars($_POST['customDigits'] ?? '888999'); ?>">
      </div>
      <span style="font-size: 11px; color: var(--primary-light); margin-top: 4px; display: block;">
        মোট ১১ ডিজিট: 09612 + আপনার পছন্দের ৬ ডিজিট
      </span>
    </div>

    <!-- 5. Password -->
    <div class="form-group">
      <label class="form-label">পাসওয়ার্ড</label>
      <input type="password" name="password" class="form-control" placeholder="কমপক্ষে ৬ ডিজিটের পাসওয়ার্ড" required>
    </div>

    <!-- 6. NID Number -->
    <div class="form-group">
      <label class="form-label">জাতীয় পরিচয়পত্র নম্বর (NID Number)</label>
      <input type="text" name="nidNumber" class="form-control" placeholder="১০ / ১৩ / ১৭ ডিজিটের NID" required value="<?php echo htmlspecialchars($_POST['nidNumber'] ?? ''); ?>">
    </div>

    <!-- 7. NID Photos with Real Preview -->
    <div class="form-group">
      <label class="form-label">NID কার্ডের ছবি আপলোড (সামনে ও পিছনে)</label>
      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
        
        <!-- NID Front -->
        <div class="nid-upload-card" onclick="document.getElementById('nidFrontInput').click()">
          <img id="nidFrontPreview" class="nid-preview-img" alt="Front Preview">
          <i class="fas fa-camera nid-upload-icon" id="frontIcon"></i>
          <div style="font-size: 12px; font-weight: 700;">NID সামনের Part</div>
          <div style="font-size: 10px; color: var(--text-muted);" id="frontText">ছবি সিলেক্ট করুন</div>
          <input type="file" name="nidFront" id="nidFrontInput" accept="image/*" style="display: none;" onchange="previewNidImage(this, 'nidFrontPreview', 'frontText')">
        </div>

        <!-- NID Back -->
        <div class="nid-upload-card" onclick="document.getElementById('nidBackInput').click()">
          <img id="nidBackPreview" class="nid-preview-img" alt="Back Preview">
          <i class="fas fa-camera nid-upload-icon" id="backIcon"></i>
          <div style="font-size: 12px; font-weight: 700;">NID পিছনের Part</div>
          <div style="font-size: 10px; color: var(--text-muted);" id="backText">ছবি সিলেক্ট করুন</div>
          <input type="file" name="nidBack" id="nidBackInput" accept="image/*" style="display: none;" onchange="previewNidImage(this, 'nidBackPreview', 'backText')">
        </div>

      </div>
    </div>

    <button type="submit" class="btn btn-primary" style="margin-top: 10px;">
      <i class="fas fa-paper-plane"></i> ওটিপি ও জিমেইল ভেরিফিকেশনে যান
    </button>
  </form>

  <div style="text-align: center; margin-top: 20px; font-size: 13px; color: var(--text-muted);">
    ইতিমধ্যে একাউন্ট আছে? <a href="login.php" style="color: var(--primary-light); font-weight: 700; text-decoration: none;">লগইন করুন</a>
  </div>
</div>

<?php include __DIR__ . '/includes/footer.php'; ?>
