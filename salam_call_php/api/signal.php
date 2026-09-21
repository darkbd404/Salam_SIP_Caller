<?php
/**
 * Salam SIP Caller - High Reliability Signaling & WebRTC Relay Engine
 */
header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/../includes/config.php';

$action = clean_input($_GET['action'] ?? ($_POST['action'] ?? ''));

// Read JSON Data
$signals = read_json_data(SIGNALS_JSON_FILE);
$users = read_json_data(USER_JSON_FILE);

$currentTime = time();
$activeSignals = [];
foreach ($signals as $s) {
    // Keep signals alive for only 2 minutes or until ENDED/REJECTED
    $age = $currentTime - ($s['timestamp'] ?? 0);
    if ($age < 120 && !in_array($s['status'] ?? '', ['ENDED', 'REJECTED_HANDLED'])) {
        $activeSignals[] = $s;
    }
}
$signals = $activeSignals;

// 1. Check Incoming Calls & Unread Messages
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
        if (($s['calleeIp'] ?? '') === $myIp && ($s['status'] ?? '') === 'RINGING') {
            // Must be less than 45 seconds old to prevent zombie rings
            if (($currentTime - ($s['timestamp'] ?? 0)) < 45) {
                $incomingCall = $s;
                break;
            }
        }
    }

    // Unread messages
    $messages = read_json_data(MESSAGES_JSON_FILE);
    $unreadMessages = [];
    foreach ($messages as $m) {
        if (($m['to'] ?? '') === $myIp && empty($m['isRead'])) {
            $unreadMessages[] = $m;
        }
    }

    if ($incomingCall) {
        echo json_encode([
            'status' => 'INCOMING_CALL',
            'call' => $incomingCall,
            'unreadMessages' => $unreadMessages
        ]);
    } else {
        echo json_encode([
            'status' => 'NONE',
            'unreadMessages' => $unreadMessages
        ]);
    }
    exit;
}

