package com.oopsjpeg.gacha.util;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.ImageCache;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CardImageCache extends ImageCache<Card>
{
    public CardImageCache(Gacha gacha)
    {
        super(gacha);
    }

    @Override
    public BufferedImage generate(Card card) throws IOException
    {
        return ImageIO.read(new File(getGacha().getSettings().getDataFolder() + "\\cards\\" + card.getImageRaw() + ".png"));
    }
}
