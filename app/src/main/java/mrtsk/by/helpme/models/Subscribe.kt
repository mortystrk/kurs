package mrtsk.by.helpme.models

data class Subscribe(val _id: String, var user: User, var meetings: ArrayList<Meeting>?)
