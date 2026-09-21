<?php
/**
 * Salam SIP Caller - IP to IP Real-time Messaging API
 */
header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/../includes/config.php';

$user = require_auth();
$action = clean_input($_GET['action'] ?? ($_POST['action'] ?? ''));

$allMessages = read_json_data(MESSAGES_JSON_FILE);

// 1. Fetch conversation with peer
if ($action === 'GET_MESSAGES') {
    $peerIp = clean_input($_GET['peer'] ?? '');
    $myIp = $user['ipNumber'];

    $convo = [];
    foreach ($allMessages as $msg) {
        if (($msg['from'] === $myIp && $msg['to'] === $peerIp) || ($msg['from'] === $peerIp && $msg['to'] === $myIp)) {
            $convo[] = $msg;
        }
    }

    echo json_encode([
        'success' => true,
        'messages' => $convo,
        'myBalance' => $user['balance'] ?? 0
    ]);
    exit;
}

// 2. Send Message (Cost: 10 Poisha / ৳ 0.10)
if ($action === 'SEND_MESSAGE') {
    $rawInput = file_get_contents('php://input');
    $data = json_decode($rawInput, true) ?: $_POST;

    $peerIp = clean_input($data['peer'] ?? '');
    $text = clean_input($data['text'] ?? '');

    if (empty($peerIp) || empty($text)) {
        echo json_encode(['success' => false, 'message' => 'বার্তা বা প্রাপক ফাঁকা হতে পারে না']);
        exit;
    }

    // Refresh user balance
    $users = read_json_data(USER_JSON_FILE);
    $currentUserIndex = -1;
    foreach ($users as $idx => $u) {
        if ($u['id'] == $user['id']) {
            $currentUserIndex = $idx;
            break;
        }
    }

    $currentBalance = $users[$currentUserIndex]['balance'] ?? 0;
    if ($currentBalance < MSG_RATE_PER_MSG) {
        echo json_encode([
            'success' => false,
            'message' => 'বার্তা পাঠানোর জন্য পর্যাপ্ত ব্যালেন্স নেই (১০ পয়সা)। রিচার্জ করুন।',
            'needRecharge' => true
        ]);
        exit;
    }

    // Deduct 10 Poisha
    $users[$currentUserIndex]['balance'] = round($currentBalance - MSG_RATE_PER_MSG, 2);
    write_json_data(USER_JSON_FILE, $users);

    $newMsg = [
        'id' => 'msg_' . time() . '_' . rand(100, 999),
        'from' => $user['ipNumber'],
        'fromName' => $user['name'],
        'to' => $peerIp,
        'text' => $text,
        'timestamp' => time(),
        'cost' => MSG_RATE_PER_MSG
    ];

    $allMessages[] = $newMsg;
    write_json_data(MESSAGES_JSON_FILE, $allMessages);

    echo json_encode([
        'success' => true,
        'message' => $newMsg,
        'newBalance' => $users[$currentUserIndex]['balance']
    ]);
    exit;
}

echo json_encode(['success' => false, 'message' => 'Invalid action']);
