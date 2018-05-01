package mrtsk.by.helpme.models

data class Post(var postId: Int?, var userAvatar: String, var userName: String, var userId: String,
                var postRating: Float, var userRating: Float, var postDescription: String, var postText: String)