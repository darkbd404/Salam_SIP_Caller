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
  
  <!-- PWA Meta Tags -->
  <link rel="manifest" href="manifest.json?v=2.6">
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

  <link rel="stylesheet" href="assets/css/style.css?v=2.6">
</head>
<body>

<div class="app-container">
  
  <!-- Top Header with Precision Flex Alignment -->
  <header class="app-header">
    <a href="index.php" class="brand-link">
      <div class="brand-icon">
        <i class="fas fa-phone-volume"></i>
      </div>
      <div style="min-width: 0;">
        <div class="brand-title">Salam SIP Caller</div>
        <div class="brand-subtitle"><i class="fas fa-shield-halved" style="color: #00E676;"></i> 09612 BTRC Verified</div>
      </div>
    </a>

    <div class="header-actions">
      <!-- Dark/Light Mode Switcher -->
      <button onclick="toggleTheme()" class="theme-toggle-btn" title="ডে / নাইট মোড" id="themeBtn">
        <i class="fas fa-moon" id="themeIcon"></i>
      </button>

      <?php if ($currentUser): ?>
        <?php if ($isAdmin): ?>
          <a href="admin.php" title="অ্যাডমিন প্যানেল" style="background: rgba(255, 179, 0, 0.2); border: 1px solid #FFB300; color: #FFB300; padding: 4px 6px; border-radius: 8px; font-size: 10px; text-decoration: none; font-weight: 700; display: flex; align-items: center; gap: 3px;">
            <i class="fas fa-user-shield"></i> অ্যাডমিন
          </a>
        <?php endif; ?>

        <a href="recharge.php" style="text-decoration: none;" title="ব্যালেন্স রিচার্জ">
          <span class="badge-balance">
            ৳ <?php echo number_format($currentUser['balance'] ?? 0, 2); ?>
          </span>
        </a>

        <a href="profile.php" style="text-decoration: none;">
          <span class="badge-ip">
            <i class="fas fa-signal" style="font-size: 9px;"></i> <?php echo htmlspecialchars(substr($currentUser['ipNumber'] ?? '09612', 0, 11)); ?>
          </span>
        </a>
      <?php else: ?>
        <a href="login.php" class="btn btn-outline" style="padding: 5px 10px; font-size: 11px; width: auto;">লগইন</a>
      <?php endif; ?>
    </div>
  </header>

  <!-- PWA Install Banner -->
  <div id="pwaInstallBanner" class="pwa-install-banner" style="display: none; margin: 10px 16px 0 16px; background: linear-gradient(90deg, #004D40, #00897B); border-radius: 12px; padding: 10px 14px; color: #fff; align-items: center; justify-content: space-between;">
    <div>
      <div style="font-weight: 700; font-size: 12px;"><i class="fas fa-download"></i> Salam SIP অ্যাপ ইনস্টল করুন</div>
      <div style="font-size: 10px; opacity: 0.9;">Android ডিভাইসে ফুলস্ক্রিন ব্যাকগ্রাউন্ড অ্যাপ</div>
    </div>
    <button id="pwaInstallBtn" class="pwa-install-btn" style="background: #fff; color: #004D40; border: none; padding: 5px 12px; border-radius: 8px; font-weight: 700; font-size: 11px; cursor: pointer;">ইনস্টল</button>
  </div>

  <main class="app-content">
