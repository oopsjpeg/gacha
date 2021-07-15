package com.oopsjpeg.gacha.util;

import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.Stats;
import com.oopsjpeg.gacha.object.user.Player;
import com.oopsjpeg.gacha.object.user.PlayerCard;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;

import java.util.function.Consumer;

public class Embeds
{
    public static Consumer<EmbedCreateSpec> card(PlayerCard playerCard, Player player, String message)
    {
        Card card = playerCard.getCard();
        String avatar = player.getUser().getAvatarUrl();
        Stats stats = playerCard.getStats();

        return embed -> embed
                .setAuthor("[" + card.getId() + "] " + card.getName() + (card.hasVariant() ? " - " + card.getVariant() : "") + " (" + Util.stars(card.getTier()) + ")",
                        card.getSource(), avatar)
                .setDescription((message != null ? message + "\n\n" : "")
                        + Util.sticker("Level", "**" + (playerCard.getLevel() + 1) + "** (" + playerCard.getXp() + " / " + playerCard.getMaxXp() + ")") + "\n"
                        + Util.sticker("Stats", "**" + stats.getHealth() + "** HP **" + stats.getDefense() + "** DF **" + stats.getAttack() + "** AT **" + stats.getMagic() + "** MG"))
                .setImage("attachment://" + card.getId() + ".png")
                .setFooter(card.getArchetype().getName() + " - " + card.getSeries().getName() + " Series", null);
    }

    public static Consumer<EmbedCreateSpec> card(PlayerCard card, Player player)
    {
        return card(card, player, null);
    }


    public static Consumer<EmbedCreateSpec> card(PlayerCard card, User user, String message)
    {
        return card(card, card.getGacha().getPlayer(user), message);
    }

    public static Consumer<EmbedCreateSpec> card(PlayerCard card, User user)
    {
        return card(card, card.getGacha().getPlayer(user), null);
    }
}
