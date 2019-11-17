package com.flinect.scrap.message

/**
 * Mark a field to be serialized inside meta object.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class MessageMeta
