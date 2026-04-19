package com.ayush.security.domain.models

enum class BiometricStatus {
    AVAILABLE,
    NONE_ENROLLED,
    NO_HARDWARE,
    HARDWARE_UNAVAILABLE,
    UNSUPPORTED;
}
