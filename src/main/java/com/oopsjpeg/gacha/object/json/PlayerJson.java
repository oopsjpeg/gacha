package com.oopsjpeg.gacha.object.json;

import org.bson.codecs.pojo.annotations.BsonId;

import java.util.HashMap;
import java.util.Map;

public class PlayerJson
{
    @BsonId
    public String id;
    public ResourcesJson resources;
    public Map<String, PlayerCardJson> cards = new HashMap<>();
    public String dailyDate;
    public String weeklyDate;
    public String description;
    public String favoriteCardId;
}
