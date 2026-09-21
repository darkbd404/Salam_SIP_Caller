<?php
/**
 * Salam SIP Caller - Real-time Call Signaling & Balance Engine
 */
header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/../includes/config.php';

$action = clean_input($_GET['action'] ?? ($_POST['action'] ?? ''));

// Read JSON Helper
$signals = read_json_data(SIGNALS_JSON_FILE);
$users = read_json_data(USER_JSON_FILE);

// Clean up stale signals older than 3 minutes
$currentTime = time();
$activeSignals = [];
foreach ($signals as $s) {
    if (($currentTime - ($s['timestamp'] ?? 0)) < 180 && ($s['status'] ?? '') !== 'ENDED') {
        $activeSignals[] = $s;
    }
}
$signals = $activeSignals;

// 1. Check Incoming Calls for Logged-in User
if ($action === 'CHECK_INCOMING') {
    $myIp = clean_input($_GET['my_ip'] ?? '');
    if (empty($myIp)) {
        $user = get_logged_in_user();
        $myIp = $user['ipNumber'] ?? '';
    }

    if (empty($myIp)) {
        echo json_encode(['status' => 'NONE']);
        exit;
    }

    $incomingCall = null;
    foreach ($signals as $s) {
        if ($s['calleeIp'] === $myIp && $s['status'] === 'RINGING') {
            $incomingCall = $s;
            break;
        }
    }

    if ($incomingCall) {
        echo json_encode([
            'status' => 'INCOMING_CALL',
            'call' => $incomingCall
        ]);
    } else {
        echo json_encode(['status' => 'NONE']);
    }
    exit;
}

// 2. Initiate a Call (Caller)
if ($action === 'INITIATE_CALL') {
    $callerUser = require_auth();
    $rawInput = file_get_contents('php://input');
    $data = json_decode($rawInput, true) ?: $_POST;

    $calleeNumber = clean_input($data['calleeNumber'] ?? '');
    $callType = clean_input($data['callType'] ?? 'AUDIO'); // AUDIO or VIDEO

    // Check caller balance
    if (($callerUser['balance'] ?? 0) < CALL_RATE_PER_MIN) {
        echo json_encode([
            'success' => false,
            'message' => 'অপর্যাপ্ত ব্যালেন্স! কল করার জন্য কমপক্ষে ৳ ০.৩০ ব্যালেন্স থাকতে হবে।',
            'needRecharge' => true
        ]);
        exit;
    }

    if (!empty($callerUser['isBlockedByAdmin'])) {
        echo json_encode(['success' => false, 'message' => 'আপনার একাউন্ট সাময়িকভাবে স্থগিত।']);
        exit;
    }

    if (empty($callerUser['isCallAllowedByAdmin'])) {
        echo json_encode(['success' => false, 'message' => 'অ্যাডমিন থেকে কল সুবিধা অনুমোদন প্রয়োজন।']);
        exit;
    }

    // Find Callee
    $calleeUser = null;
    foreach ($users as $u) {
        if ($u['ipNumber'] === $calleeNumber || $u['mobileNumber'] === $calleeNumber) {
            $calleeUser = $u;
            break;
        }
    }

    $sessionId = 'call_' . time() . '_' . rand(1000, 9999);
    $newSignal = [
        'sessionId' => $sessionId,
        'callerId' => $callerUser['id'],
        'callerIp' => $callerUser['ipNumber'],
        'callerName' => $callerUser['name'],
        'callerPhoto' => $callerUser['profilePhoto'] ?? '',
        'calleeIp' => $calleeNumber,
        'calleeName' => $calleeUser ? $calleeUser['name'] : $calleeNumber,
        'calleePhoto' => $calleeUser ? ($calleeUser['profilePhoto'] ?? '') : '',
        'callType' => $callType,
        'status' => 'RINGING',
        'isIpToIp' => str_starts_with($calleeNumber, IP_PREFIX),
        'timestamp' => time(),
        'connectedAt' => 0,
        'lastBilledAt' => 0
    ];

    $signals[] = $newSignal;
    write_json_data(SIGNALS_JSON_FILE, $signals);

    echo json_encode([
        'success' => true,
        'sessionId' => $sessionId,
        'signal' => $newSignal
    ]);
    exit;
}

