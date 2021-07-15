package com.oopsjpeg.gacha.object;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.json.CardJson;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Card
{
    private final Gacha gacha;
    private final CardJson data;
    private final int id;

    public Card(Gacha gacha, CardJson data, int id)
    {
        this.gacha = gacha;
        this.data = data;
        this.id = id;
    }

    public Gacha getGacha()
    {
        return gacha;
    }

    public CardJson getData()
    {
        return data;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return data.name;
    }

    public String getVariant()
    {
        return data.variant;
    }

    public boolean hasVariant()
    {
        return data.variant != null;
    }

    public BufferedImage getImage() throws IOException
    {
        return gacha.getCardImageCache().retrieve(this);
    }

    public String getImageRaw()
    {
        return data.image;
    }

    public Series getSeries()
    {
        return data.series;
    }

    public String getSource()
    {
        return data.source;
    }

    public boolean hasSource()
    {
        return getSource() != null;
    }

    public int getTier()
    {
        return data.tier;
    }

    public boolean isDisabled()
    {
        return data.disabled;
    }

    public boolean isExclusive()
    {
        return data.exclusive;
    }

    public String getFrame()
    {
        return data.frame != null ? data.frame : "default";
    }

    public Color getFrameColor()
    {
        return getFrameColorRaw() != null ? Util.stringToColor(getFrameColorRaw()) : Color.BLACK;
    }

    public String getFrameColorRaw()
    {
        return data.frameColor;
    }

    public String getBorder()
    {
        return data.border;
    }

    public boolean hasBorder()
    {
        return getBorder() != null;
    }

    public Color getBorderColor()
    {
        return getBorderColorRaw() != null ? Util.stringToColor(getBorderColorRaw()) : new Color(0, 0, 0, 0.6f);
    }

    public String getBorderColorRaw()
    {
        return data.borderColor;
    }

    public Font getFont() throws IOException
    {
        return Util.font(getFontRaw(), getFontSize());
    }

    public String getFontRaw()
    {
        return data.font != null ? data.font : "MERRIWEATHER";
    }

    public int getFontSize()
    {
        return data.fontSize == 0 ? Util.fontSize(getFontRaw()) : data.fontSize;
    }

    public Color getFontColor()
    {
        return getFontColorRaw() != null ? Util.stringToColor(getFontColorRaw()) : Color.WHITE;
    }

    public String getFontColorRaw()
    {
        return data.fontColor;
    }

    public Archetype getArchetype()
    {
        return data.archetype;
    }

    public Stats getStats()
    {
        return Stats.stringToStats(data.stats);
    }

    public String format()
    {
        return "[`" + getId() + "`] (" + Util.stars(getTier()) + ") **" + getName() + "**" + (hasVariant() ? " - " + getVariant() : "");
    }

    public String formatRaw()
    {
        return format().replaceAll("\\*", "");
    }

    @Override
    public String toString()
    {
        return "Card[id=" + getId() + ", name=" + getName() + "]";
    }
}