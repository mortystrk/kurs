package mrtsk.by.helpme.models

data class Meeting(val _id: String, val name: String, val description: String,
                   var creator: User?)
