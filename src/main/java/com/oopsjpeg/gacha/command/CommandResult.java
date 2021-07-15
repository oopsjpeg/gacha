package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.user.PlayerCard;
import com.oopsjpeg.gacha.util.Embeds;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class CommandResult
{
    private static final Color COLOR_SUCCESS = Color.of(119, 178, 85);
    private static final Color COLOR_FAILURE = Color.of(221, 46, 68);

    private final CommandCall call;
    private String content;
    private Consumer<EmbedCreateSpec> embed;
    private Tuple2<String, InputStream> file;
    private LayoutComponent[] components;

    public static CommandResult success(CommandCall call, String message)
    {
        return new CommandResult(call).setEmbed(e -> e
                .setColor(COLOR_SUCCESS)
                .setDescription(":white_check_mark:｜" + message));
    }

    public static CommandResult failure(CommandCall call, String message)
    {
        return new CommandResult(call).setEmbed(e -> e
                .setColor(COLOR_FAILURE)
                .setDescription(":x:｜" + message));
    }

    public static CommandResult failure(CommandCall call, Throwable throwable)
    {
        return failure(call, "An unknown exception occurred.\n\n" + throwable.getMessage());
    }

    public static CommandResult info(CommandCall call, String message)
    {
        return new CommandResult(call).setEmbed(e -> e.setDescription(message));
    }

    public static CommandResult card(CommandCall call, PlayerCard card, String content, String message) throws IOException
    {
        Gacha gacha = call.getGacha();
        try (InputStream is = gacha.getCardFullRenderCache().retrieveAsStream(card.getCard()))
        {
            return new CommandResult(call)
                    .setContent(content)
                    .setEmbed(Embeds.card(card, call.getUser(), message))
                    .setFile(card.getId() + ".png", is);
        }
    }

    public CommandResult(CommandCall call)
    {
        this.call = call;
    }

    public CommandCall getCall()
    {
        return call;
    }

    public User getUser()
    {
        return call.getUser();
    }

    public MessageChannel getChannel()
    {
        return call.getChannel();
    }

    public String getContent()
    {
        return content;
    }

    public CommandResult setContent(String content)
    {
        this.content = content;
        return this;
    }

    public Consumer<EmbedCreateSpec> getEmbed()
    {
        return embed;
    }

    public CommandResult setEmbed(Consumer<EmbedCreateSpec> embed)
    {
        this.embed = ((Consumer<EmbedCreateSpec>) e -> e
                .setColor(Util.getColor(getUser(), getChannel()))
                .setAuthor(Util.formatUsername(getUser()), null, getUser().getAvatarUrl()))
                .andThen(embed);
        return this;
    }

    public Tuple2<String, InputStream> getFile()
    {
        return file;
    }

    public CommandResult setFile(String fileName, InputStream stream)
    {
        file = Tuples.of(fileName, stream);
        return this;
    }

    public LayoutComponent[] getComponents()
    {
        return components;
    }

    public CommandResult setComponents(LayoutComponent... components)
    {
        this.components = components;
        return this;
    }
}
