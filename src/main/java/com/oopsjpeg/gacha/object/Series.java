package com.oopsjpeg.gacha.object;

public enum Series
{
    NONE(0, "None"),
    VOCALOID(1, "Virtual Singer"),
    VTUBER(2, "VTuber");

    private final int id;
    private final String name;

    Series(int id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public static Series fromId(int id)
    {
        for (Series s : values())
            if (s.id == id) return s;
        return null;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }
}
