package schoolManagement.app.helper

import schoolManagement.app.Config.Config
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

// Singleton object to manage application configuration and logging setup
object AppContext {

    // Default environment if not specified in configuration
    private const val ENVIRONMENT = "test"

    // Config instance to hold loaded configuration settings; `lateinit` means it will be initialized later
    private lateinit var config: Config

    // Flag to check if logging is enabled
    private var loggerEnabled: Boolean = false

    // Logger for logging information or errors
    private val logger: Logger = Logger.getLogger(AppContext::class.java.name)

    // Checks if logging is enabled based on configuration
    fun isLoggerEnabled(): Boolean {
        return loggerEnabled
    }

    // Retrieves a specific property value from the configuration
    fun getProp(prop: String): String? {
        return config.getString(prop)  // Fetches the property value directly; assumes config is initialized
    }

    /**
     * Initializes the application context by loading configuration files and setting up logging if enabled.
     * @param arguments Array of strings containing paths to configuration files
     * @throws RuntimeException if no configuration files are provided or if loading fails
     */
    @Throws(RuntimeException::class)
    fun init(arguments: Array<String>) {

        // Initializes the config object
        config = Config()

        // Ensures at least one configuration file is provided; otherwise throws an error
        if (arguments.isEmpty()) {
            throw RuntimeException("Configuration file is not provided")
        }

        // List to collect any errors that occur during file loading
        val loadingErrors = mutableListOf<String>()

        // Loads each configuration file in the arguments array
        arguments.forEach {
            arg ->
            try {
                config.load(arg)  // Loads the configuration file specified by 'arg'
                logger.log(Level.INFO, "Loaded configuration from: $arg")
            } catch (e: IOException) {
                // Adds any load errors to the list and logs a warning
                loadingErrors.add("Failed to load configuration from $arg: ${e.message}")
                logger.log(Level.WARNING, "Some exception while loading property file: ${e.message}")
            }
        }
    }

}
