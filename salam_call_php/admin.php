<?php
require_once __DIR__ . '/includes/config.php';
$adminUser = require_admin();

$msg = '';
$error = '';

$users = read_json_data(USER_JSON_FILE);
$recharges = read_json_data(RECHARGE_JSON_FILE);
$calls = read_json_data(CALLS_JSON_FILE);

// Handle Admin Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = clean_input($_POST['action'] ?? '');
    
    // 1. Approve Recharge
    if ($action === 'approve_recharge') {
        $rechargeId = clean_input($_POST['recharge_id'] ?? '');
        $rechargeAmount = 0;
        $targetUserIp = '';

        foreach ($recharges as &$r) {
            if ($r['id'] === $rechargeId && $r['status'] === 'PENDING') {
                $r['status'] = 'APPROVED';
                $r['approvedAt'] = time();
                $rechargeAmount = (float)$r['amount'];
                $targetUserIp = $r['userIp'];
                break;
            }
        }
        write_json_data(RECHARGE_JSON_FILE, $recharges);

        if ($rechargeAmount > 0 && !empty($targetUserIp)) {
            foreach ($users as &$u) {
                if ($u['ipNumber'] === $targetUserIp) {
                    $u['balance'] = round(($u['balance'] ?? 0) + $rechargeAmount, 2);
                    break;
                }
            }
            write_json_data(USER_JSON_FILE, $users);
            $msg = "৳ $rechargeAmount রিচার্জ সফলভাবে অনুমোদন ও ব্যালেন্স যোগ করা হয়েছে!";
        }
    }

    // 2. Reject Recharge
    elseif ($action === 'reject_recharge') {
        $rechargeId = clean_input($_POST['recharge_id'] ?? '');
        foreach ($recharges as &$r) {
            if ($r['id'] === $rechargeId) {
                $r['status'] = 'REJECTED';
                break;
            }
        }
        write_json_data(RECHARGE_JSON_FILE, $recharges);
        $msg = 'রিচার্জ রিকুয়েস্ট বাতিল করা হয়েছে।';
    }

    // 3. Edit User Full Account
    elseif ($action === 'edit_user_full') {
        $targetId = (int)($_POST['user_id'] ?? 0);
        $uName = clean_input($_POST['name'] ?? '');
        $uMobile = clean_input($_POST['mobileNumber'] ?? '');
        $uEmail = clean_input($_POST['email'] ?? '');
        $uPassword = clean_input($_POST['password'] ?? '');
        $uIp = clean_input($_POST['ipNumber'] ?? '');
        $uNid = clean_input($_POST['nidNumber'] ?? '');
        $uBalance = (float)($_POST['balance'] ?? 0);
        $uBlocked = isset($_POST['isBlockedByAdmin']);
        $uCallAllowed = isset($_POST['isCallAllowedByAdmin']);
        $uKyc = isset($_POST['isKycVerified']);

        foreach ($users as &$u) {
            if ($u['id'] === $targetId) {
                $u['name'] = $uName;
                $u['mobileNumber'] = $uMobile;
                $u['email'] = $uEmail;
                if (!empty($uPassword)) $u['password'] = $uPassword;
                $u['ipNumber'] = $uIp;
                $u['nidNumber'] = $uNid;
                $u['balance'] = $uBalance;
                $u['isBlockedByAdmin'] = $uBlocked;
                $u['isCallAllowedByAdmin'] = $uCallAllowed;
                $u['isKycVerified'] = $uKyc;
                break;
            }
        }
        write_json_data(USER_JSON_FILE, $users);
        $msg = 'ইউজার একাউন্ট সফলভাবে পরিবর্তন ও আপডেট করা হয়েছে!';
    }

    // 4. Delete User Permanently
    elseif ($action === 'delete_user') {
        $targetId = (int)($_POST['user_id'] ?? 0);
        $filtered = [];
        foreach ($users as $u) {
            if ($u['id'] !== $targetId) {
                $filtered[] = $u;
            }
        }
        $users = $filtered;
        write_json_data(USER_JSON_FILE, $users);
        $msg = 'ইউজার একাউন্ট ডাটাবেজ থেকে মুছে ফেলা হয়েছে।';
    }

    // 5. Quick Add Balance
    elseif ($action === 'add_balance') {
        $targetId = (int)($_POST['user_id'] ?? 0);
        $addBal = (float)($_POST['amount'] ?? 0);
        foreach ($users as &$u) {
            if ($u['id'] === $targetId) {
                $u['balance'] = round(($u['balance'] ?? 0) + $addBal, 2);
                break;
            }
        }
        write_json_data(USER_JSON_FILE, $users);
        $msg = "৳ $addBal ব্যালেন্স প্রদান করা হয়েছে!";
    }
}

