package org.animation.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform