package com.example.chatmessenger.util

import android.util.Patterns

fun validationEmail(email: String): RegisterValidation {
    if (email.isEmpty())
        return RegisterValidation.Failed("Email Cannot Be Empty")
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        return RegisterValidation.Failed("Wrong Email Format")
    return RegisterValidation.Success
}

fun validationPassword(password: String): RegisterValidation {
    if (password.isEmpty())
        return RegisterValidation.Failed("Password Cannot Be Empty")
    if (password.length < 6)
        return RegisterValidation.Failed("Password Should Be 6 Char Long Or More")
    return RegisterValidation.Success
}