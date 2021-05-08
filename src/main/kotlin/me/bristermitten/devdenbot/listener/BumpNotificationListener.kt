package me.bristermitten.devdenbot.listener

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.bristermitten.devdenbot.commands.roles.BUMP_NOTIFICATIONS_ROLE_ID
import me.bristermitten.devdenbot.data.StatsUsers
import me.bristermitten.devdenbot.extensions.await
import me.bristermitten.devdenbot.util.inc
import me.bristermitten.devdenbot.util.log
import me.bristermitten.devdenbot.util.mention
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.concurrent.TimeUnit

class BumpNotificationListener : ListenerAdapter() {

    companion object {
        private const val DISBOARD_BOT_ID = 302050872383242240
        private val BUMP_COOLDOWN = TimeUnit.HOURS.toMillis(2)
        private val logger by log()
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {

        if (event.author.idLong != DISBOARD_BOT_ID) {
            return
        }

        if (!event.message.embeds.first().description!!.contains("Check it on DISBOARD:")) {
            logger.trace {
                "DISBOARD bot sent a message that did not trigger a bump stat increase. The message's description was ${
                    event.message.embeds.first().description
                }"
            }
            return
        }
        logger.trace {
            "Bump successful. Increasing bumps stat for user ${event.message.mentionedUsers[0].name} from ${
                StatsUsers[event.message.mentionedUsers[0].idLong].bumps
            } to ${StatsUsers[event.message.mentionedUsers[0].idLong].bumps.get() + 1}"
        }

        StatsUsers[event.message.mentionedUsers[0].idLong].bumps++

        GlobalScope.launch {
            delay(BUMP_COOLDOWN)
            event.channel.sendMessage("${mention(BUMP_NOTIFICATIONS_ROLE_ID)}, the server is ready to be bumped! **!d bump**")
                .await()
            logger.trace { "Notified Bump Notification users that a new bump is available." }
        }
    }
}
