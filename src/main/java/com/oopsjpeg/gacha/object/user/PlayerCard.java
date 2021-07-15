package com.oopsjpeg.gacha.object.user;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.Stats;
import com.oopsjpeg.gacha.object.json.PlayerCardJson;
import discord4j.core.object.entity.User;

import java.util.Objects;

public class PlayerCard
{
    private final Player player;
    private final PlayerCardJson data;

    public static PlayerCard create(Player player, Card card)
    {
        PlayerCardJson data = new PlayerCardJson();
        data.id = card.getId();
        return new PlayerCard(player, data);
    }

    public PlayerCard(Player player, PlayerCardJson data)
    {
        Objects.requireNonNull(player);
        Objects.requireNonNull(data);
        this.player = player;
        this.data = data;
    }

    public Player getPlayer()
    {
        return player;
    }

    public User getUser()
    {
        return player.getUser();
    }

    public PlayerCardJson getData()
    {
        return data;
    }

    public Gacha getGacha()
    {
        return player.getGacha();
    }

    public int getId()
    {
        return data.id;
    }

    public Card getCard()
    {
        return getGacha().getCard(data.id);
    }

    public String getName()
    {
        return getCard().getName();
    }

    public String getImageRaw()
    {
        return getCard().getImageRaw();
    }

    public int getTier()
    {
        return getCard().getTier();
    }

    public boolean isExclusive()
    {
        return getCard().isExclusive();
    }

    public Stats getStats()
    {
        return getCard().getStats().multiply(getLevel() * (0.1f + (0.02f * getTier())));
    }

    public int getLevel()
    {
        return data.level;
    }

    public void setLevel(int level)
    {
        data.level = level;
    }

    public void addLevels(int levels)
    {
        setLevel(getLevel() + levels);
    }

    public int getXp()
    {
        return data.xp;
    }

    public void setXp(int xp)
    {
        data.xp = xp;
    }

    public void addXp(int xp)
    {
        setXp(getXp() + xp);
    }

    public void subXp(int xp)
    {
        setXp(getXp() - xp);
    }

    public boolean handleXp()
    {
        int oldLevel = getLevel();
        while (getXp() > getMaxXp())
        {
            subXp(getMaxXp());
            addLevels(1);
        }
        return getLevel() != oldLevel;
    }

    public int getMaxXp()
    {
        return (int) (200 + (Math.pow(getLevel() * 125, 1.03)));
    }

    public String format()
    {
        return getCard().format();
    }

    public String formatRaw()
    {
        return getCard().formatRaw();
    }
}
