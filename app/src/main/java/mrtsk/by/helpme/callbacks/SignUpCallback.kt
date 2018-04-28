package mrtsk.by.helpme.callbacks

import mrtsk.by.helpme.responses.SignUpResponse

interface SignUpCallback {
    fun finish(signUpResponse: SignUpResponse?)
}