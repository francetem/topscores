package org.gmd.command

import com.github.ajalt.clikt.core.CliktCommand

class Leaderboard : CliktCommand(printHelpOnEmptyArgs = true), SlackCommand {
    override fun run() = Unit
}