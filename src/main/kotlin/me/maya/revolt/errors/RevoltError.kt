package me.maya.revolt.errors

sealed class RevoltError(msg: String?, cause: Throwable?): Throwable(msg, cause) {
    sealed class Api(val code: Int, val reason: String, cause: Throwable?): RevoltError("$code $reason", cause) {
        class BadRequest(cause: Throwable? = null): Api(400, "Bad Request", cause)
        class Unauthorized(cause: Throwable? = null): Api(401, "Unauthorized", cause)
        class Forbidden(cause: Throwable? = null): Api(403, "Forbidden", cause)
        class NotFound(cause: Throwable? = null): Api(404, "Not Found", cause)

        class InternalError(code: Int, cause: Throwable? = null): Api(code, "Internal Exception Occured", cause)
    }

    sealed class Gateway(msg: String?, cause: Throwable?): RevoltError(msg, cause) {
        class LabelMe(cause: Throwable? = null): Gateway("Unknown error occured", cause)
        class InternalError(cause: Throwable? = null): Gateway("An internal error occured", cause)
        class InvalidSession(cause: Throwable? = null): Gateway("Provided authentication token is invalid", cause)
        class OnboardingNotFinished(cause: Throwable? = null): Gateway("User has not chosen a username", cause)
        class AlreadyAuthenticated(cause: Throwable? = null): Gateway("This connection is already authenticated", cause)

        class UnknownGatewayError(msg: String?, cause: Throwable? = null): Gateway(msg, cause)
    }
}