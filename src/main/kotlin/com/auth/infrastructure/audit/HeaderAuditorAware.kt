package com.auth.infrastructure.audit

import com.auth.infrastructure.web.RequestHeaderUtils
import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component
import java.util.Optional

@Component("headerAuditorAware")
class HeaderAuditorAware : AuditorAware<String> {
    override fun getCurrentAuditor(): Optional<String> = Optional.ofNullable(RequestHeaderUtils.getCurrentUserInfo())
}
