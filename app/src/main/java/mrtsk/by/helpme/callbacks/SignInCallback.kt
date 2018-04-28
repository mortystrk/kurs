package mrtsk.by.helpme.callbacks

import mrtsk.by.helpme.responses.SignInResponse

interface SignInCallback {
    fun finish(signInResponse: SignInResponse?)
}
