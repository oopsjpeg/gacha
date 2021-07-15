package com.oopsjpeg.gacha.object;

public enum Archetype
{
    BLOCKER("Blocker"),
    SHREDDER("Shredder"),
    FIGHTER("Fighter"),
    CASTER("Caster");

    private final String name;

    Archetype(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
