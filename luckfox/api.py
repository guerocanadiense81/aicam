#!/usr/bin/env python3
import os, json, time
from fastapi import FastAPI, Response
from fastapi.responses import FileResponse, StreamingResponse
from pydantic import BaseModel
from threading import Event

BASE = "/opt/cam"
CFG = f"{BASE}/config.json"
CLIPS = f"{BASE}/clips"
EVENTS_FILE = f"{BASE}/events.json"
tick_event = Event()

app = FastAPI()

class Config(BaseModel):
    rtsp: str
    res: str
    fps: int
    threshold: float
    cooldown: int

def read_events():
    if not os.path.exists(EVENTS_FILE): return []
    try:
        return json.loads(open(EVENTS_FILE).read())
    except:
        return []

@app.get("/health")
def health(): return {"ok": True, "time": int(time.time())}

@app.get("/config")
def get_cfg(): return json.loads(open(CFG).read())

@app.post("/config")
def set_cfg(c: Config):
    open(CFG, "w").write(c.model_dump_json())
    return {"ok": True}

@app.get("/alerts")
def alerts(after: int = 0):
    evs = [e for e in read_events() if e.get("ts",0) > after]
    return {"events": evs}

@app.get("/events")
def sse():
    def gen():
        yield "retry: 10000\n\n"
        while True:
            from time import sleep
            tick_event.wait(timeout=15)
            tick_event.clear()
            yield f"data: tick {int(time.time())}\n\n"
            sleep(0.1)
    return StreamingResponse(gen(), media_type="text/event-stream")

@app.get("/tick")
def tick():
    tick_event.set(); return {"ok": True}

@app.get("/clips/{name}")
def clips(name: str):
    path = os.path.join(CLIPS, name)
    if not os.path.isfile(path): return Response(status_code=404)
    return FileResponse(path)
