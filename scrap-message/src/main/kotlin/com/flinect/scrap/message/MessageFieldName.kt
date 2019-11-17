package com.flinect.scrap.message

/**
 * Sets a custom field name.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class MessageFieldName(val value: String)
