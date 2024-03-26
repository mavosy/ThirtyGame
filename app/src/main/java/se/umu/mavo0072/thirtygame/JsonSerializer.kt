package se.umu.mavo0072.thirtygame

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Provides JSON serialization and deserialization utilities using Gson.
 */
object JsonSerializer {
    private val gson = Gson()

    /**
     * Serializes an object to its JSON representation.
     * @param T The type of the data to serialize.
     * @param data The object to serialize.
     * @return The JSON string representation of the data.
     */
    internal inline fun <reified T> serialize(data: T): String {
        return gson.toJson(data)
    }

    /**
     * Deserializes a JSON string to an object of the specified type.
     * @param T The type to which the JSON will be deserialized.
     * @param json The JSON string to deserialize.
     * @return The deserialized object of type T.
     */
    internal inline fun <reified T> deserialize(json: String): T {
        return gson.fromJson(json, object: TypeToken<T>() {}.type)
    }
}