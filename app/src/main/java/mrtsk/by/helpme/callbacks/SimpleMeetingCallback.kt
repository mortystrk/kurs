package mrtsk.by.helpme.callbacks

import mrtsk.by.helpme.responses.SimpleMeetingResponse

interface SimpleMeetingCallback {
    fun finish(simpleMeetingResponse: SimpleMeetingResponse)
}