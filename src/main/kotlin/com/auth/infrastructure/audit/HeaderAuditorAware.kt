package com.auth.infrastructure.audit

import com.auth.infrastructure.web.RequestHeaderUtils
import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component
import java.util.*

@Component("headerAuditorAware")
class HeaderAuditorAware : AuditorAware<String> {
    override fun getCurrentAuditor(): Optional<String> {
        return Optional.ofNullable(RequestHeaderUtils.getCurrentUserInfo())
    }
}