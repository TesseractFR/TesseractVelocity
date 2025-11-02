package onl.tesseract.tesseractVelocity.utils

object IpUtil {
    private val IPv4_REGEX = Regex("^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$")
    fun isValidIPv4(input: String): Boolean = IPv4_REGEX.matches(input)
}