// Reload updated data
$users = read_json_data(USER_JSON_FILE);
$recharges = read_json_data(RECHARGE_JSON_FILE);

include __DIR__ . '/includes/header.php';
?>

<div class="card">
  
  <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
    <div>
      <div class="card-title" style="color: #FFB300; margin-bottom: 2px;">
        <i class="fas fa-shield-halved"></i> মাস্টার অ্যাডমিন কন্ট্রোল প্যানেল
      </div>
      <div style="font-size: 11px; color: var(--text-muted);">
        লগইন: <strong><?php echo MASTER_ADMIN_NAME; ?></strong> (<?php echo MASTER_ADMIN_EMAIL; ?>)
      </div>
    </div>
    <span class="badge-ip" style="background: rgba(255, 179, 0, 0.2); border-color: #FFB300; color: #FFB300;">SUPER ADMIN</span>
  </div>

  <?php if ($msg): ?>
    <div class="alert alert-success"><i class="fas fa-circle-check"></i> <?php echo $msg; ?></div>
  <?php endif; ?>

  <!-- Admin Stats Overview -->
  <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-bottom: 16px;">
    <div style="background: rgba(0,0,0,0.3); padding: 10px; border-radius: 10px; text-align: center; border: 1px solid var(--card-border);">
      <div style="font-size: 10px; color: var(--text-muted);">মোট ইউজার</div>
      <div style="font-size: 18px; font-weight: 700; color: #00E676;"><?php echo count($users); ?></div>
    </div>
    <div style="background: rgba(0,0,0,0.3); padding: 10px; border-radius: 10px; text-align: center; border: 1px solid var(--card-border);">
      <div style="font-size: 10px; color: var(--text-muted);">পেন্ডিং রিচার্জ</div>
      <div style="font-size: 18px; font-weight: 700; color: #FFB300;">
        <?php 
          $pendingCount = 0;
          foreach ($recharges as $r) {
              if (($r['status'] ?? '') === 'PENDING') $pendingCount++;
          }
          echo $pendingCount;
        ?>
      </div>
    </div>
    <div style="background: rgba(0,0,0,0.3); padding: 10px; border-radius: 10px; text-align: center; border: 1px solid var(--card-border);">
      <div style="font-size: 10px; color: var(--text-muted);">মোট কল লগ</div>
      <div style="font-size: 18px; font-weight: 700; color: #4DB6AC;"><?php echo count($calls); ?></div>
    </div>
  </div>

  <!-- Admin Navigation Tabs -->
  <div style="display: flex; gap: 6px; margin-bottom: 16px;">
    <button id="tabUsersBtn" onclick="switchAdminTab('users')" class="btn" style="flex: 1; padding: 7px; font-size: 12px; background: #FFB300; color: #000; font-weight: 700;">
      <i class="fas fa-users-gear"></i> ইউজার লিস্ট ও এডিট
    </button>
    <button id="tabRechargeBtn" onclick="switchAdminTab('recharge')" class="btn" style="flex: 1; padding: 7px; font-size: 12px; background: rgba(255,255,255,0.08); color: var(--text-muted);">
      <i class="fas fa-money-bill-wave"></i> রিচার্জ অনুমোদন
    </button>
  </div>

  <!-- Tab 1: User Management & Full Account Control -->
  <div id="adminUsersTab">
    <div style="display: flex; flex-direction: column; gap: 12px;">
      <?php foreach ($users as $u): ?>
        <div style="background: rgba(0,0,0,0.4); border: 1px solid var(--card-border); border-radius: 12px; padding: 14px;">
          
          <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
            <div style="display: flex; align-items: center; gap: 10px;">
              <div style="width: 40px; height: 40px; border-radius: 50%; background: var(--primary); display: flex; align-items: center; justify-content: center; font-size: 16px; color: #fff; overflow: hidden;">
                <?php if (!empty($u['profilePhoto']) && file_exists(__DIR__ . '/' . $u['profilePhoto'])): ?>
                  <img src="<?php echo htmlspecialchars($u['profilePhoto']); ?>" style="width: 100%; height: 100%; object-fit: cover;" alt="Avatar">
                <?php else: ?>
                  <i class="fas fa-user"></i>
                <?php endif; ?>
              </div>
              <div>
                <div style="font-size: 14px; font-weight: 700; color: #fff;"><?php echo htmlspecialchars($u['name']); ?></div>
                <div style="font-size: 11px; color: var(--primary-light); font-weight: 600;">আইপি: <?php echo htmlspecialchars($u['ipNumber']); ?></div>
              </div>
            </div>

            <span style="font-size: 10px; padding: 2px 8px; border-radius: 4px; font-weight: bold; background: <?php echo !empty($u['isBlockedByAdmin']) ? '#FF5252' : '#00E676'; ?>; color: #000;">
              <?php echo !empty($u['isBlockedByAdmin']) ? 'ব্লকড' : 'সক্রিয়'; ?>
            </span>
          </div>

          <div style="font-size: 12px; color: var(--text-muted); line-height: 1.6; margin-bottom: 10px; background: rgba(255,255,255,0.03); padding: 8px; border-radius: 8px;">
            <div><strong>মোবাইল:</strong> <?php echo htmlspecialchars($u['mobileNumber']); ?></div>
            <div><strong>ইমেইল:</strong> <?php echo htmlspecialchars($u['email']); ?></div>
            <div><strong>NID:</strong> <?php echo htmlspecialchars($u['nidNumber']); ?></div>
            <div><strong>ব্যালেন্স:</strong> <span style="color: #FFD54F; font-weight: 700;">৳ <?php echo number_format($u['balance'] ?? 0, 2); ?></span></div>
          </div>

          <!-- NID Photos preview if uploaded -->
          <?php if (!empty($u['nidFrontPath']) || !empty($u['nidBackPath'])): ?>
            <div style="display: flex; gap: 8px; margin-bottom: 10px;">
              <?php if (!empty($u['nidFrontPath']) && file_exists(__DIR__ . '/' . $u['nidFrontPath'])): ?>
                <a href="<?php echo htmlspecialchars($u['nidFrontPath']); ?>" target="_blank" class="btn btn-outline" style="padding: 4px 8px; font-size: 10px; width: auto;">
                  <i class="fas fa-id-card"></i> NID Front
                </a>
              <?php endif; ?>
              <?php if (!empty($u['nidBackPath']) && file_exists(__DIR__ . '/' . $u['nidBackPath'])): ?>
                <a href="<?php echo htmlspecialchars($u['nidBackPath']); ?>" target="_blank" class="btn btn-outline" style="padding: 4px 8px; font-size: 10px; width: auto;">
                  <i class="fas fa-id-card"></i> NID Back
                </a>
              <?php endif; ?>
            </div>
          <?php endif; ?>

          <!-- Action Buttons -->
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 6px; margin-bottom: 8px;">
            <button type="button" onclick="openEditModal(<?php echo htmlspecialchars(json_encode($u)); ?>)" class="btn btn-primary" style="padding: 6px; font-size: 11px;">
              <i class="fas fa-edit"></i> সম্পূর্ণ তথ্য পরিবর্তন
            </button>
            <form action="admin.php" method="POST" onsubmit="return confirm('আপনি কি নিশ্চিতভাবে এই ইউজার ডিলিট করতে চান?')">
              <input type="hidden" name="action" value="delete_user">
              <input type="hidden" name="user_id" value="<?php echo $u['id']; ?>">
              <button type="submit" class="btn" style="padding: 6px; font-size: 11px; background: rgba(255, 82, 82, 0.2); color: #FF5252; width: 100%;">
                <i class="fas fa-trash"></i> ডিলিট একাউন্ট
              </button>
            </form>
          </div>

          <!-- Quick Balance Add Form -->
          <form action="admin.php" method="POST" style="display: flex; gap: 6px;">
            <input type="hidden" name="action" value="add_balance">
            <input type="hidden" name="user_id" value="<?php echo $u['id']; ?>">
            <input type="number" step="1" name="amount" placeholder="+৳ ব্যালেন্স দিন" style="flex: 1; padding: 6px 10px; font-size: 11px; background: rgba(0,0,0,0.3); border: 1px solid var(--card-border); color: #fff; border-radius: 6px;" required>
            <button type="submit" class="btn" style="width: auto; padding: 6px 12px; font-size: 11px; background: #00E676; color: #000; font-weight: bold; border-radius: 6px;">যোগ</button>
          </form>

        </div>
      <?php endforeach; ?>
    </div>
  </div>

  <!-- Tab 2: Recharge Approval Requests -->
  <div id="adminRechargeTab" style="display: none;">
    <div style="display: flex; flex-direction: column; gap: 12px;">
      <?php if (empty($recharges)): ?>
        <div style="text-align: center; padding: 30px; color: var(--text-muted); font-size: 13px;">
          এখনও কোনো রিচার্জের রিকুয়েস্ট জমা পড়েনি।
        </div>
      <?php else: ?>
        <?php foreach (array_reverse($recharges) as $r): ?>
          <div style="background: rgba(0,0,0,0.4); border: 1px solid var(--card-border); border-radius: 12px; padding: 14px;">
            
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
              <span style="font-weight: 700; font-size: 15px; color: #FFD54F;">৳ <?php echo number_format($r['amount'], 2); ?></span>
              <span style="font-size: 10px; padding: 2px 8px; border-radius: 4px; font-weight: bold; background: <?php echo $r['status'] === 'APPROVED' ? '#00E676' : ($r['status'] === 'REJECTED' ? '#FF5252' : '#FFB300'); ?>; color: #000;">
                <?php echo $r['status']; ?>
              </span>
            </div>

            <div style="font-size: 12px; color: var(--text-muted); line-height: 1.6; margin-bottom: 10px;">
              <div><strong>ইউজার আইপি:</strong> <?php echo htmlspecialchars($r['userIp']); ?> (<?php echo htmlspecialchars($r['userName']); ?>)</div>
              <div><strong>পেমেন্ট মাধ্যম:</strong> <?php echo htmlspecialchars($r['method']); ?></div>
              <div><strong>প্রেরক নম্বর:</strong> <?php echo htmlspecialchars($r['senderNumber']); ?></div>
              <div><strong>TrxID / ট্রানজেকশন আইডি:</strong> <code style="color: #00E676; font-size: 13px; font-weight: bold;"><?php echo htmlspecialchars($r['trxId']); ?></code></div>
              <div><strong>সময়:</strong> <?php echo date('d M Y, h:i A', $r['timestamp']); ?></div>
            </div>

            <?php if ($r['status'] === 'PENDING'): ?>
              <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">
                <form action="admin.php" method="POST">
                  <input type="hidden" name="action" value="approve_recharge">
                  <input type="hidden" name="recharge_id" value="<?php echo $r['id']; ?>">
                  <button type="submit" class="btn" style="background: #00E676; color: #000; font-weight: bold; padding: 8px; font-size: 12px;">
                    <i class="fas fa-check"></i> অনুমোদন ও ব্যালেন্স যোগ
                  </button>
                </form>

                <form action="admin.php" method="POST">
                  <input type="hidden" name="action" value="reject_recharge">
                  <input type="hidden" name="recharge_id" value="<?php echo $r['id']; ?>">
                  <button type="submit" class="btn" style="background: rgba(255, 82, 82, 0.2); color: #FF5252; padding: 8px; font-size: 12px;">
                    <i class="fas fa-times"></i> বাতিল করুন
                  </button>
                </form>
              </div>
            <?php endif; ?>

          </div>
        <?php endforeach; ?>
      <?php endif; ?>
    </div>
  </div>

