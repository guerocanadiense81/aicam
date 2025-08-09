# Luckfox Pico-M Setup (Edge AI Camera)
1) Flash the official Pico-M RV1103 image to microSD (use Etcher or dd).
2) Boot with Ethernet attached, find IP (e.g., `192.168.1.x`).
3) SSH: `ssh root@<CAM_IP>` → change password.
4) Install deps:
   ```bash
   apt update && apt install -y python3-pip ffmpeg v4l-utils
   pip3 install fastapi==0.111.0 uvicorn==0.30.1 pydantic==2.7.1 opencv-python==4.9.0.80 rknnlite==1.6.0
   ```
5) Enable RTSP:
   ```bash
   systemctl enable rkipc && systemctl start rkipc
   # Stream: rtsp://<CAM_IP>/live/main
   ```
6) Copy files in this folder to `/opt/cam/` and enable services:
   ```bash
   mkdir -p /opt/cam/{models,clips,logs}
   cp ai_detect.py api.py yolo_post.py /opt/cam/
   cp config.json /opt/cam/config.json
   cp cam-api.service /etc/systemd/system/
   cp ai-detect.service /etc/systemd/system/
   systemctl daemon-reload
   systemctl enable --now cam-api ai-detect
   ```