// 3. Poll Call Status (Both Caller & Callee)
if ($action === 'POLL_CALL') {
    $sessionId = clean_input($_GET['session_id'] ?? '');
    foreach ($signals as $s) {
        if ($s['sessionId'] === $sessionId) {
            echo json_encode([
                'success' => true,
                'signal' => $s
            ]);
            exit;
        }
    }
    echo json_encode(['success' => false, 'status' => 'ENDED']);
    exit;
}

// 4. Accept Call (Callee)
if ($action === 'ACCEPT_CALL') {
    $user = require_auth();
    $rawInput = file_get_contents('php://input');
    $data = json_decode($rawInput, true) ?: $_POST;
    $sessionId = clean_input($data['sessionId'] ?? '');

    $found = false;
    foreach ($signals as &$s) {
        if ($s['sessionId'] === $sessionId) {
            $s['status'] = 'CONNECTED';
            $s['connectedAt'] = time();
            $s['lastBilledAt'] = time();
            $found = true;
            break;
        }
    }

    if ($found) {
        write_json_data(SIGNALS_JSON_FILE, $signals);
        echo json_encode(['success' => true]);
    } else {
        echo json_encode(['success' => false, 'message' => 'কল পাওয়া যায়নি']);
    }
    exit;
}

// 5. Reject Call (Callee)
if ($action === 'REJECT_CALL') {
    $rawInput = file_get_contents('php://input');
    $data = json_decode($rawInput, true) ?: $_POST;
    $sessionId = clean_input($data['sessionId'] ?? '');

    foreach ($signals as &$s) {
        if ($s['sessionId'] === $sessionId) {
            $s['status'] = 'REJECTED';
            break;
        }
    }
    write_json_data(SIGNALS_JSON_FILE, $signals);
    echo json_encode(['success' => true]);
    exit;
}

// 6. End Call & Deduct Balance (Caller or Callee)
if ($action === 'END_CALL') {
    $rawInput = file_get_contents('php://input');
    $data = json_decode($rawInput, true) ?: $_POST;
    $sessionId = clean_input($data['sessionId'] ?? '');
    $duration = (int)($data['duration'] ?? 0);

    $callerIp = '';
    $calleeIp = '';
    $calleeName = '';
    $callType = 'AUDIO';

    foreach ($signals as &$s) {
        if ($s['sessionId'] === $sessionId) {
            $s['status'] = 'ENDED';
            $callerIp = $s['callerIp'];
            $calleeIp = $s['calleeIp'];
            $calleeName = $s['calleeName'];
            $callType = $s['callType'];
            break;
        }
    }
    write_json_data(SIGNALS_JSON_FILE, $signals);

    // Calculate Billing: 30 Poisha (0.30 Tk) per minute
    if ($duration > 0 && !empty($callerIp)) {
        $minutes = max(1, ceil($duration / 60));
        $cost = round($minutes * CALL_RATE_PER_MIN, 2);

        // Deduct from caller
        $users = read_json_data(USER_JSON_FILE);
        foreach ($users as &$u) {
            if (($u['ipNumber'] ?? '') === $callerIp) {
                $u['balance'] = max(0, round(($u['balance'] ?? 0) - $cost, 2));
                break;
            }
        }
        write_json_data(USER_JSON_FILE, $users);
    } else {
        $cost = 0;
    }

    // Save to Calls History
    if (!empty($callerIp)) {
        $calls = read_json_data(CALLS_JSON_FILE);
        $calls[] = [
            'id' => time() . '_' . rand(100, 999),
            'callerId' => $callerIp,
            'calleeNumber' => $calleeIp,
            'calleeName' => $calleeName,
            'duration' => $duration,
            'cost' => $cost,
            'callType' => $callType,
            'type' => 'OUTGOING',
            'timestamp' => time() * 1000
        ];
        write_json_data(CALLS_JSON_FILE, $calls);
    }

    echo json_encode(['success' => true, 'cost' => $cost]);
    exit;
}

echo json_encode(['success' => false, 'message' => 'Invalid action']);
