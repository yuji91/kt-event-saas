package com.example.kteventsaas

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class OpenInViewLogger(
    @Value("\${spring.jpa.open-in-view}") private val openInView: Boolean
) : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        println("✅ [確認] spring.jpa.open-in-view is set to: $openInView")
    }
}
