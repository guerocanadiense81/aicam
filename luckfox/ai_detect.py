#!/usr/bin/env python3
import os, time, json, subprocess, cv2, threading
from rknnlite.api import RKNNLite
from http.client import HTTPConnection
from yolo_post import postprocess_yolov5

CFG = "/opt/cam/config.json"
CLIP_DIR = "/opt/cam/clips"
MODEL = "/opt/cam/models/yolov5n_person_int8.rknn"
EVENTS = "/opt/cam/events.json"
LOG = "/opt/cam/logs/err.log"

def log(s):
    try:
        with open(LOG, "a") as f: f.write(str(s)+"\n")
    except: pass

def load_cfg():
    with open(CFG) as f: return json.load(f)

def append_event(ev):
    try:
        if not os.path.exists(EVENTS): open(EVENTS, "w").write("[]")
        data = json.loads(open(EVENTS).read())
        data.append(ev); data = data[-500:]
        open(EVENTS, "w").write(json.dumps(data))
    except Exception as e:
        log(e)

def ping_sse():
    try:
        c = HTTPConnection("127.0.0.1", 8080, timeout=1)
        c.request("GET", "/tick"); c.getresponse().read()
    except Exception as e:
        log(e)

def record_clip(rtsp, path, dur=10):
    subprocess.run([
      "ffmpeg","-rtsp_transport","tcp","-y","-i",rtsp,"-t",str(dur),"-c","copy",path
    ], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

def take_snapshot(rtsp, path):
    subprocess.run([
      "ffmpeg","-rtsp_transport","tcp","-y","-i",rtsp,"-frames:v","1","-q:v","2",path
    ], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

def parse_person(outputs, thr=0.45):
    try:
        dets = outputs[0]
        if hasattr(dets, 'ndim') and dets.ndim == 2 and dets.shape[-1] >= 6:
            return any(float(d[4]) >= thr and int(d[5]) == 0 for d in dets)
    except Exception:
        pass
    dets = postprocess_yolov5(outputs, img_size=320, conf_thres=thr, iou_thres=0.45, num_classes=80, person_cls=0)
    return dets.shape[0] > 0

def run():
    cfg = load_cfg()
    rtsp = cfg["rtsp"]
    thr = float(cfg["threshold"])
    cool = int(cfg["cooldown"])
    os.makedirs(CLIP_DIR, exist_ok=True)

    rknn = RKNNLite(); rknn.load_rknn(MODEL); rknn.init_runtime()
    cap = cv2.VideoCapture(rtsp)
    next_ok = 0
    while True:
        ok, frame = cap.read()
        if not ok:
            time.sleep(0.2); cap.release(); cap = cv2.VideoCapture(rtsp); continue
        img = cv2.resize(frame, (320,320))
        outs = rknn.inference([img])
        if parse_person(outs, thr) and time.time() >= next_ok:
            ts = int(time.time()); base = f"{ts}"
            mp4 = f"{CLIP_DIR}/{base}.mp4"; jpg = f"{CLIP_DIR}/{base}.jpg"
            threading.Thread(target=record_clip, args=(rtsp, mp4, 10), daemon=True).start()
            threading.Thread(target=take_snapshot, args=(rtsp, jpg), daemon=True).start()
            event = {"id": base, "ts": ts, "score": 0.99, "clip": f"/clips/{base}.mp4", "snapshot": f"/clips/{base}.jpg"}
            append_event(event); ping_sse(); next_ok = time.time() + cool

if __name__ == "__main__":
    run()
