package com.oopsjpeg.gacha.object;

import java.util.List;

public class Mission
{

    private String id;
    private int cooldown; // in days
    private int reward;
    private List<String> dependencies;
    private List<Task> tasks;

    public String getId()
    {
        return id;
    }

    public int getCooldown()
    {
        return cooldown;
    }

    public int getReward()
    {
        return reward;
    }

    public List<String> getDependencies()
    {
        return dependencies;
    }

    public List<Task> getTasks()
    {
        return tasks;
    }
}
