package com.bennyhuo.jako

import android.app.Application
import android.content.Context
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Entry point for initializing and accessing the global application runtime environment on PC / Native.
 *
 * Calling [init] is optional: if [application] or [applicationContext] is accessed before [init],
 * a default [Application] instance is automatically initialized using platform-standard
 * directory conventions.
 */
object JakoApplication {
    private val lock = ReentrantLock()
    private var initialized = false
    private var activeConfig = JakoApplicationConfig()
    private var activeApplication: Application? = null
    private var activeContext: Context? = null

    val isInitialized: Boolean
        get() = lock.withLock { initialized }

    val config: JakoApplicationConfig
        get() = lock.withLock { activeConfig }

    val application: Application
        get() = lock.withLock {
            var app = activeApplication
            if (app == null) {
                app = Application()
                activeApplication = app
                if (activeContext == null) {
                    activeContext = app
                }
            }
            app
        }

    val applicationContext: Context
        get() = lock.withLock {
            activeContext ?: application
        }

    fun init(): Application = init(JakoApplicationConfig())

    fun init(config: JakoApplicationConfig): Application = lock.withLock {
        activeConfig = config
        initialized = true
        val app = Application()
        app.onCreate()
        activeApplication = app
        activeContext = app
        app
    }

    fun init(context: Context): Context = lock.withLock {
        initialized = true
        @Suppress("USELESS_ELVIS")
        val appCtx = context.getApplicationContext() ?: context
        activeContext = appCtx
        if (context is Application) {
            activeApplication = context
        } else if (appCtx is Application) {
            activeApplication = appCtx
        }
        appCtx
    }

    fun init(context: Context, config: JakoApplicationConfig): Context = lock.withLock {
        activeConfig = config
        initialized = true
        @Suppress("USELESS_ELVIS")
        val appCtx = context.getApplicationContext() ?: context
        activeContext = appCtx
        if (context is Application) {
            activeApplication = context
        } else if (appCtx is Application) {
            activeApplication = appCtx
        }
        appCtx
    }

    fun reset() = lock.withLock {
        initialized = false
        activeConfig = JakoApplicationConfig()
        activeApplication = null
        activeContext = null
    }
}

/**
 * Initializes [JakoApplication] using a [JakoApplicationConfig] DSL block and returns the global [Application].
 */
public inline fun JakoApplication.init(block: JakoApplicationConfig.() -> Unit): Application {
    val cfg = JakoApplicationConfig()
    cfg.block()
    return init(cfg)
}
