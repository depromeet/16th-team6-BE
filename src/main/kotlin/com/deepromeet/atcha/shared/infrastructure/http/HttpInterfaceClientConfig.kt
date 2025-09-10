package com.deepromeet.atcha.shared.infrastructure.http

import org.springframework.context.annotation.Configuration

@Configuration
class HttpInterfaceClientConfig {
    // 각 클라이언트에서 개별적으로 HttpServiceProxyFactory를 생성하도록 변경
    // 이 클래스는 추후 공통 설정이 필요할 때 사용
}
