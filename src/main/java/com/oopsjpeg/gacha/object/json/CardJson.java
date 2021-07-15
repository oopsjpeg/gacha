package com.oopsjpeg.gacha.object.json;

import com.oopsjpeg.gacha.object.Archetype;
import com.oopsjpeg.gacha.object.Series;

public class CardJson
{
    public String name;
    public String variant;
    public String image;
    public Series series;
    public String source;

    public int tier;
    public boolean disabled;
    public boolean exclusive;

    public String frame;
    public String frameColor;
    public String border;
    public String borderColor;
    public String font;
    public int fontSize;
    public String fontColor;

    public Archetype archetype;
    public String stats;
}
