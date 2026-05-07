package com.example.infrastructure.config

import com.example.domain.LinkReplacementService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * ドメインサービスをSpringのDIコンテナに登録する設定クラス。
 * ドメイン層にはSpringアノテーションを付与しないため、ここでBean定義を行う。
 */
@Configuration
class DomainConfig {

    @Bean
    fun linkReplacementService(): LinkReplacementService = LinkReplacementService()
}
