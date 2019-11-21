package com.flinect.scrap.message

/**
 * Generic message listener.
 * Called on new [Message]. Returns whether processing was successful.
 */
typealias MessageListener = (message: Message) -> Boolean
