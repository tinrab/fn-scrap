package com.flinect.scrap.message

import kotlin.reflect.KClass

/**
 * Defines an array of all sub messages.
 */
@Target(AnnotationTarget.CLASS)
annotation class MessageTypes(vararg val types: KClass<*>)
