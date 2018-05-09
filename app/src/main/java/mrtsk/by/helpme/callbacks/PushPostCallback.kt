package mrtsk.by.helpme.callbacks

import mrtsk.by.helpme.models.Meeting
import mrtsk.by.helpme.responses.SimpleResponse

interface PushPostCallback {
    fun finish(meeting: Meeting)
}