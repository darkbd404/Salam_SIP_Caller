<?php
require_once __DIR__ . '/includes/config.php';
$user = require_auth();

$msg = '';
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = clean_input($_POST['action'] ?? '');
    
    if ($action === 'add_contact') {
        $cName = clean_input($_POST['name'] ?? '');
        $cNumber = clean_input($_POST['number'] ?? '');

        if (!empty($cName) && !empty($cNumber)) {
            $contacts = read_json_data(CONTACTS_JSON_FILE);
            $contacts[] = [
                'id' => time() . '_' . rand(100, 999),
                'userId' => $user['id'],
                'name' => $cName,
                'number' => $cNumber,
                'createdAt' => time()
            ];
            write_json_data(CONTACTS_JSON_FILE, $contacts);
            $msg = 'কন্টাক্ট সফলভাবে সংরক্ষণ করা হয়েছে!';
        }
    } elseif ($action === 'import_device_contacts') {
        $raw = file_get_contents('php://input');
        $data = json_decode($raw, true);
        if (is_array($data)) {
            $contacts = read_json_data(CONTACTS_JSON_FILE);
            foreach ($data as $dc) {
                if (!empty($dc['name']) && !empty($dc['number'])) {
                    $contacts[] = [
                        'id' => time() . '_' . rand(100, 999),
                        'userId' => $user['id'],
                        'name' => clean_input($dc['name']),
                        'number' => clean_input($dc['number']),
                        'createdAt' => time()
                    ];
                }
            }
            write_json_data(CONTACTS_JSON_FILE, $contacts);
            echo json_encode(['success' => true]);
            exit;
        }
    }
}

$allUsers = read_json_data(USER_JSON_FILE);
$savedContacts = read_json_data(CONTACTS_JSON_FILE);

include __DIR__ . '/includes/header.php';
?>

<div class="card">
  <div class="card-title"><i class="fas fa-address-book"></i> সালাম টেলিফোনি কন্টাক্টস</div>
  
  <?php if ($msg): ?>
    <div class="alert alert-success"><?php echo $msg; ?></div>
  <?php endif; ?>

  <!-- Tabs -->
  <div style="display: flex; gap: 8px; margin-bottom: 16px; border-bottom: 1px solid var(--card-border); padding-bottom: 10px;">
    <button id="tabIpBtn" class="btn" style="flex: 1; padding: 10px; font-size: 13px; background: var(--primary); color: #fff; font-weight: 700;" onclick="switchTab('ip')">
      <i class="fas fa-satellite-dish"></i> সালাম আইপি ইউজার
    </button>
    <button id="tabPhoneBtn" class="btn" style="flex: 1; padding: 10px; font-size: 13px; background: rgba(125,125,125,0.15); color: var(--text-muted);" onclick="switchTab('phone')">
      <i class="fas fa-mobile-screen"></i> ফোনবুক কন্টাক্ট
    </button>
  </div>

  <!-- Tab 1: Registered Salam IP Users -->
  <div id="tabIpContent">
    <div style="font-size: 12px; color: var(--text-muted); margin-bottom: 12px;">
      সিস্টেমে নিবন্ধিত সকল ০9612 আইপি গ্রাহক (সরাসরি আনলিমিটেড এইচডি কল ও বার্তা):
    </div>

    <div style="display: flex; flex-direction: column; gap: 8px;">
      <?php foreach ($allUsers as $u): ?>
        <?php if ($u['id'] == $user['id']) continue; ?>
        <div class="contact-item-card">
          <div style="display: flex; align-items: center; gap: 12px; min-width: 0; flex: 1;">
            <!-- Profile Thumbnail Avatar -->
            <div class="contact-thumb-avatar">
              <?php if (!empty($u['profilePhoto']) && file_exists(__DIR__ . '/' . $u['profilePhoto'])): ?>
                <img src="<?php echo htmlspecialchars($u['profilePhoto']); ?>" alt="Avatar">
              <?php else: ?>
                <?php echo mb_substr($u['name'] ?? 'U', 0, 1, 'UTF-8'); ?>
              <?php endif; ?>
              <span class="online-dot" title="Active"></span>
            </div>
            
            <div style="min-width: 0;">
              <div style="font-size: 14px; font-weight: 700; color: var(--text-main); display: flex; align-items: center; gap: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                <?php echo htmlspecialchars($u['name']); ?>
                <span style="font-size: 9px; background: rgba(0,230,118,0.2); color: #00E676; padding: 2px 6px; border-radius: 4px; font-weight: 700;">VERIFIED</span>
              </div>
              <div style="font-size: 12px; color: var(--primary-light); font-weight: 600; letter-spacing: 0.5px;">
                <i class="fas fa-signal" style="font-size: 10px;"></i> <?php echo htmlspecialchars($u['ipNumber']); ?>
              </div>
            </div>
          </div>

          <!-- Actions -->
          <div style="display: flex; gap: 6px; flex-shrink: 0;">
            <a href="chat.php?recipient=<?php echo urlencode($u['ipNumber']); ?>" class="btn" style="width: 36px; height: 36px; padding: 0; border-radius: 50%; background: rgba(0, 176, 255, 0.18); color: #00B0FF;" title="মেসেজ পাঠান">
              <i class="fas fa-comment"></i>
            </a>
            <a href="call-active.php?callee=<?php echo urlencode($u['ipNumber']); ?>&type=AUDIO" class="btn" style="width: 36px; height: 36px; padding: 0; border-radius: 50%; background: #00E676; color: #00291B;" title="কল দিন">
              <i class="fas fa-phone"></i>
            </a>
            <a href="call-active.php?callee=<?php echo urlencode($u['ipNumber']); ?>&type=VIDEO" class="btn" style="width: 36px; height: 36px; padding: 0; border-radius: 50%; background: rgba(0, 230, 118, 0.2); color: #00E676;" title="ভিডিও কল">
              <i class="fas fa-video"></i>
            </a>
          </div>
        </div>
      <?php endforeach; ?>
    </div>
  </div>

  <!-- Tab 2: Saved Phonebook Contacts -->
  <div id="tabPhoneContent" style="display: none;">
    <div style="display: flex; gap: 8px; margin-bottom: 14px;">
      <button onclick="document.getElementById('addContactForm').style.display='block'" class="btn btn-primary" style="font-size: 12px; padding: 8px 12px;">
        <i class="fas fa-plus"></i> নতুন কন্টাক্ট
      </button>
      <button onclick="importDeviceContacts()" class="btn btn-outline" style="font-size: 12px; padding: 8px 12px;">
        <i class="fas fa-file-import"></i> ফোনবুক ইম্পোর্ট
      </button>
    </div>

    <!-- Add Contact Form -->
    <div id="addContactForm" style="display: none; background: var(--input-bg); padding: 14px; border-radius: 12px; border: 1px solid var(--card-border); margin-bottom: 14px;">
      <form action="contacts.php" method="POST">
        <input type="hidden" name="action" value="add_contact">
        <div class="form-group">
          <label class="form-label">নাম</label>
          <input type="text" name="name" class="form-control" placeholder="যেমন: মো: সালাম" required>
        </div>
        <div class="form-group">
          <label class="form-label">মোবাইল বা আইপি নম্বর</label>
          <input type="text" name="number" class="form-control" placeholder="017... বা 09612..." required>
        </div>
        <div style="display: flex; gap: 8px;">
          <button type="submit" class="btn btn-primary" style="flex: 1;">সংরক্ষণ করুন</button>
          <button type="button" onclick="document.getElementById('addContactForm').style.display='none'" class="btn btn-outline" style="width: auto;">বাতিল</button>
        </div>
      </form>
    </div>

    <div style="display: flex; flex-direction: column; gap: 8px;">
      <?php if (empty($savedContacts)): ?>
        <div style="text-align: center; padding: 25px; color: var(--text-muted);">কোনো কন্টাক্ট সংরক্ষিত নেই।</div>
      <?php else: ?>
        <?php foreach ($savedContacts as $c): ?>
          <div class="contact-item-card">
            <div style="display: flex; align-items: center; gap: 12px;">
              <div class="contact-thumb-avatar" style="background: linear-gradient(135deg, #FFB300, #F57F17);">
                <?php echo mb_substr($c['name'] ?? 'C', 0, 1, 'UTF-8'); ?>
              </div>
              <div>
                <div style="font-size: 14px; font-weight: 700;"><?php echo htmlspecialchars($c['name']); ?></div>
                <div style="font-size: 12px; color: var(--text-muted);"><?php echo htmlspecialchars($c['number']); ?></div>
              </div>
            </div>
            <a href="call-active.php?callee=<?php echo urlencode($c['number']); ?>&type=AUDIO" class="btn" style="width: 36px; height: 36px; padding: 0; border-radius: 50%; background: #00E676; color: #00291B;">
              <i class="fas fa-phone"></i>
            </a>
          </div>
        <?php endforeach; ?>
      <?php endif; ?>
    </div>
  </div>

