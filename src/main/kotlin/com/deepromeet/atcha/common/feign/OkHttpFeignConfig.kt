package com.deepromeet.atcha.common.feign

import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class OkHttpFeignConfig {
    @Bean
    fun okHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("Connection", "close")
                        .build()
                )
            }
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .callTimeout(5, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()

    @Bean
    fun feignClient(okHttpClient: OkHttpClient): feign.Client = feign.okhttp.OkHttpClient(okHttpClient)
}
