package me.bristermitten.devdenbot.pasting

import me.bristermitten.devdenbot.discord.*
import me.bristermitten.devdenbot.extensions.await
import me.bristermitten.devdenbot.inject.Used
import me.bristermitten.devdenbot.listener.EventListener
import me.bristermitten.devdenbot.util.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent

@Used
class PasteReactionListener : EventListener {

    companion object {
        private val log by log()
    }

    private suspend fun onReactionAdd(event: MessageReactionAddEvent) {

        if (!event.reactionEmote.isEmote) {
            // only consider custom emojis
            return
        }

        if (event.reactionEmote.emote.idLong != PASTE_EMOJI_ID) {
            return
        }

        val reactionMember =
            event.user
                ?.let { event.guild.getMember(it) }
                ?: event.guild.retrieveMemberById(event.userId).await()

        if (reactionMember.user.isBot) {
            log.warn { "Bot ${reactionMember.user.name} tried to use the paste reaction command. Only humans may perform this action." }
            return
        }

        if (!reactionMember.hasRoleOrAbove(SUPPORT_ROLE_ID)) {
            log.debug { "User ${reactionMember.user.name} has insufficient permissions to perform paste reactions." }
            return
        }

        val message = event.retrieveMessage().await()
        val mention = message.fetchMember().getPing()
        val pasteUrl = HasteClient.postCode(message.contentStripped)

        message.delete().queue()

        val pasteMessage = "$mention, your code is available at $pasteUrl"
        event.channel.sendMessage(pasteMessage).queue()
    }

    override fun register(jda: JDA) {
        jda.listenFlow<MessageReactionAddEvent>().handleEachIn(scope, this::onReactionAdd)
    }
}