</div>

<script>
function switchTab(tab) {
  const ipTab = document.getElementById('tabIpContent');
  const phoneTab = document.getElementById('tabPhoneContent');
  const ipBtn = document.getElementById('tabIpBtn');
  const phoneBtn = document.getElementById('tabPhoneBtn');

  if (tab === 'ip') {
    ipTab.style.display = 'block';
    phoneTab.style.display = 'none';
    ipBtn.style.background = 'var(--primary)';
    ipBtn.style.color = '#fff';
    phoneBtn.style.background = 'rgba(125,125,125,0.15)';
    phoneBtn.style.color = 'var(--text-muted)';
  } else {
    ipTab.style.display = 'none';
    phoneTab.style.display = 'block';
    phoneBtn.style.background = 'var(--primary)';
    phoneBtn.style.color = '#fff';
    ipBtn.style.background = 'rgba(125,125,125,0.15)';
    ipBtn.style.color = 'var(--text-muted)';
  }
}

async function importDeviceContacts() {
  if ('contacts' in navigator && 'ContactsManager' in window) {
    try {
      const props = ['name', 'tel'];
      const contacts = await navigator.contacts.select(props, { multiple: true });
      if (contacts && contacts.length > 0) {
        const payload = contacts.map(c => ({
          name: (c.name && c.name[0]) || 'Contact',
          number: (c.tel && c.tel[0]) || ''
        })).filter(c => c.number.length > 0);

        await fetch('contacts.php', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        window.location.reload();
      }
    } catch (err) {
      alert('ডিভাইস কন্টাক্ট এক্সেস ব্যর্থ হয়েছে');
    }
  } else {
    alert('আপনার ব্রাউজার বা ডিভাইসে সরাসরি কন্টাক্ট পিকার এপিআই সমর্থিত নয়। অনুগ্রহ করে "নতুন কন্টাক্ট" বাটনে যোগ করুন।');
  }
}
</script>

<?php include __DIR__ . '/includes/footer.php'; ?>
