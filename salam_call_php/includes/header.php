<?php
if (!defined('APP_NAME')) {
    require_once __DIR__ . '/config.php';
}
$currentUser = get_logged_in_user();
$isAdmin = is_admin_user($currentUser);
?>
<!DOCTYPE html>
<html lang="bn">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title><?php echo APP_NAME . ' - ' . APP_SUBTITLE; ?></title>
  
  <!-- PWA Meta Tags (Distinct App Identity) -->
  <link rel="manifest" href="manifest.json?v=2.5">
  <meta name="theme-color" content="#00897B">
  <meta name="apple-mobile-web-app-capable" content="yes">
  <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent">
  <meta name="apple-mobile-web-app-title" content="Salam SIP">
  <link rel="apple-touch-icon" href="assets/icons/icon-192.png">

  <!-- Icons & Fonts -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Hind+Siliguri:wght@400;500;600;700&display=swap" rel="stylesheet">

  <link rel="stylesheet" href="assets/css/style.css">
</head>
<body>

<div class="app-container">
  
  <!-- Header -->
  <header class="app-header">
    <a href="index.php" style="text-decoration: none; display: flex; align-items: center; gap: 10px; color: inherit;">
      <div class="brand-icon">
        <i class="fas fa-phone-volume"></i>
      </div>
      <div>
        <div class="brand-title">Salam SIP Caller</div>
        <div class="brand-subtitle"><i class="fas fa-shield-halved" style="color: #00E676;"></i> 09612 BTRC Officials Verified</div>
      </div>
    </a>

    <div class="header-actions" style="display: flex; align-items: center; gap: 8px;">
      <?php if ($currentUser): ?>
        <?php if ($isAdmin): ?>
          <a href="admin.php" title="অ্যাডমিন প্যানেল" style="background: rgba(255, 179, 0, 0.2); border: 1px solid #FFB300; color: #FFB300; padding: 4px 8px; border-radius: 8px; font-size: 11px; text-decoration: none; font-weight: 700; display: flex; align-items: center; gap: 4px;">
            <i class="fas fa-user-shield"></i> অ্যাডমিন
          </a>
        <?php endif; ?>

        <a href="recharge.php" style="text-decoration: none;" title="ব্যালেন্স রিচার্জ">
          <span style="background: rgba(255, 213, 79, 0.15); border: 1px solid rgba(255, 213, 79, 0.3); color: #FFD54F; padding: 4px 8px; border-radius: 8px; font-size: 11px; font-weight: 700;">
            ৳ <?php echo number_format($currentUser['balance'] ?? 0, 2); ?>
          </span>
        </a>

        <a href="profile.php" style="text-decoration: none;">
          <span class="badge-ip"><i class="fas fa-signal"></i> <?php echo htmlspecialchars($currentUser['ipNumber'] ?? '09612...'); ?></span>
        </a>
      <?php else: ?>
        <a href="login.php" class="btn btn-outline" style="padding: 6px 12px; font-size: 12px; width: auto;">লগইন</a>
      <?php endif; ?>
    </div>
  </header>

  <!-- PWA Install Banner -->
  <div id="pwaInstallBanner" class="pwa-install-banner" style="display: none; margin: 12px 18px 0 18px;">
    <div>
      <div style="font-weight: 700; font-size: 13px;"><i class="fas fa-download"></i> Salam SIP Caller অ্যাপ ইনস্টল করুন</div>
      <div style="font-size: 11px; opacity: 0.9;">Android ডিভাইসে ফুলস্ক্রিন অ্যাপ হিসেবে ইন্সটল করুন</div>
    </div>
    <button id="pwaInstallBtn" class="pwa-install-btn">ইনস্টল</button>
  </div>

  <main class="app-content">
