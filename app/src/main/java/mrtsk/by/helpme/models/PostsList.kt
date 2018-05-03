package mrtsk.by.helpme.models

class PostsList(private var posts: ArrayList<Post>) {

    fun addUsers(newPosts: ArrayList<Post>) {
        posts.addAll(newPosts)
    }

    fun addPost(newPost: Post) {
        posts.add(0, newPost)
    }

    fun getAllPosts() : ArrayList<Post> {
        return posts
    }

    fun size() : Int {
        return posts.size
    }

    fun getPost(index: Int) : Post {
        return posts[index]
    }
}

