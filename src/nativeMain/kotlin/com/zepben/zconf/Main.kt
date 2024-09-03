package com.zepben.zconf

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class Main: CliktCommand(name = "zconf") {
    override fun run() {
        // Noop
    }
}

fun main(args: Array<String>) = Main().subcommands(Version()).main(args)
