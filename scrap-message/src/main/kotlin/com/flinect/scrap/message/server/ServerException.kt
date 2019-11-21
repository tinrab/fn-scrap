package com.flinect.scrap.message.server

import com.flinect.scrap.common.ServiceException

class ServerException private constructor(
    code: String,
    kind: Kind,
    message: String,
    cause: Throwable? = null
) : ServiceException(code, kind, message, cause) {
    companion object {
        private const val CODE = "message.server"

        fun badRequest(message: String, cause: Throwable? = null): ServerException {
            return ServerException(CODE, Kind.BAD_REQUEST, message, cause)
        }

        fun internal(message: String, cause: Throwable? = null): ServerException {
            return ServerException(CODE, Kind.INTERNAL, message, cause)
        }
    }
}
