package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.data.EventUtils;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import com.oopsjpeg.roboops.framework.commands.exception.NotOwnerException;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.util.stream.Collectors;

public class EventsCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) throws NotOwnerException {
		if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
			IUser author = message.getAuthor();
			if (!Gacha.getInstance().getClient().getApplicationOwner().equals(author))
				throw new NotOwnerException();
			else
				Gacha.getInstance().loadEvents();
		} else {
			EmbedBuilder builder = new EmbedBuilder();
			builder.withTitle("Gacha Event Schedule");
			builder.withColor(Color.PINK);
			builder.withDesc(EventUtils.listEventsByDate(Gacha.getInstance().getEvents()
					.stream().filter(e -> !e.isFinished()).collect(Collectors.toList())));

			Bufferer.sendMessage(message.getChannel(), "Viewing the event schedule.", builder.build());
		}
	}

	@Override
	public String getName() {
		return "events";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"schedule"};
	}
}
