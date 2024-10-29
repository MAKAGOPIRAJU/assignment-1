package schoolManagement.app.Config

import org.w3c.dom.Document
import javax.xml.parsers.DocumentBuilderFactory
import java.io.File
import java.io.IOException

class Config {

    // A mutable map to hold the key-value pairs from the configuration
    private val properties = mutableMapOf<String, String>()

    // Method to load properties from an XML file
    fun load(filePath: String) {
        try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val document: Document = builder.parse(File(filePath))

            val entries = document.getElementsByTagName("entry")
            for (i in 0 until entries.length) {
                val entry = entries.item(i)
                if (entry.nodeType == org.w3c.dom.Node.ELEMENT_NODE) {
                    val key = entry.attributes.getNamedItem("key").nodeValue
                    val value = entry.textContent
                    properties[key] = value
                }
            }
        } catch (e: Exception) {
            throw IOException("Error loading configuration file: ${e.message}", e)
        }
    }

    // Method to get a String property
    fun getString(key: String): String? {
        return properties[key]
    }

    // Method to get a Boolean property
    fun getBoolean(key: String): Boolean {
        return properties[key]?.toBoolean() ?: false
    }

    // Method to put a property programmatically
    fun put(key: String, value: String) {
        properties[key] = value
    }
}