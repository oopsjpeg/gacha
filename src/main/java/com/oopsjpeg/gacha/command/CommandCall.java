package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

import java.util.Arrays;

public class CommandCall
{
    private final CommandManager manager;
    private final String alias;
    private final String[] arguments;

    private final Snowflake guildId;
    private final Snowflake channelId;
    private final Snowflake userId;

    public CommandCall(CommandManager manager, String alias, String[] arguments, Guild guild, MessageChannel channel, User user)
    {
        this.manager = manager;
        this.alias = alias;
        this.arguments = arguments;

        guildId = guild == null ? null : guild.getId();
        channelId = channel == null ? null : channel.getId();
        userId = user == null ? null : user.getId();
    }

    public static CommandCall of(CommandManager manager, Message message)
    {
        Gacha gacha = manager.getGacha();
        User user = message.getAuthor().orElse(null);

        if (user != null && !user.isBot())
        {
            Guild guild = message.getGuild().block();
            MessageChannel channel = message.getChannel().block();

            String content = message.getContent();
            String[] split = content.split(" ");

            if (split[0].toLowerCase().startsWith(manager.getPrefix().toLowerCase()))
            {
                String alias = split[0].replaceFirst(manager.getPrefix(), "");
                String[] args = Arrays.copyOfRange(split, 1, split.length);
                return new CommandCall(manager, alias, args, guild, channel, user);
            }
        }

        return null;
    }

    public static CommandCall of(CommandManager manager, Interaction interaction)
    {
        ApplicationCommandInteraction aci = interaction.getCommandInteraction().get();
        String alias = aci.getName().get();
        String[] args = aci.getOptions().stream()
                .map(o -> o.getValue()
                        .map(ApplicationCommandInteractionOptionValue::asString).get())
                .toArray(String[]::new);
        Guild guild = interaction.getGuild().block();
        MessageChannel channel = interaction.getChannel().block();
        User user = interaction.getUser();

        return new CommandCall(manager, alias, args, guild, channel, user);
    }

    public String format(Command command)
    {
        return manager.getPrefix() + command.getName();
    }

    public CommandManager getManager()
    {
        return manager;
    }

    public Gacha getGacha()
    {
        return getManager().getGacha();
    }

    public GatewayDiscordClient getClient()
    {
        return getGacha().getClient();
    }

    public String getAlias()
    {
        return alias;
    }

    public String[] getArguments()
    {
        return arguments;
    }

    public String getArgument(int index)
    {
        return index >= 0 && getArguments().length > index ? getArguments()[index] : "";
    }

    public boolean hasArguments()
    {
        return getArguments().length != 0;
    }

    public String getLastArgument()
    {
        return getArgument(getArguments().length - 1);
    }

    public String getArgumentsRaw()
    {
        return String.join(" ", getArguments());
    }

    public Guild getGuild()
    {
        return getClient().getGuildById(guildId).block();
    }

    public MessageChannel getChannel()
    {
        return getClient().getChannelById(channelId).cast(MessageChannel.class).block();
    }

    public User getUser()
    {
        return getClient().getUserById(userId).block();
    }

    public Member getMember()
    {
        return getUser().asMember(guildId).block();
    }
}
