package com.flinect.scrap.message

/**
 * Do not serialize this field.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class MessageFieldSkip
