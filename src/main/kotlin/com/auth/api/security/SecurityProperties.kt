package com.auth.api.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "security")
class SecurityProperties {
    var permitAllPatterns: List<String> = emptyList()
}
