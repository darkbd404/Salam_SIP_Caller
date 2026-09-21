  </main> <!-- /app-content -->

  <!-- Bottom Navigation Bar -->
  <nav class="bottom-nav">
    <a href="index.php" class="nav-item <?php echo basename($_SERVER['PHP_SELF']) == 'index.php' ? 'active' : ''; ?>">
      <i class="fas fa-house"></i>
      <span>হোম</span>
    </a>
    <a href="dialer.php" class="nav-item <?php echo basename($_SERVER['PHP_SELF']) == 'dialer.php' ? 'active' : ''; ?>">
      <i class="fas fa-calculator"></i>
      <span>ডায়াল</span>
    </a>
    <a href="messages.php" class="nav-item <?php echo in_array(basename($_SERVER['PHP_SELF']), ['messages.php', 'chat.php']) ? 'active' : ''; ?>">
      <i class="fas fa-comment-dots"></i>
      <span>মেসেজ</span>
    </a>
    <a href="contacts.php" class="nav-item <?php echo basename($_SERVER['PHP_SELF']) == 'contacts.php' ? 'active' : ''; ?>">
      <i class="fas fa-address-book"></i>
      <span>কন্টাক্টস</span>
    </a>
    <a href="history.php" class="nav-item <?php echo basename($_SERVER['PHP_SELF']) == 'history.php' ? 'active' : ''; ?>">
      <i class="fas fa-clock-rotate-left"></i>
      <span>হিস্ট্রি</span>
    </a>
    <a href="profile.php" class="nav-item <?php echo basename($_SERVER['PHP_SELF']) == 'profile.php' ? 'active' : ''; ?>">
      <i class="fas fa-user"></i>
      <span>প্রোফাইল</span>
    </a>
  </nav>

</div> <!-- /app-container -->

<script src="assets/js/app.js?v=2.6"></script>
<script src="assets/js/dialer.js?v=2.6"></script>

<?php if (isset($currentUser['ipNumber'])): ?>
<script>
document.addEventListener('DOMContentLoaded', () => {
  initIncomingCallListener("<?php echo addslashes($currentUser['ipNumber']); ?>");
});
</script>
<?php endif; ?>

</body>
</html>