</div>

<!-- Full User Edit Modal Popup -->
<div id="userEditModal" style="display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.85); z-index: 99999; padding: 20px; overflow-y: auto; font-family: 'Hind Siliguri', sans-serif;">
  <div style="background: #0B1917; border: 1px solid var(--primary); border-radius: 14px; max-width: 480px; margin: 20px auto; padding: 18px; color: #fff;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; border-bottom: 1px solid var(--card-border); padding-bottom: 8px;">
      <div style="font-size: 16px; font-weight: 700; color: #FFB300;"><i class="fas fa-user-pen"></i> সম্পূর্ণ একাউন্ট এডিট ও নিয়ন্ত্রণ</div>
      <button onclick="document.getElementById('userEditModal').style.display='none'" style="background: none; border: none; color: #fff; font-size: 18px; cursor: pointer;">&times;</button>
    </div>

    <form action="admin.php" method="POST">
      <input type="hidden" name="action" value="edit_user_full">
      <input type="hidden" name="user_id" id="editUserId">

      <div class="form-group">
        <label class="form-label">নাম</label>
        <input type="text" name="name" id="editName" class="form-control" required>
      </div>

      <div class="form-group">
        <label class="form-label">মোবাইল নম্বর</label>
        <input type="tel" name="mobileNumber" id="editMobile" class="form-control" required>
      </div>

      <div class="form-group">
        <label class="form-label">জিমেইল / ইমেইল</label>
        <input type="email" name="email" id="editEmail" class="form-control" required>
      </div>

      <div class="form-group">
        <label class="form-label">০9612 আইপি নম্বর</label>
        <input type="text" name="ipNumber" id="editIp" class="form-control" required>
      </div>

      <div class="form-group">
        <label class="form-label">পাসওয়ার্ড (পরিবর্তন করতে চাইলে লিখুন)</label>
        <input type="text" name="password" id="editPassword" class="form-control" placeholder="নতুন পাসওয়ার্ড">
      </div>

      <div class="form-group">
        <label class="form-label">NID নম্বর</label>
        <input type="text" name="nidNumber" id="editNid" class="form-control" required>
      </div>

      <div class="form-group">
        <label class="form-label">ব্যালেন্স (টাকা)</label>
        <input type="number" step="0.01" name="balance" id="editBalance" class="form-control" required>
      </div>

      <!-- Checkboxes -->
      <div style="display: flex; flex-direction: column; gap: 8px; margin-bottom: 16px; background: rgba(0,0,0,0.3); padding: 10px; border-radius: 8px;">
        <label style="display: flex; align-items: center; gap: 8px; font-size: 13px; cursor: pointer;">
          <input type="checkbox" name="isCallAllowedByAdmin" id="editCallAllowed"> কল সুবিধা সক্রিয় রাখুন
        </label>
        <label style="display: flex; align-items: center; gap: 8px; font-size: 13px; cursor: pointer;">
          <input type="checkbox" name="isKycVerified" id="editKyc"> BTRC KYC ভেরিফাইড
        </label>
        <label style="display: flex; align-items: center; gap: 8px; font-size: 13px; color: #FF5252; cursor: pointer;">
          <input type="checkbox" name="isBlockedByAdmin" id="editBlocked"> একাউন্ট স্থগিত / ব্লক করুন
        </label>
      </div>

      <div style="display: flex; gap: 8px;">
        <button type="submit" class="btn btn-primary" style="flex: 1;">সংরক্ষণ করুন</button>
        <button type="button" onclick="document.getElementById('userEditModal').style.display='none'" class="btn btn-outline" style="width: auto;">বাতিল</button>
      </div>
    </form>
  </div>
