package mrtsk.by.helpme.callbacks

import mrtsk.by.helpme.models.User

interface UserDataCallback {
    fun finish(user: User?)
}
