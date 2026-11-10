package ca.pebble.watchpatrol.data.remote.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * Reads a Double whether the API sends it as a JSON number (1234.5) or a
 * JSON string ("1234.50"). Django REST Framework serializes DecimalField as a
 * string by default, so this keeps prices robust either way.
 */
object FlexibleDoubleSerializer : KSerializer<Double?> {
    override val descriptor =
        PrimitiveSerialDescriptor("FlexibleDouble", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Double? {
        val jsonDecoder = decoder as? JsonDecoder ?: return runCatching { decoder.decodeDouble() }.getOrNull()
        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonNull) return null
        val primitive = element as? JsonPrimitive ?: return null
        return primitive.content.toDoubleOrNull()
    }

    override fun serialize(encoder: Encoder, value: Double?) {
        if (value == null) encoder.encodeString("") else encoder.encodeDouble(value)
    }
}
