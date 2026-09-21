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
    <button id="tabIpBtn" class="btn" style="flex: 1; padding: 8px; font-size: 13px; background: var(--primary); color: #fff; font-weight: 700;" onclick="switchTab('ip')">
      <i class="fas fa-satellite-dish"></i> সালাম আইপি ইউজার
    </button>
    <button id="tabPhoneBtn" class="btn" style="flex: 1; padding: 8px; font-size: 13px; background: rgba(255,255,255,0.08); color: var(--text-muted);" onclick="switchTab('phone')">
      <i class="fas fa-mobile-screen"></i> ফোনবুক কন্টাক্ট
    </button>
  </div>

  <!-- Tab 1: Registered Salam IP Users -->
  <div id="tabIpContent">
    <div style="font-size: 12px; color: var(--text-muted); margin-bottom: 12px;">
      সিস্টেমে নিবন্ধিত সকল ০9612 আইপি ব্যবহারকারী (সরাসরি আনলিমিটেড এইচডি কল ও বার্তা):
    </div>

    <div style="display: flex; flex-direction: column; gap: 10px;">
      <?php foreach ($allUsers as $u): ?>
        <?php if ($u['id'] == $user['id']) continue; ?>
        <div class="contact-card">
          <div class="contact-avatar" style="overflow: hidden;">
            <?php if (!empty($u['profilePhoto']) && file_exists(__DIR__ . '/' . $u['profilePhoto'])): ?>
              <img src="<?php echo htmlspecialchars($u['profilePhoto']); ?>" style="width: 100%; height: 100%; object-fit: cover;" alt="Avatar">
            <?php else: ?>
              <i class="fas fa-user"></i>
            <?php endif; ?>
          </div>
          
          <div class="contact-info">
            <div class="contact-name">
              <?php echo htmlspecialchars($u['name']); ?>
              <span style="font-size: 10px; background: rgba(0,230,118,0.2); color: #00E676; padding: 2px 6px; border-radius: 4px; margin-left: 4px;">VERIFIED</span>
            </div>
            <div class="contact-num">
              <i class="fas fa-signal" style="font-size: 10px;"></i> আইপি: <?php echo htmlspecialchars($u['ipNumber']); ?>
            </div>
          </div>

          <!-- Actions -->
          <div class="contact-actions">
            <!-- Audio Call -->
            <a href="call-active.php?callee=<?php echo urlencode($u['ipNumber']); ?>&type=AUDIO" class="btn-action btn-audio" title="অডিও কল">
              <i class="fas fa-phone"></i>
            </a>
            <!-- Video Call -->
            <a href="call-active.php?callee=<?php echo urlencode($u['ipNumber']); ?>&type=VIDEO" class="btn-action btn-video" title="ভিডিও কল">
              <i class="fas fa-video"></i>
            </a>
            <!-- Message -->
            <a href="chat.php?peer=<?php echo urlencode($u['ipNumber']); ?>" class="btn-action" style="background: rgba(255, 179, 0, 0.2); color: #FFB300;" title="মেসেজ পাঠান">
              <i class="fas fa-comment"></i>
            </a>
          </div>
        </div>
      <?php endforeach; ?>
    </div>
  </div>

  <!-- Tab 2: Phonebook / Device Contacts -->
  <div id="tabPhoneContent" style="display: none;">
    
    <!-- Action Buttons -->
    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 14px;">
      <button type="button" onclick="importFromDevice()" class="btn btn-outline" style="padding: 8px; font-size: 11px;">
        <i class="fas fa-download"></i> ফোন থেকে আনুন
      </button>
      <button type="button" onclick="document.getElementById('addContactForm').style.display='block'" class="btn btn-primary" style="padding: 8px; font-size: 11px;">
        <i class="fas fa-plus"></i> নতুন কন্টাক্ট
      </button>
    </div>

    <!-- Add Contact Form Modal/Inline -->
    <div id="addContactForm" style="display: none; background: rgba(0,0,0,0.3); border: 1px solid var(--card-border); padding: 12px; border-radius: 10px; margin-bottom: 14px;">
      <div style="font-size: 13px; font-weight: 700; margin-bottom: 8px;">নতুন কন্টাক্ট সংরক্ষণ</div>
      <form action="contacts.php" method="POST">
        <input type="hidden" name="action" value="add_contact">
        <div class="form-group">
          <input type="text" name="name" class="form-control" placeholder="নাম" required>
        </div>
        <div class="form-group">
          <input type="tel" name="number" class="form-control" placeholder="নম্বর (যেমন: 017XXXXXXXX)" required>
        </div>
        <div style="display: flex; gap: 8px;">
          <button type="submit" class="btn btn-primary" style="padding: 6px 12px; font-size: 12px;">সংরক্ষণ</button>
          <button type="button" onclick="document.getElementById('addContactForm').style.display='none'" class="btn btn-outline" style="padding: 6px 12px; font-size: 12px;">বাতিল</button>
        </div>
      </form>
    </div>

    <!-- Saved Device Contacts List -->
    <div style="display: flex; flex-direction: column; gap: 10px;">
      <?php if (empty($savedContacts)): ?>
        <div style="text-align: center; padding: 25px; color: var(--text-muted); font-size: 13px;">
          কোনো সংরক্ষিত ফোন কন্টাক্ট নেই। "ফোন থেকে আনুন" বাটনে ক্লিক করে ফোনের নম্বর আমদানি করুন।
        </div>
      <?php else: ?>
        <?php foreach ($savedContacts as $sc): ?>
          <div class="contact-card">
            <div class="contact-avatar" style="background: rgba(255,255,255,0.08);"><i class="fas fa-user"></i></div>
            <div class="contact-info">
              <div class="contact-name"><?php echo htmlspecialchars($sc['name']); ?></div>
              <div class="contact-num"><?php echo htmlspecialchars($sc['number']); ?></div>
            </div>
            <div class="contact-actions">
              <a href="call-active.php?callee=<?php echo urlencode($sc['number']); ?>&type=AUDIO" class="btn-action btn-audio">
                <i class="fas fa-phone"></i>
              </a>
              <a href="dialer.php?number=<?php echo urlencode($sc['number']); ?>" class="btn-action" style="background: rgba(77, 182, 172, 0.2); color: var(--primary-light);">
                <i class="fas fa-th"></i>
              </a>
            </div>
          </div>
        <?php endforeach; ?>
      <?php endif; ?>
    </div>

  </div>

