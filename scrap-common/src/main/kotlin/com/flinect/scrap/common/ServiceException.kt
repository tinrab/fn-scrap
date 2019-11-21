package com.flinect.scrap.common

/**
 * Represents a general service exception. It is used to communicate exception between services.
 */
open class ServiceException(
    val code: String,
    val kind: Kind = Kind.BAD_REQUEST,
    override val message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    constructor(code: String, cause: Throwable?) : this(code, message = null, cause = cause)

    val httpStatus = when (kind) {
        ServiceException.Kind.INTERNAL -> 500
        ServiceException.Kind.BAD_REQUEST -> 400
    }

    enum class Kind {
        INTERNAL,
        BAD_REQUEST
    }
}
