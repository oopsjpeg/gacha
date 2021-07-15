package com.oopsjpeg.gacha.object.user;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.Resources;
import com.oopsjpeg.gacha.object.json.PlayerJson;
import com.oopsjpeg.gacha.object.json.ResourcesJson;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Player
{
    private final Gacha gacha;
    private final PlayerJson data;

    public static Player create(Gacha gacha, String id)
    {
        PlayerJson data = new PlayerJson();
        data.id = id;
        return new Player(gacha, data);
    }

    public Player(Gacha gacha, PlayerJson data)
    {
        this.gacha = gacha;
        this.data = data;
    }

    public Gacha getGacha()
    {
        return gacha;
    }

    public PlayerJson getData()
    {
        return data;
    }

    public String getId()
    {
        return data.id;
    }

    public User getUser()
    {
        return gacha.getClient().getUserById(Snowflake.of(getId())).block();
    }

    public Resources getResources()
    {
        if (data.resources == null)
            data.resources = new ResourcesJson();
        return new Resources(gacha, data.resources);
    }

    public void setResources(Resources resources)
    {
        data.resources = resources.getData();
    }

    public List<PlayerCard> getCards()
    {
        return data.cards.values().stream()
                .map(json -> new PlayerCard(this, json))
                .collect(Collectors.toList());
    }

    public Map<PlayerCard, Integer> searchCards(String query)
    {
        Map<PlayerCard, Integer> results = new HashMap<>();
        String[] split = query.toLowerCase().split(" ");

        for (String term : split)
        {
            for (PlayerCard card : getCards())
            {
                int matches = 0;
                // ID
                if (String.valueOf(card.getId()).equals(term))
                    matches++;
                // Name
                if (card.getName().toLowerCase().contains(term))
                    matches++;
                // Variant
                if (card.getCard().hasVariant() && card.getCard().getVariant().toLowerCase().contains(term))
                    matches++;

                if (matches > 0)
                    results.put(card, matches);
            }
        }
        return results;
    }

    public PlayerCard searchCard(String query)
    {
        return searchCards(query).entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public boolean hasCards()
    {
        return !getCards().isEmpty();
    }

    public PlayerCard getCard(Card card)
    {
        return getCard(card.getId());
    }

    public PlayerCard getCard(int id)
    {
        return new PlayerCard(this, data.cards.get(String.valueOf(id)));
    }

    public PlayerCard getBestCard()
    {
        return getCards().stream().max(Comparator.comparingInt(PlayerCard::getTier)).orElse(null);
    }

    public void addCard(PlayerCard card)
    {
        data.cards.put(String.valueOf(card.getId()), card.getData());
    }

    public void removeCard(PlayerCard card)
    {
        data.cards.remove(String.valueOf(card.getId()));
    }

    public boolean hasCard(Card card)
    {
        return data.cards.containsKey(String.valueOf(card.getId()));
    }

    public LocalDateTime getDailyDate()
    {
        return data.dailyDate == null ? null : LocalDateTime.parse(data.dailyDate);
    }

    public void setDailyDate(LocalDateTime dailyDate)
    {
        data.dailyDate = dailyDate.toString();
    }

    public boolean hasDaily()
    {
        return data.dailyDate == null || LocalDateTime.now().isAfter(getDailyDate().plusDays(1));
    }

    public String timeUntilDaily()
    {
        return Util.timeDiff(LocalDateTime.now(), getDailyDate().plusDays(1));
    }

    public LocalDateTime getWeeklyDate()
    {
        return data.weeklyDate == null ? null : LocalDateTime.parse(data.weeklyDate);
    }

    public void setWeeklyDate(LocalDateTime weeklyDate)
    {
        data.weeklyDate = weeklyDate.toString();
    }

    public String timeUntilWeekly()
    {
        return Util.timeDiff(LocalDateTime.now(), getWeeklyDate().plusWeeks(1));
    }

    public boolean hasWeekly()
    {
        return data.weeklyDate == null || LocalDateTime.now().isAfter(getWeeklyDate().plusWeeks(1));
    }

    public String getDescription()
    {
        return hasDescription() ? data.description : "Curious Cardseeker";
    }

    public void setDescription(String description)
    {
        data.description = description;
    }

    public void clearDescription()
    {
        data.description = null;
    }

    public boolean hasDescription()
    {
        return data.description != null;
    }

    public PlayerCard getFavoriteCard()
    {
        return hasFavoriteCard() ? getCard(Integer.parseInt(data.favoriteCardId)) : null;
    }

    public void setFavoriteCard(PlayerCard card)
    {
        data.favoriteCardId = String.valueOf(card.getId());
    }

    public boolean hasFavoriteCard()
    {
        return data.favoriteCardId != null;
    }

    public void clearFavoriteCard()
    {
        data.favoriteCardId = null;
    }

    public int getTier()
    {
        return getCards().isEmpty() ? 1 : Collections.max(getCards().stream()
                .map(PlayerCard::getTier)
                .collect(Collectors.toList()));
    }
}
