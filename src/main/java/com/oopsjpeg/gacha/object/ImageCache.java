package com.oopsjpeg.gacha.object;

import com.oopsjpeg.gacha.Gacha;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class ImageCache<T>
{
    private final Gacha gacha;
    private final Map<T, BufferedImage> cache = new HashMap<>();

    public ImageCache(Gacha gacha)
    {
        this.gacha = gacha;
    }

    public BufferedImage retrieve(T t) throws IOException
    {
        if (!cache.containsKey(t))
            cache.put(t, generate(t));
        return cache.get(t);
    }

    public ByteArrayInputStream retrieveAsStream(T t) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(retrieve(t), "png", baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public abstract BufferedImage generate(T t) throws IOException;

    public Gacha getGacha()
    {
        return gacha;
    }
}
