package mrtsk.by.helpme.callbacks

import mrtsk.by.helpme.models.Meeting
import mrtsk.by.helpme.models.Post
import mrtsk.by.helpme.responses.DownloadAllPostsResponse

interface DownloadPostsCallback {
    fun finish(meetings: List<Meeting>?)
}
