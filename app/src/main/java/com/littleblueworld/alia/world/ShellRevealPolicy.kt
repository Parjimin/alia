package com.littleblueworld.alia.world

object ShellRevealPolicy {
    fun message(
        alreadyFound: Boolean,
        firstReveal: List<String>,
        optionalJoke: String,
    ): List<String> = if (alreadyFound) {
        listOf(optionalJoke)
    } else {
        firstReveal + optionalJoke
    }
}
