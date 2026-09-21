<?php
header('Content-Type: application/json');
require_once __DIR__ . '/../includes/config.php';

$user = get_logged_in_user();
if (!$user) {
    http_response_code(401);
    echo json_encode(['success' => false, 'error' => 'Unauthorized']);
    exit;
}

$input = json_decode(file_get_contents('php://input'), true);
if (!$input) {
    echo json_encode(['success' => false, 'error' => 'Invalid data']);
    exit;
}

$calls = read_json_data(CALLS_JSON_FILE);

$callRecord = [
    'id' => time() . '_' . rand(100, 999),
    'userId' => $user['id'],
    'callerNumber' => $user['ipNumber'],
    'calleeNumber' => $input['calleeNumber'] ?? '',
    'calleeName' => $input['calleeName'] ?? ($input['calleeNumber'] ?? 'Unknown'),
    'durationSeconds' => (int)($input['duration'] ?? 0),
    'type' => $input['type'] ?? 'OUTGOING',
    'timestamp' => time()
];

$calls[] = $callRecord;
write_json_data(CALLS_JSON_FILE, $calls);

// Deduct minor calling cost if applicable
if ($callRecord['durationSeconds'] > 0) {
    $cost = round(($callRecord['durationSeconds'] / 60) * 0.45, 2); // 45 poisha/min
    $users = read_json_data(USER_JSON_FILE);
    foreach ($users as &$u) {
        if ($u['id'] === $user['id']) {
            $u['balance'] = max(0, ($u['balance'] ?? 50.0) - $cost);
            break;
        }
    }
    write_json_data(USER_JSON_FILE, $users);
}

echo json_encode(['success' => true, 'call' => $callRecord]);
