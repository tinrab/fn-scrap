package com.flinect.scrap.message

/**
 * Can be used to ignore a field during serialization and deserialization.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class MessageFieldIgnore
