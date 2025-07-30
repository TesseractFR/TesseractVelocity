package onl.tesseract.tesseractVelocity.domain.admin

import java.time.Instant

data class Ban(
    var id: Int? = null,

    var uuid: String? = null,

    var ip: String? = null,

    var staff: String? = null,

    var reason: String? = null,

    var server: String? = null,

    var begin: Instant? = null,

    var end: Instant? = null,

    var state: Boolean? = false,

    var unbandate: Instant? = null,

    var unbanstaff: String? = null,

    var unbanreason: String? = null,
){
    fun isActive() : Boolean {
        val now = Instant.now()
        return state == true && (end == null || now.isBefore(end))
    }
}
