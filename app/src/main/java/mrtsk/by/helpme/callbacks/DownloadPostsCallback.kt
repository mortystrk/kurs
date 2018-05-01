package mrtsk.by.helpme.callbacks

import mrtsk.by.helpme.models.Post
import mrtsk.by.helpme.responses.DownloadAllPostsResponse

interface DownloadPostsCallback {
    fun finish(posts: List<Post>?)
}