</div>

<script>
function switchTab(tab) {
  const ipBtn = document.getElementById('tabIpBtn');
  const phoneBtn = document.getElementById('tabPhoneBtn');
  const ipContent = document.getElementById('tabIpContent');
  const phoneContent = document.getElementById('tabPhoneContent');

  if (tab === 'ip') {
    ipBtn.style.background = 'var(--primary)';
    ipBtn.style.color = '#fff';
    ipBtn.style.fontWeight = '700';

    phoneBtn.style.background = 'rgba(255,255,255,0.08)';
    phoneBtn.style.color = 'var(--text-muted)';
    phoneBtn.style.fontWeight = 'normal';

    ipContent.style.display = 'block';
    phoneContent.style.display = 'none';
  } else {
    phoneBtn.style.background = 'var(--primary)';
    phoneBtn.style.color = '#fff';
    phoneBtn.style.fontWeight = '700';

    ipBtn.style.background = 'rgba(255,255,255,0.08)';
    ipBtn.style.color = 'var(--text-muted)';
    ipBtn.style.fontWeight = 'normal';

    phoneContent.style.display = 'block';
    ipContent.style.display = 'none';
  }
}

// Device Contact Picker API
async function importFromDevice() {
  if ('contacts' in navigator && 'ContactsManager' in window) {
    try {
      const props = ['name', 'tel'];
      const contacts = await navigator.contacts.select(props, { multiple: true });
      if (contacts && contacts.length > 0) {
        const payload = contacts.map(c => ({
          name: (c.name && c.name[0]) ? c.name[0] : 'Contact',
          number: (c.tel && c.tel[0]) ? c.tel[0].replace(/[^0-9+]/g, '') : ''
        })).filter(c => c.number.length > 0);

        await fetch('contacts.php', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        location.reload();
      }
    } catch (err) {
      console.warn('Device contacts picker canceled/failed:', err);
    }
  } else {
    document.getElementById('addContactForm').style.display = 'block';
    alert('আপনার ডিভাইসে সরাসরি কন্টাক্ট সিলেক্ট বাটন উন্মুক্ত না হলে নিচের ফর্ম দিয়ে নাম ও নম্বর সংরক্ষণ করুন।');
  }
}
</script>

<?php include __DIR__ . '/includes/footer.php'; ?>
