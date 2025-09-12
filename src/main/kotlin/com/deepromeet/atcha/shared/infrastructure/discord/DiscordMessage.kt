package com.deepromeet.atcha.shared.infrastructure.discord

class DiscordMessage(
    var content: String = "content",
    var embeds: List<Embed> = listOf(Embed())
) {
    class Embed(
        var title: String = "타이틀",
        var description: String = "설명"
    )
}
