<?php
/**
 * Salam SIP Caller - Core Configuration & JSON Database Engine
 * 09612 BTRC Officials Verified IP Telephony
 */

// Error handling & compatibility
error_reporting(E_ALL & ~E_NOTICE & ~E_DEPRECATED & ~E_WARNING);
@ini_set('display_errors', '0');

// Safe Session Initialization
if (session_status() === PHP_SESSION_NONE) {
    @session_start();
}

// PHP 7.x Polyfills for complete host compatibility
if (!function_exists('str_starts_with')) {
    function str_starts_with($haystack, $needle) {
        return (string)$needle === '' || strncmp((string)$haystack, (string)$needle, strlen((string)$needle)) === 0;
    }
}
if (!function_exists('str_contains')) {
    function str_contains($haystack, $needle) {
        return (string)$needle === '' || strpos((string)$haystack, (string)$needle) !== false;
    }
}
if (!function_exists('str_ends_with')) {
    function str_ends_with($haystack, $needle) {
        return (string)$needle === '' || substr((string)$haystack, -strlen((string)$needle)) === (string)$needle;
    }
}

// Paths
define('BASE_DIR', __DIR__ . '/..');
define('DATA_DIR', BASE_DIR . '/data');
define('UPLOADS_DIR', BASE_DIR . '/uploads');
define('USER_JSON_FILE', DATA_DIR . '/user.json');
define('CALLS_JSON_FILE', DATA_DIR . '/calls.json');
define('CONTACTS_JSON_FILE', DATA_DIR . '/contacts.json');
define('RECHARGE_JSON_FILE', DATA_DIR . '/recharges.json');
define('SIGNALS_JSON_FILE', DATA_DIR . '/signals.json');
define('MESSAGES_JSON_FILE', DATA_DIR . '/messages.json');
define('SETTINGS_JSON_FILE', DATA_DIR . '/settings.json');

// App Branding & Details
define('APP_NAME', 'Salam SIP Caller');
define('APP_NAME_BN', 'সালাম এসআইপি কলার');
define('APP_SUBTITLE', '09612 BTRC Officials Verified');
define('IP_PREFIX', '09612');

// Rates & Pricing
define('CALL_RATE_PER_MIN', 0.30); // 30 poisha / min
define('MSG_RATE_PER_MSG', 0.10);  // 10 poisha / message
define('WELCOME_BONUS', 50.00);    // 50 Taka Welcome Bonus

// Master Admin Fixed Credentials (ONLY this user has Admin privilege)
define('MASTER_ADMIN_NAME', 'Abdus Salam');
define('MASTER_ADMIN_MOBILE', '01620230864');
define('MASTER_ADMIN_IP', '09612230864');
define('MASTER_ADMIN_EMAIL', 'salam230864@gmail.com');

// Ensure required directories exist
if (!file_exists(DATA_DIR)) {
    @mkdir(DATA_DIR, 0777, true);
}
if (!file_exists(UPLOADS_DIR)) {
    @mkdir(UPLOADS_DIR, 0777, true);
}

// Thread-safe JSON Initialization
function init_json_file($file_path, $default_content = '[]') {
    if (!file_exists($file_path)) {
        @file_put_contents($file_path, $default_content);
        @chmod($file_path, 0666);
    }
}

init_json_file(USER_JSON_FILE, '[]');
init_json_file(CALLS_JSON_FILE, '[]');
init_json_file(CONTACTS_JSON_FILE, '[]');
init_json_file(RECHARGE_JSON_FILE, '[]');
init_json_file(SIGNALS_JSON_FILE, '[]');
init_json_file(MESSAGES_JSON_FILE, '[]');
init_json_file(SETTINGS_JSON_FILE, json_encode([
    'callRatePerMin' => CALL_RATE_PER_MIN,
    'msgRatePerMsg' => MSG_RATE_PER_MSG,
    'welcomeBonus' => WELCOME_BONUS,
    'bkashNumber' => '01620230864',
    'nagadNumber' => '01620230864',
    'rocketNumber' => '01620230864'
], JSON_PRETTY_PRINT));

// Thread-safe JSON Read
function read_json_data($file_path) {
    if (!file_exists($file_path)) {
        return [];
    }
    $fp = @fopen($file_path, 'r');
    if (!$fp) return [];
    
    flock($fp, LOCK_SH);
    $size = filesize($file_path);
    $content = $size > 0 ? fread($fp, $size) : '[]';
    flock($fp, LOCK_UN);
    fclose($fp);
    
    $data = json_decode($content, true);
    return is_array($data) ? $data : [];
}

