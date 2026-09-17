package com.torquelab.autoservice.auth.presentation

import androidx.annotation.StringRes
import com.torquelab.autoservice.R

@StringRes
fun AuthUiError.stringResourceId(): Int {
    return when (this) {
        AuthUiError.EMAIL_REQUIRED ->
            R.string.error_email_required

        AuthUiError.PASSWORD_REQUIRED ->
            R.string.error_password_required

        AuthUiError.WORKSHOP_NAME_REQUIRED ->
            R.string.error_workshop_name_required

        AuthUiError.PASSWORD_TOO_SHORT ->
            R.string.error_password_min_length

        AuthUiError.PASSWORDS_DO_NOT_MATCH ->
            R.string.error_passwords_not_match

        AuthUiError.INVALID_CREDENTIALS ->
            R.string.error_invalid_credentials

        AuthUiError.AUTHENTICATION_FAILED ->
            R.string.error_authentication_failed

        AuthUiError.REGISTRATION_FAILED ->
            R.string.error_registration_failed

        AuthUiError.CONNECTION_ERROR ->
            R.string.error_connection

        AuthUiError.UNEXPECTED_ERROR ->
            R.string.error_unexpected_authentication
    }
}