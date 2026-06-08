package com.hanshow.mapExample.data.model.auth

/**
 * Allstar platform error code mapping
 * Maps resultCode to user-friendly error descriptions
 */
enum class ErrorCode(val code: String, val description: String) {
    // Login related
    LOGOUT("C0109", "Account has been logged out"),
    WRONG_CREDENTIAL("A0201", "Incorrect username or password"),
    WRONG_CREDENTIAL_ALT("C0110", "Incorrect username or password"),
    NO_PERMISSION("A0301", "No permission, please contact admin"),
    NO_RESOURCE_ACCESS("A0302", "No access to this resource"),
    PARAM_ERROR("A0401", "Invalid parameters"),
    RATE_LIMIT("B0499", "Too many requests, please try again later"),
    RATE_LIMIT_ALT("A0499", "Too many requests, please try again later"),
    BAD_CREDENTIALS("C0102", "Incorrect password"),
    INVALID_TOKEN("C0105", "Invalid token"),
    NOT_INSTALLED("C0102_ALT", "App not installed"),
    NOT_SUPPORT("C0301", "Unsupported operation"),
    APP_NOT_FOUND("C0302", "App not found"),
    PLUGIN_NOT_FOUND("B0200", "Plugin not found"),
    CANNOT_MODIFY_PASSWORD("A0212", "Cannot modify another user's password"),
    NO_ORG_PERMISSION("A0304", "No permission for this organization or device"),
    DOC_EXISTS("A0429", "Document already exists"),
    DEVICE_NOT_FOUND("A0428", "Device not found"),
    DOC_DATA_ERROR("A0510", "Document data error"),
    DOC_NOT_FOUND("A0432", "Document not found"),
    PASSWORD_ERROR("A0211", "Incorrect password"),
    LOGIN_FAILED("A0210", "Incorrect username or password, please login again"),
    VERIFY_CODE_ERROR("A0444", "Verification code is incorrect"),
    DATA_TYPE_ERROR("A0512", "Invalid data type");

    companion object {
        /**
         * Get error description by resultCode and message
         * C0102 requires additional message check to distinguish "Incorrect password" and "App not installed"
         */
        fun fromCode(code: String, message: String? = null): String {
            // C0102 special case: distinguish by message content
            if (code == BAD_CREDENTIALS.code) {
                return if (message?.contains("Bad credentials") == true) {
                    BAD_CREDENTIALS.description
                } else {
                    NOT_INSTALLED.description
                }
            }

            // Direct match for other error codes
            return entries.firstOrNull { it.code == code }?.description
                ?: "Unknown error"
        }
    }
}