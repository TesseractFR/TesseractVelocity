package onl.tesseract.tesseractVelocity.utils

import java.util.UUID

object UUIDUtil {
    @Throws(IllegalArgumentException::class)
    fun fromStringOrFix(uuid: String): UUID {
        return try {
            UUID.fromString(uuid) // Si déjà formaté.
        } catch (e: IllegalArgumentException) {
            // Ajout automatique des tirets sur les UUID non formatés
            if (uuid.length == 32) {
                UUID.fromString(
                    "${uuid.substring(0, 8)}-${uuid.substring(8, 12)}-${uuid.substring(12, 16)}-${uuid.substring(16, 20)}-${uuid.substring(20)}"
                )
            } else throw e // Exception pour non-conformité à une structure UUID
        }
    }

    fun uuidToString(uuid: UUID): String {
        return uuid.toString().replace("-", "")
    }
}
