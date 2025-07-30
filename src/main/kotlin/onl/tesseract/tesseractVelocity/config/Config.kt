package onl.tesseract.tesseractVelocity.config

import java.io.File
import com.moandjiezana.toml.Toml;

object Config  {
    lateinit var dbAdmin: DatabaseConfig

    fun load() {
        val configFile = File("plugins/tesseract", "config.toml")

        check(configFile.exists())

        val toml = Toml().read(configFile)

        dbAdmin = DatabaseConfig(
            url = toml.getString("database_url")?: error("database url is missing"),
            user = toml.getString("database_user")?: error("user is missing"),
            password = toml.getString("database_password")?: error("password is missing"),
            dialect = toml.getString("database_dialect")?: error("dialect is missing"),
            driver = toml.getString("database_driver")?: error("driver is missing")
        )
    }
}


data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
    val dialect: String,
    val driver: String,
)