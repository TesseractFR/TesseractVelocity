package onl.tesseract.tesseractVelocity.utils

import java.time.Duration

object TimeParser {
    fun parse(input: String): Duration? {
        val regex = Regex("(\\d+)([a-zA-Z]+)")
        val match = regex.matchEntire(input) ?: return null

        val (value, unit) = match.destructured
        val number = value.toLongOrNull() ?: return null

        return when (unit.lowercase()) {
            "d" -> Duration.ofDays(number)
            "m" -> Duration.ofDays(30 * number)
            "h" -> Duration.ofHours(number)
            "min" -> Duration.ofMinutes(number)
            "s" -> Duration.ofSeconds(number)
            else -> null
        }
    }
}
