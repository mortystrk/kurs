package mrtsk.by.helpme.callbacks

import mrtsk.by.helpme.models.Meeting
import mrtsk.by.helpme.responses.MeetingDetailResponse

interface MeetingDetailCallback {
    fun finish(meetingDetailResponse: MeetingDetailResponse)
}
