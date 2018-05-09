package mrtsk.by.helpme.responses

import mrtsk.by.helpme.models.SimpleMeeting
import java.util.*
import kotlin.collections.ArrayList

data class SimpleMeetingResponse(private val error: String, private val simpleMeeting: Array<SimpleMeeting>?) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SimpleMeetingResponse

        if (error != other.error) return false
        if (!Arrays.equals(simpleMeeting, other.simpleMeeting)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = error.hashCode()
        result = 31 * result + Arrays.hashCode(simpleMeeting)
        return result
    }

    fun getError(): String {
        return error
    }

    fun getMeetings() : Array<SimpleMeeting>? {
        return simpleMeeting
    }
}
