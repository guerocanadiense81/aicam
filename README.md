# AiCam – Android APK (No Android Studio)

This repo builds the **AiCam** Android app that discovers a **Luckfox Pico‑M** AI camera on your LAN, streams RTSP, and shows alert clips. Built via **GitHub Actions** – no Android Studio required.

## 1) Repo Setup (Windows-friendly)
1. Download this project as a ZIP and extract to `C:\AiCam`.
2. Open **GitHub Desktop** → **Add Local Repository** → `C:\AiCam` → Commit & Push.

## 2) Build the APK in GitHub Actions
1. Go to **Actions** tab → Run **Build APK**.
2. Download artifact **AiCam-debug-apk** → extract `app-debug.apk`.
3. Install APK on your Android phone.

## 3) First Launch & Auto-Discovery
- App scans **192.168.1.0/24** first (your network), then the phone’s /24.
- It verifies `http://IP:8080/health` and sets RTSP to `rtsp://IP/live/main`.
- If not found, go to **Settings** and enter IPs manually.

## 4) Luckfox Pico‑M Setup
See `/luckfox/README.md` inside this repo for the camera-side steps.
