package mrtsk.by.helpme.callbacks

import mrtsk.by.helpme.responses.SimpleResponse

interface SimpleCallback {
    fun finish(simpleResponse: SimpleResponse)
}
