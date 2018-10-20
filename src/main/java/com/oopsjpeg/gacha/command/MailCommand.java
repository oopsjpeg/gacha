package com.oopsjpeg.gacha.command;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.user.UserInfo;
import com.oopsjpeg.gacha.object.user.UserMail;
import com.oopsjpeg.gacha.util.Embeds;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;
import java.util.stream.Collectors;

public class MailCommand implements Command {
	private final Gacha instance = Gacha.getInstance();

	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IChannel channel = message.getChannel();
		IUser author = message.getAuthor();
		UserInfo info = instance.getUser(author);

		if (info.getMail().isEmpty())
			// User doesn't have any mail
			Util.sendError(channel, author, "you do not have any mail.");
		else if (args.length >= 1 && args[0].equalsIgnoreCase("notifs")) {
			if (info.getMailNotifs()) {
				info.setMailNotifs(false);
				Bufferer.sendMessage(channel, Util.nameThenID(author) + ", you have disabled mail notifications.");
			} else {
				info.setMailNotifs(true);
				Bufferer.sendMessage(channel, Util.nameThenID(author) + ", you have enabled mail notifications.");
			}
			instance.getMongo().saveUser(info);
		} else if (args.length >= 1 && args[0].equalsIgnoreCase("discard")) {
			// Discard mail
			if (args.length >= 2 && args[1].equalsIgnoreCase("all"))
				// Discard all deletable mail
				discardAll(channel, info);
			else if (args.length >= 2) {
				// Discard specific mail
				int index = Integer.parseInt(args[1]);
				if (index <= 0 || index > info.getMail().size())
					Util.sendError(channel, author, "invalid mail index.");
				else
					discard(channel, info, info.getMail(index - 1));
			} else if (info.getLastMail() != null)
				// Discord last mail viewed
				discard(channel, info, info.getLastMail());
		} else if (args.length >= 1 && args[0].equalsIgnoreCase("collect")) {
			// Collect gifts from mail
			if (args.length >= 2 && args[1].equalsIgnoreCase("all"))
				// Collect all gifts available
				collectAllGifts(channel, info);
			else if (args.length >= 2) {
				// Collect specific gift from mail
				int index = Integer.parseInt(args[1]);
				if (index <= 0 || index > info.getMail().size())
					Util.sendError(channel, author, "invalid mail index.");
				else
					collectGift(channel, info, info.getMail(index - 1));
			} else if (info.getLastMail() != null)
				// Collect gift from last mail viewed
				collectGift(channel, info, info.getLastMail());
		} else {
			// View specific mail / most recent mail
			int index = args.length == 0 ? info.getMail().size() : Integer.parseInt(args[0]);
			if (index <= 0 || index > info.getMail().size())
				Util.sendError(channel, author, "invalid mail index.");
			else {
				UserMail mail = info.getMail(index - 1);
				instance.getMongo().saveUser(info);
				Bufferer.sendMessage(channel, "Viewing mail **" + index + "** of **"
								+ info.getMail().size() + "** for " + Util.nameThenID(author) + ".",
						Embeds.mail(author, channel, mail));
			}
		}
	}

	@Override
	public String getName() {
		return "mail";
	}

	private void discardAll(IChannel channel, UserInfo info) {
		IUser user = info.getUser();
		List<UserMail> mail = info.getMail().stream()
				.filter(m -> !m.hasGift() || m.isGiftCollected())
				.collect(Collectors.toList());

		if (mail.isEmpty())
			Util.sendError(channel, user, "you do not have any deletable mail.");
		else {
			info.getMail().removeAll(mail);
			instance.getMongo().saveUser(info);
			Bufferer.sendMessage(channel, Util.nameThenID(user) + " discarded all their deletable mail.");
		}
	}

	private void discard(IChannel channel, UserInfo info, UserMail mail) {
		IUser user = info.getUser();
		if (mail.hasGift() && !mail.isGiftCollected())
			Util.sendError(channel, user, "you cannot discard mail with pending gift(s).");
		else {
			info.getMail().remove(mail);
			instance.getMongo().saveUser(info);
			Bufferer.sendMessage(channel, Util.nameThenID(user) + " discarded **"
					+ mail.getContent().getSubject() + "**.");
		}
	}

	private void collectAllGifts(IChannel channel, UserInfo info) {
		IUser user = info.getUser();
		if (info.getMail().stream().allMatch(mail -> !mail.hasGift() || mail.isGiftCollected()))
			Util.sendError(channel, user, "you do not have any gifts to collect.");
		else {
			info.getMail().forEach(mail -> mail.collectGift(info));
			instance.getMongo().saveUser(info);
			Bufferer.sendMessage(channel, Util.nameThenID(user) + " collected all their gifts.");
		}
	}

	private void collectGift(IChannel channel, UserInfo info, UserMail mail) {
		IUser user = info.getUser();
		if (mail.getGift() == null)
			Util.sendError(channel, user, "this mail does not have a gift.");
		else if (mail.isGiftCollected())
			Util.sendError(channel, user, "you already collected this gift.");
		else {
			mail.collectGift(info);
			instance.getMongo().saveUser(info);
			Bufferer.sendMessage(channel, Util.nameThenID(user) + " collected a gift from **"
					+ mail.getContent().getSubject() + "**.");
		}
	}
}
