package me.bristermitten.devdenbot.listener

import com.google.inject.Inject
import me.bristermitten.devdenbot.discord.getPing
import me.bristermitten.devdenbot.discord.isStaff
import me.bristermitten.devdenbot.discord.shouldNotBePinged
import me.bristermitten.devdenbot.extensions.await
import me.bristermitten.devdenbot.extensions.commands.embedDefaults
import me.bristermitten.devdenbot.inject.Used
import me.bristermitten.devdenbot.serialization.DDBConfig
import me.bristermitten.devdenbot.trait.HasConfig
import me.bristermitten.devdenbot.util.addAndToCommaSeparated
import me.bristermitten.devdenbot.util.handleEachIn
import me.bristermitten.devdenbot.util.listenFlow
import me.bristermitten.devdenbot.util.scope
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

@Used
class PingingListener @Inject constructor(override val ddbConfig: DDBConfig) : EventListener, HasConfig {

    suspend fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.author.isBot || event.message.referencedMessage != null) {
            return
        }

        if (event.member?.isStaff() == false) {
            return
        }

        val thoseWhoShouldNotHaveBeenPinged = event.message.mentionedMembers.filter {
            it.shouldNotBePinged() && it.idLong != event.author.idLong
        }
        if (thoseWhoShouldNotHaveBeenPinged.isEmpty()) {
            return
        }
        val formattedNoPinged = thoseWhoShouldNotHaveBeenPinged.joinToString(",") {
            it.getPing()
        }.addAndToCommaSeparated()

        val authorPing = event.member?.getPing() ?: return
        event.channel.sendMessage(embedDefaults {
            title = "Anti Ping"
            description = """
                ${authorPing}, the following users have asked to not be pinged: $formattedNoPinged
                Please respect their decisions and try not to ping them!
            """.trimIndent()
        }).await()
    }

    override fun register(jda: JDA) {
        jda.listenFlow<GuildMessageReceivedEvent>().handleEachIn(scope, this::onGuildMessageReceived)
    }
}
