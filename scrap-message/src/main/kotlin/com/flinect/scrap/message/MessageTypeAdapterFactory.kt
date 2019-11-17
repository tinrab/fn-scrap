package com.flinect.scrap.message

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.internal.Streams
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.util.LinkedHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

internal class MessageTypeAdapterFactory<T : Any> constructor(
    private val baseType: KClass<*>,
    private val typeFieldName: String
) : TypeAdapterFactory {
    private val labelToType = LinkedHashMap<String, KClass<*>>()
    private val typeToLabel = LinkedHashMap<KClass<*>, String>()

    fun registerMessageType(clazz: KClass<*>): MessageTypeAdapterFactory<T> {
        val typeName = clazz.annotations.filterIsInstance<MessageTypeName>()
            .lastOrNull()
        requireNotNull(typeName) {
            "MessageTypeName annotation is required on all message types."
        }
        val label = typeName.value
        require(!(typeToLabel.containsKey(clazz) || labelToType.containsKey(label))) {
            "Types and labels must be unique."
        }
        labelToType[label] = clazz
        typeToLabel[clazz] = label

        // Check if field names are unique
        var fieldNames = clazz.declaredMemberProperties
            .map {
                it.annotations.filterIsInstance<MessageFieldName>()
                    .map { messageFieldName -> messageFieldName.value }
                    .lastOrNull() ?: it.name
            }
        val distinctFieldNames = fieldNames.distinct()
        require(fieldNames.size == distinctFieldNames.size) {
            distinctFieldNames.forEach {
                fieldNames = fieldNames.minus(it)
            }
            "Detected duplicate field names: $fieldNames."
        }

        return this
    }

    fun <S : T> isRegistered(value: S): Boolean {
        return typeToLabel.containsKey(value::class)
    }

    override fun <R : Any> create(gson: Gson, type: TypeToken<R>): TypeAdapter<R>? {
        if (type.rawType != baseType.java) {
            return null
        }

        val labelToDelegate = LinkedHashMap<String, TypeAdapter<*>>()
        val typeToDelegate = LinkedHashMap<KClass<*>, TypeAdapter<*>>()
        for (entry in labelToType.entries) {
            val delegate = gson.getDelegateAdapter(this, TypeToken.get(entry.value.java))
            labelToDelegate[entry.key] = delegate
            typeToDelegate[entry.value] = delegate
        }

        return object : TypeAdapter<R>() {
            @Throws(IOException::class)
            override fun read(reader: JsonReader): R {
                val jsonElement = Streams.parse(reader)
                require(jsonElement.isJsonObject) {
                    "Cannot deserialize a non-object type."
                }
                val jsonObject = jsonElement.asJsonObject

                // Find message type
                val label = getLabel(jsonObject)
                val messageType = labelToType[label]
                requireNotNull(messageType) { "Unknown message type '$label'." }
                require(labelToDelegate.containsKey(label)) { "Unknown message type '$label'." }
                @Suppress("UNCHECKED_CAST")
                val delegate = labelToDelegate[label] as TypeAdapter<R>

                requireNotNull(jsonObject.get(PAYLOAD_GROUP)) { "Missing message payload." }

                // Construct
                val fieldDescriptions = getFieldDescriptions(messageType)
                val flatObject = JsonObject()

                for (group in GROUPS) {
                    require(jsonObject.get(group).isJsonObject) {
                        "Nested '$group' field must be an object."
                    }

                    val groupObject = jsonObject.get(group).asJsonObject

                    for (field in groupObject.entrySet()) {
                        val fieldName = fieldDescriptions.find {
                            it.group == group && it.targetFieldName == field.key
                        }?.sourceFieldName
                        if (fieldName != null) {
                            flatObject.add(fieldName, field.value)
                        }
                    }
                }

                return delegate.fromJsonTree(flatObject)
            }

            @Throws(IOException::class)
            override fun write(out: JsonWriter, value: R) {
                val valueClass = value::class
                requireNotNull(typeToDelegate[valueClass]) { "Message type was not registered." }

                @Suppress("UNCHECKED_CAST")
                val delegate = typeToDelegate[valueClass] as TypeAdapter<R>
                val jsonObject = delegate.toJsonTree(value).asJsonObject

                // Validate json object
                require(!jsonObject.has(typeFieldName)) {
                    "Cannot serialize '$baseType' because it already defines a field name '$typeFieldName'."
                }
                GROUPS.forEach {
                    require(!jsonObject.has(it)) {
                        "Cannot serialize '$baseType' because it already defines a field name '$it'."
                    }
                }

                // Construct a result
                val result = JsonObject()
                result.add(typeFieldName, JsonPrimitive(typeToLabel[valueClass]))

                val groups = HashMap<String, JsonObject>()
                val fieldDescriptions = getFieldDescriptions(valueClass)
                for (fd in fieldDescriptions) {
                    if (!jsonObject.has(fd.sourceFieldName)) {
                        continue
                    }
                    if (!groups.containsKey(fd.group)) {
                        groups[fd.group] = JsonObject()
                    }
                    groups[fd.group]?.add(fd.targetFieldName, jsonObject.get(fd.sourceFieldName))
                }
                for (group in groups) {
                    if (group.value.size() != 0) {
                        result.add(group.key, group.value)
                    }
                }

                Streams.write(result, out)
            }
        }.nullSafe()
    }

    private fun getLabel(jsonObject: JsonObject): String {
        val labelJsonElement = jsonObject.get(typeFieldName)
        requireNotNull(labelJsonElement) {
            "Cannot deserialize '$baseType' because it does not define a field name '$typeFieldName'."
        }
        return labelJsonElement.asString
    }

    private data class FieldDescription(
        val sourceFieldName: String,
        val targetFieldName: String,
        val group: String
    )

    private fun <R : Any> getFieldDescriptions(clazz: KClass<R>): List<FieldDescription> {
        val fieldDescriptions = arrayListOf<FieldDescription>()

        for (property in clazz.declaredMemberProperties) {
            // Remove skipped fields
            if (property.annotations.find { it is MessageFieldSkip } != null) {
                continue
            }
            // Extract field name
            val fieldName = property.annotations
                .filterIsInstance<MessageFieldName>()
                .map { it.value }
                .lastOrNull() ?: property.name
            // Define a group for this field
            val group = property.annotations
                .filterIsInstance<MessageMeta>()
                .map { META_GROUP }
                .firstOrNull() ?: PAYLOAD_GROUP

            fieldDescriptions.add(FieldDescription(property.name, fieldName, group))
        }

        return fieldDescriptions
    }

    companion object {
        private const val PAYLOAD_GROUP = "payload"
        private const val META_GROUP = "meta"
        private val GROUPS = arrayOf(PAYLOAD_GROUP, META_GROUP)
    }
}
