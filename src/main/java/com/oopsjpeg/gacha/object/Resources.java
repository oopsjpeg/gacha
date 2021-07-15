package com.oopsjpeg.gacha.object;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.object.json.ResourcesJson;

public class Resources
{
    private final Gacha gacha;
    private final ResourcesJson data;

    public static Resources create(Gacha gacha)
    {
        ResourcesJson data = new ResourcesJson();
        return new Resources(gacha, data);
    }

    public Resources(Gacha gacha, ResourcesJson data)
    {
        this.gacha = gacha;
        this.data = data;
    }

    public int getCrystals()
    {
        return data.crystals;
    }

    public void setCrystals(int cr)
    {
        data.crystals = cr;
    }

    public void addCrystals(int cr)
    {
        setCrystals(getCrystals() + cr);
    }

    public void subCrystals(int cr)
    {
        setCrystals(getCrystals() - cr);
    }

    public int getVioletRunes()
    {
        return data.violetRunes;
    }

    public void setVioletRunes(int vr)
    {
        data.violetRunes = vr;
    }

    public void addVioletRunes(int vr)
    {
        setVioletRunes(getVioletRunes() + vr);
    }

    public void subVioletRunes(int vr)
    {
        setVioletRunes(getVioletRunes() - vr);
    }

    public int getZenithCores()
    {
        return data.zenithCores;
    }

    public void setZenithCores(int zc)
    {
        data.zenithCores = zc;
    }

    public void addZenithCores(int zc)
    {
        setZenithCores(getZenithCores() + zc);
    }

    public void subZenithCores(int zc)
    {
        setZenithCores(getZenithCores() - zc);
    }

    public Gacha getGacha()
    {
        return gacha;
    }

    public ResourcesJson getData()
    {
        return data;
    }
}