</div>

<script>
function switchAdminTab(tab) {
  const usersBtn = document.getElementById('tabUsersBtn');
  const rechargeBtn = document.getElementById('tabRechargeBtn');
  const usersTab = document.getElementById('adminUsersTab');
  const rechargeTab = document.getElementById('adminRechargeTab');

  if (tab === 'users') {
    usersBtn.style.background = '#FFB300';
    usersBtn.style.color = '#000';
    usersBtn.style.fontWeight = '700';

    rechargeBtn.style.background = 'rgba(255,255,255,0.08)';
    rechargeBtn.style.color = 'var(--text-muted)';
    rechargeBtn.style.fontWeight = 'normal';

    usersTab.style.display = 'block';
    rechargeTab.style.display = 'none';
  } else {
    rechargeBtn.style.background = '#FFB300';
    rechargeBtn.style.color = '#000';
    rechargeBtn.style.fontWeight = '700';

    usersBtn.style.background = 'rgba(255,255,255,0.08)';
    usersBtn.style.color = 'var(--text-muted)';
    usersBtn.style.fontWeight = 'normal';

    rechargeTab.style.display = 'block';
    usersTab.style.display = 'none';
  }
}

function openEditModal(user) {
  document.getElementById('editUserId').value = user.id;
  document.getElementById('editName').value = user.name || '';
  document.getElementById('editMobile').value = user.mobileNumber || '';
  document.getElementById('editEmail').value = user.email || '';
  document.getElementById('editIp').value = user.ipNumber || '';
  document.getElementById('editPassword').value = user.password || '';
  document.getElementById('editNid').value = user.nidNumber || '';
  document.getElementById('editBalance').value = user.balance || 0;
  document.getElementById('editCallAllowed').checked = !!user.isCallAllowedByAdmin;
  document.getElementById('editKyc').checked = !!user.isKycVerified;
  document.getElementById('editBlocked').checked = !!user.isBlockedByAdmin;

  document.getElementById('userEditModal').style.display = 'block';
}
</script>

<?php include __DIR__ . '/includes/footer.php'; ?>
