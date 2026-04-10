package com.example.gitsage.ai

import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.net.HttpConfigurable
import okhttp3.OkHttpClient
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

object HttpClientConfig {
    private val logger = Logger.getInstance(HttpClientConfig::class.java)
    private const val CONNECT_TIMEOUT_SECONDS = 10L
    private const val READ_TIMEOUT_SECONDS = 60L
    private const val WRITE_TIMEOUT_SECONDS = 30L

    fun createClient(): OkHttpClient {
        logger.info("[HttpClientConfig] Creating HTTP client")
        val builder = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        configureProxy(builder)

        val client = builder.build()
        logger.info("[HttpClientConfig] HTTP client created successfully")
        return client
    }

    private fun configureProxy(builder: OkHttpClient.Builder) {
        val httpConfigurable = HttpConfigurable.getInstance()
        logger.info("[HttpClientConfig] Configuring proxy")
        logger.info("[HttpClientConfig] USE_HTTP_PROXY: ${httpConfigurable.USE_HTTP_PROXY}")

        if (httpConfigurable.USE_HTTP_PROXY && httpConfigurable.PROXY_HOST.isNotEmpty()) {
            val proxy = Proxy(
                Proxy.Type.HTTP,
                InetSocketAddress(httpConfigurable.PROXY_HOST, httpConfigurable.PROXY_PORT)
            )
            builder.proxy(proxy)
            logger.info("[HttpClientConfig] Proxy configured: ${httpConfigurable.PROXY_HOST}:${httpConfigurable.PROXY_PORT}")
        } else {
            logger.info("[HttpClientConfig] No proxy configured")
        }
    }
}
