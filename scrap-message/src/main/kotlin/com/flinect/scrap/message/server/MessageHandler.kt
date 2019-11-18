package com.flinect.scrap.message.server

import com.flinect.scrap.message.Message

/**
 * Generic message handler.
 */
typealias MessageHandler<T> = (message: T, headers: HeaderMap) -> Message
