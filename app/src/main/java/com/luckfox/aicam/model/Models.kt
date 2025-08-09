package com.luckfox.aicam.model

data class CamConfig(val rtsp:String, val res:String, val fps:Int, val threshold:Double, val cooldown:Int)
data class Event(val id:String, val ts:Long, val clip:String, val snapshot:String, val score:Double)
data class Alerts(val events: List<Event>)
