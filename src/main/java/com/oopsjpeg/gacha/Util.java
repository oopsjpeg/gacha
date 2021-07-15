package com.oopsjpeg.gacha;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Util
{

    public static final Random RANDOM = new Random();

    public static Color tweakColorAlpha(Color old, int alpha)
    {
        return new Color(old.getRed(), old.getGreen(), old.getBlue(), alpha);
    }

    public static Font font(String font, int size) throws IOException
    {
        try (InputStream fontStream = Util.class.getClassLoader().getResourceAsStream(font + ".TTF"))
        {
            return Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.PLAIN, size);
        }
        catch (FontFormatException error)
        {
            return new Font("Arial", Font.PLAIN, size);
        }
    }

    public static void drawImage(BufferedImage src, BufferedImage image, int align, int x, int y, int w, int h)
    {
        Graphics2D g2d = src.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        switch (align)
        {
            case 1:
                g2d.drawImage(image, x - (w / 2), y, w, h, null);
                break;
            case 2:
                g2d.drawImage(image, x - w, y, w, h, null);
                break;
            default:
                g2d.drawImage(image, x, y, w, h, null);
                break;
        }
    }

    public static void drawText(BufferedImage src, String s, Font font, Color color, int align, float x, float y)
    {
        Graphics2D g2d = src.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setFont(font);
        g2d.setColor(color);

        FontMetrics metrics = g2d.getFontMetrics();
        Rectangle2D bounds = metrics.getStringBounds(s, g2d);
        int ascent = metrics.getAscent();
        float fWidth = (float) bounds.getWidth();
        float fHeight = (float) bounds.getHeight();

        switch (align)
        {
            case 1:
                g2d.drawString(s, x - (fWidth / 2), y - (fHeight / 2) + ascent);
                break;
            case 2:
                g2d.drawString(s, x - fWidth, y - (fHeight / 2) + ascent);
                break;
            default:
                g2d.drawString(s, x, y - (fHeight / 2) + ascent);
                break;
        }
    }

    public static String stars(int amount)
    {
        if (amount == 6)
        {
            return "\u2726";
        }

        return String.join("", Collections.nCopies(amount, "\u2605"));
    }

    public static String starsRaw(int amount)
    {
        StringBuilder output = new StringBuilder();
        stars(amount).chars().forEach(c -> output.append("\\").append((char) c));
        return output.toString();
    }

    public static String toFileName(Object o)
    {
        return o.toString().replaceAll("[^a-zA-Z0-9.\\-]", "_");
    }

    public static String formatUsername(User user)
    {
        return user.getUsername() + "#" + user.getDiscriminator();
    }

    public static discord4j.rest.util.Color getColor(User user, Channel channel)
    {
        GatewayDiscordClient client = user.getClient();
        // Check if the channel is in a guild
        if (channel instanceof GuildChannel)
        {
            Guild guild = ((GuildChannel) channel).getGuild().block();
            Member member = user.asMember(guild.getId()).block();
            return getColor(member);
        }

        return discord4j.rest.util.Color.LIGHT_GRAY;
    }

    public static discord4j.rest.util.Color getColor(Member member)
    {
        discord4j.rest.util.Color color = discord4j.rest.util.Color.LIGHT_GRAY;

        // Get top-most role with a color
        Role topRole = member.getRoles()
                .filter(r -> !r.getColor().equals(discord4j.rest.util.Color.of(0)))
                .sort(Comparator.comparingInt(Role::getRawPosition))
                .blockLast();

        if (topRole != null)
            color = topRole.getColor();

        return color;
    }

    public static Color stringToColor(String s)
    {
        int[] rgb = Arrays.stream(s.split(", ")).mapToInt(Integer::parseInt).toArray();
        return new Color(rgb[0], rgb[1], rgb[2]);
    }

    public static boolean isDigits(CharSequence str)
    {
        return str.length() > 0 && str.codePoints().allMatch(Character::isDigit);
    }

    public static String timeDiff(LocalDateTime date1, LocalDateTime date2)
    {
        Duration duration = Duration.between(date1, date2);
        Stack<String> stack = new Stack<>();

        if (duration.toDays() > 0)
            stack.push(duration.toDays() + "d");
        duration = duration.minusDays(duration.toDays());

        if (duration.toHours() > 0)
            stack.push(duration.toHours() + "h");
        duration = duration.minusHours(duration.toHours());

        if (duration.toMinutes() > 0)
            stack.push(duration.toMinutes() + "m");
        duration = duration.minusMinutes(duration.toMinutes());

        if (duration.getSeconds() > 0)
            stack.push(duration.getSeconds() + "s");

        return stack.stream().limit(3).collect(Collectors.joining(" "));
    }

    public static String comma(int value)
    {
        return new DecimalFormat("#,###").format(value);
    }

    public static String percent(float f)
    {
        return new DecimalFormat("#").format(f * 100) + "%";
    }

    public static String sticker(String key, String value)
    {
        return "[**" + key + "**] " + value;
    }

    public static String crystals(int cr)
    {
        return "CR $" + comma(cr);
    }

    public static String violetRunes(int vr)
    {
        return "VR $" + comma(vr);
    }

    public static String zenithCores(int zc)
    {
        return "ZC $" + comma(zc);
    }

    public static String xp(int xp)
    {
        return "XP " + comma(xp);
    }

    public static int fontSize(String font)
    {
        switch (font)
        {
            default:
                return 36;
        }
    }
}
