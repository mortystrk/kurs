package mrtsk.by.helpme.responses

import mrtsk.by.helpme.models.Meeting

data class MeetingDetailResponse(val error: String, val meeting: Meeting?)