// 2. Initiate Call
if ($action === 'INITIATE_CALL') {
    $callerUser = require_auth();
    $rawInput = file_get_contents('php://input');
    $data = json_decode($rawInput, true) ?: $_POST;

    $calleeNumber = clean_input($data['calleeNumber'] ?? '');
    $callType = clean_input($data['callType'] ?? 'AUDIO');
    $sdpOffer = $data['sdpOffer'] ?? null;

    if (($callerUser['balance'] ?? 0) < CALL_RATE_PER_MIN) {
        echo json_encode([
            'success' => false,
            'message' => 'অপর্যাপ্ত ব্যালেন্স! কল করার জন্য কমপক্ষে ৳ ০.৩০ ব্যালেন্স থাকতে হবে।',
            'needRecharge' => true
        ]);
        exit;
    }

    if (!empty($callerUser['isBlockedByAdmin'])) {
        echo json_encode(['success' => false, 'message' => 'আপনার একাউন্ট সাময়িকভাবে স্থগিত।']);
        exit;
    }

    // Find Callee
    $calleeUser = null;
    foreach ($users as $u) {
        if (($u['ipNumber'] ?? '') === $calleeNumber || ($u['mobileNumber'] ?? '') === $calleeNumber) {
            $calleeUser = $u;
            break;
        }
    }

    // Clean any prior pending signal for this pair
    $cleanedSignals = [];
    foreach ($signals as $s) {
        if (!(($s['callerIp'] ?? '') === $callerUser['ipNumber'] && ($s['calleeIp'] ?? '') === $calleeNumber)) {
            $cleanedSignals[] = $s;
        }
    }
    $signals = $cleanedSignals;

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
        'sdpOffer' => $sdpOffer,
        'sdpAnswer' => null,
        'callerCandidates' => [],
        'calleeCandidates' => [],
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

// 3. Poll Call Status & Signals
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
    $rawInput = file_get_contents('php://input');
    $data = json_decode($rawInput, true) ?: $_POST;
    $sessionId = clean_input($data['sessionId'] ?? '');
    $sdpAnswer = $data['sdpAnswer'] ?? null;

    $found = false;
    foreach ($signals as &$s) {
        if ($s['sessionId'] === $sessionId) {
            $s['status'] = 'CONNECTED';
            if ($sdpAnswer) {
                $s['sdpAnswer'] = $sdpAnswer;
            }
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

// 5. Send WebRTC Signals
if ($action === 'SEND_WEBRTC') {
    $rawInput = file_get_contents('php://input');
    $data = json_decode($rawInput, true) ?: $_POST;
    $sessionId = clean_input($data['sessionId'] ?? '');
    $type = clean_input($data['type'] ?? '');
    $payload = $data['payload'] ?? null;

    $found = false;
    foreach ($signals as &$s) {
        if ($s['sessionId'] === $sessionId) {
            if ($type === 'offer') {
                $s['sdpOffer'] = $payload;
            } elseif ($type === 'answer') {
                $s['sdpAnswer'] = $payload;
                $s['status'] = 'CONNECTED';
            } elseif ($type === 'caller_candidate' && $payload) {
                if (!isset($s['callerCandidates']) || !is_array($s['callerCandidates'])) $s['callerCandidates'] = [];
                $s['callerCandidates'][] = $payload;
            } elseif ($type === 'callee_candidate' && $payload) {
                if (!isset($s['calleeCandidates']) || !is_array($s['calleeCandidates'])) $s['calleeCandidates'] = [];
                $s['calleeCandidates'][] = $payload;
            }
            $found = true;
            break;
        }
    }

    if ($found) {
        write_json_data(SIGNALS_JSON_FILE, $signals);
        echo json_encode(['success' => true]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Session not found']);
    }
    exit;
}

// 6. Reject Call
if ($action === 'REJECT_CALL') {
    $rawInput = file_get_contents('php://input');
    $data = json_decode($rawInput, true) ?: $_POST;
    $sessionId = clean_input($data['sessionId'] ?? '');

    $callerIp = '';
    $calleeIp = '';
    $calleeName = '';
    $callType = 'AUDIO';

    foreach ($signals as &$s) {
        if ($s['sessionId'] === $sessionId) {
            $s['status'] = 'REJECTED';
            $callerIp = $s['callerIp'];
            $calleeIp = $s['calleeIp'];
            $calleeName = $s['calleeName'];
            $callType = $s['callType'];
            break;
        }
    }
    write_json_data(SIGNALS_JSON_FILE, $signals);

    // Save Rejected status in History
    if (!empty($callerIp)) {
        $calls = read_json_data(CALLS_JSON_FILE);
        $calls[] = [
            'id' => time() . '_' . rand(100, 999),
            'callerId' => $callerIp,
            'callerNumber' => $callerIp,
            'calleeNumber' => $calleeIp,
            'calleeName' => $calleeName,
            'duration' => 0,
            'durationSeconds' => 0,
            'cost' => 0,
            'callType' => $callType,
            'status' => 'REJECTED',
            'type' => 'REJECTED',
            'timestamp' => time()
        ];
        write_json_data(CALLS_JSON_FILE, $calls);
    }

    echo json_encode(['success' => true]);
    exit;
}

// 7. End Call
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

    // Billing Calculation
    if ($duration > 0 && !empty($callerIp)) {
        $minutes = max(1, ceil($duration / 60));
        $cost = round($minutes * CALL_RATE_PER_MIN, 2);

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

    if (!empty($callerIp)) {
        $calls = read_json_data(CALLS_JSON_FILE);
        $finalStatus = ($duration > 0) ? 'OUTGOING' : 'MISSED';
        
        $calls[] = [
            'id' => time() . '_' . rand(100, 999),
            'callerId' => $callerIp,
            'callerNumber' => $callerIp,
            'calleeNumber' => $calleeIp,
            'calleeName' => $calleeName,
            'duration' => $duration,
            'durationSeconds' => $duration,
            'cost' => $cost,
            'callType' => $callType,
            'status' => $finalStatus,
            'type' => $finalStatus,
            'timestamp' => time()
        ];
        write_json_data(CALLS_JSON_FILE, $calls);
    }

    echo json_encode(['success' => true, 'cost' => $cost]);
    exit;
}

echo json_encode(['success' => false, 'message' => 'Invalid action']);