// Thread-safe JSON Write
function write_json_data($file_path, $data) {
    $fp = @fopen($file_path, 'w');
    if (!$fp) return false;
    
    flock($fp, LOCK_EX);
    $json = json_encode($data, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    fwrite($fp, $json);
    fflush($fp);
    flock($fp, LOCK_UN);
    fclose($fp);

    // Sync root user.json if modifying user.json
    if ($file_path === USER_JSON_FILE) {
        $rootUserJson = BASE_DIR . '/../user.json';
        if (@file_exists($rootUserJson) || @is_writable(@dirname($rootUserJson))) {
            @file_put_contents($rootUserJson, $json);
        }
    }
    return true;
}

// Ensure Master Admin exists in database
function ensure_master_admin_exists() {
    $users = read_json_data(USER_JSON_FILE);
    $adminFound = false;
    foreach ($users as &$u) {
        if (($u['email'] ?? '') === MASTER_ADMIN_EMAIL || ($u['mobileNumber'] ?? '') === MASTER_ADMIN_MOBILE || ($u['ipNumber'] ?? '') === MASTER_ADMIN_IP) {
            $u['role'] = 'ADMIN';
            $u['isEmailVerified'] = true;
            $u['isKycVerified'] = true;
            $u['isCallAllowedByAdmin'] = true;
            $u['isBlockedByAdmin'] = false;
            $adminFound = true;
            break;
        }
    }
    if (!$adminFound) {
        $adminUser = [
            'id' => count($users) > 0 ? (max(array_column($users, 'id')) + 1) : 1,
            'name' => MASTER_ADMIN_NAME,
            'mobileNumber' => MASTER_ADMIN_MOBILE,
            'email' => MASTER_ADMIN_EMAIL,
            'password' => '123456',
            'nidNumber' => '19951234567890123',
            'nidFrontPath' => '',
            'nidBackPath' => '',
            'profilePhoto' => '',
            'ipNumber' => MASTER_ADMIN_IP,
            'isEmailVerified' => true,
            'isKycVerified' => true,
            'isCallAllowedByAdmin' => true,
            'isBlockedByAdmin' => false,
            'role' => 'ADMIN',
            'balance' => 500.00,
            'createdAt' => time()
        ];
        $users[] = $adminUser;
        write_json_data(USER_JSON_FILE, $users);
    }
}
ensure_master_admin_exists();

// Check if a user is the authorized Master Admin (Strictly only Abdus Salam / 01620230864 / salam230864@gmail.com)
function is_admin_user($user) {
    if (!$user || !is_array($user)) return false;
    $email = strtolower(trim($user['email'] ?? ''));
    $mobile = trim($user['mobileNumber'] ?? '');
    $ip = trim($user['ipNumber'] ?? '');

    return (
        $email === strtolower(MASTER_ADMIN_EMAIL) || 
        $mobile === MASTER_ADMIN_MOBILE || 
        $ip === MASTER_ADMIN_IP
    );
}

// Get Logged In User
function get_logged_in_user() {
    if (!isset($_SESSION['user_id'])) {
        return null;
    }
    $users = read_json_data(USER_JSON_FILE);
    foreach ($users as $u) {
        if ($u['id'] == $_SESSION['user_id']) {
            return $u;
        }
    }
    return null;
}

// Require Authentication
function require_auth() {
    $user = get_logged_in_user();
    if (!$user) {
        header('Location: login.php');
        exit;
    }
    if (!empty($user['isBlockedByAdmin'])) {
        die('<div style="font-family:sans-serif; text-align:center; padding:50px; background:#111; color:#fff;"><h2>🚫 আপনার একাউন্টটি সাময়িকভাবে স্থগিত করা হয়েছে।</h2><p>অ্যাডমিনের সাথে যোগাযোগ করুন: ' . MASTER_ADMIN_EMAIL . '</p><a href="logout.php" style="color:#00E676;">লগআউট</a></div>');
    }
    return $user;
}

// Require Master Admin Access
function require_admin() {
    $user = require_auth();
    if (!is_admin_user($user)) {
        header('HTTP/1.1 403 Forbidden');
        die('<div style="font-family:sans-serif; text-align:center; padding:50px; background:#111; color:#fff;"><h2>⛔ অ্যাক্সেস নিষিদ্ধ (403 Forbidden)</h2><p>এই পেজে শুধুমাত্র প্রধান অ্যাডমিন (' . MASTER_ADMIN_EMAIL . ') প্রবেশ করতে পারবেন।</p><a href="index.php" style="color:#00E676;">হোমে ফিরে যান</a></div>');
    }
    return $user;
}

// Clean and Sanitize Input
function clean_input($data) {
    return htmlspecialchars(trim((string)$data), ENT_QUOTES, 'UTF-8');
}
