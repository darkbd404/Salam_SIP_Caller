# 🚀 সালাম এসআইপি কলার (Salam SIP Caller) - GitHub APK Build & Deployment Guide

এই রিপোজিটরিতে **GitHub Actions CI/CD** কনফিগারেশন যুক্ত করা হয়েছে, যার মাধ্যমে কোনো লোকাল পিসিতে Android Studio ছাড়াই সরাসরি **GitHub থেকে ১-ক্লিকে অ্যান্ড্রয়েড APK ফাইল (.apk)** তৈরি ও ডাউনলোড করা যাবে।

---

## 📱 ১. GitHub Actions দিয়ে APK বিল্ড করার নিয়ম:

### ধাপ ১: গিটহাবে পুশ (Push) করুন
আপনি যখনই এই রিপোজিটরিটি GitHub-এ আপলোড বা `git push` করবেন, স্বয়ংক্রিয়ভাবে **GitHub Actions Workflow** চালু হয়ে যাবে।

### ধাপ ২: Actions ট্যাবে যান
1. আপনার GitHub রিপোজিটরিতে প্রবেশ করুন।
2. উপরের মেনু থেকে **"Actions"** ট্যাবে ক্লিক করুন।
3. বাঁপাশের লিস্ট থেকে **"Build Salam SIP Caller Android APK"** দেখতে পাবেন।
4. রানিং বা কমপ্লিট হওয়া বিল্ডটিতে ক্লিক করুন।

### ধাপ ৩: তৈরি হওয়া APK ডাউনলোড করুন
1. বিল্ড সফলভাবে শেষ হলে পেজের নিচে **"Artifacts"** সেকশন দেখতে পাবেন।
2. **"Salam_SIP_Caller_APK"** লিংকে ক্লিক করলেই `Salam_SIP_Caller_v2.6_debug.apk` ফাইলটি ডাউনলোড হয়ে যাবে।
3. সরাসরি ফোনে ইনস্টল করে ব্যবহার করতে পারবেন।

---

## 🌐 ২. cPanel / Web Hosting-এ PHP PWA হোস্ট করার নিয়ম:

1. `/salam_call_php/` ফোল্ডারের ভেতরের সব ফাইল ও ফোল্ডার জিপ (ZIP) করুন।
2. আপনার cPanel-এর **File Manager** ওপেন করে `public_html`-এ আপলোড ও Extract করুন।
3. ডোমেইন বা লিংকে ব্রাউজ করলেই PWA ডায়ালার ও কলিং সিস্টেম লাইভ হয়ে যাবে।

---

## 📂 প্রোজেক্ট ফোল্ডার স্ট্রাকচার:

```text
├── .github/workflows/
│   └── build-apk.yml        # গিটহাব থেকে সরাসরি APK বিল্ড করার অটোমেশন ফাইল
├── app/                     # Android Native Jetpack Compose সোর্স কোড
├── salam_call_php/          # cPanel / Hosting Ready 100% Pure PHP + PWA প্যাকেজ
├── build.gradle.kts         # অ্যান্ড্রয়েড রুট গ্রেডল বিল্ড কনফিগারেশন
├── settings.gradle.kts      # গ্রেডল সেটিংস
├── gradlew & gradlew.bat    # গ্রেডল র‍্যাপার স্ক্রিপ্ট
└── metadata.json            # প্ল্যাটফর্ম মেটাডাটা
```
