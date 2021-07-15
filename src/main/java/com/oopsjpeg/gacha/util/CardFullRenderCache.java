package com.oopsjpeg.gacha.util;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.ImageCache;
import com.oopsjpeg.gacha.object.Stats;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CardFullRenderCache extends ImageCache<Card>
{
    public CardFullRenderCache(Gacha gacha)
    {
        super(gacha);
    }

    @Override
    public BufferedImage generate(Card card) throws IOException
    {
        BufferedImage canvas = new BufferedImage(500, 680, BufferedImage.TYPE_INT_ARGB);
        BufferedImage image = card.getImage();
        BufferedImage frame = ImageIO.read(new File(getGacha().getSettings().getDataFolder() + "\\frames\\" + card.getFrame() + ".png"));
        Font font = card.getFont();

        // Draw the frame
        Util.drawImage(canvas, frame, 0, 0, 0, canvas.getWidth(), canvas.getHeight());
        // Color the frame
        Graphics2D g2d = canvas.createGraphics();
        g2d.setComposite(MultiplyComposite.Multiply);
        g2d.setColor(card.getFrameColor());
        g2d.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw the card image
        Util.drawImage(canvas, image, 0, 16, 90, 468, 468);

        // Draw text with variant
        Color fontColor = card.getFontColor();
        if (card.hasVariant())
        {
            // Draw the card name
            Util.drawText(canvas, card.getName(), font, fontColor, 0, 14, 35);
            // Draw the card variant
            Util.drawText(canvas, card.getVariant(), font.deriveFont(18.0f), Util.tweakColorAlpha(Color.WHITE, 230), 0, 14, 67);
        }
        else
        {
            // Draw the card name
            Util.drawText(canvas, card.getName(), font, fontColor, 0, 14, 45);
        }

        // Draw stats text
        Stats stats = card.getStats();
        Util.drawText(canvas, "HP " + stats.getHealth() + " / DF " + stats.getDefense(), font.deriveFont(22.0f), Color.WHITE, 2, canvas.getWidth() - 14, 30);
        Util.drawText(canvas, "AT " + stats.getAttack() + " / MG " + stats.getMagic(), font.deriveFont(22.0f), Color.WHITE, 2, canvas.getWidth() - 14, 60);

        // Draw the card stars
        Font starFont = new Font("Default", Font.BOLD, 60);
        Util.drawText(canvas, Util.stars(card.getTier()), starFont, fontColor, 1, (float) canvas.getWidth() / 2, canvas.getHeight() - 69);

        // Draw legend stars
        if (card.getTier()== 6)
        {
            Color textColorMini = Util.tweakColorAlpha(fontColor, 116);
            Font starFontMini = starFont.deriveFont(46.0f);

            Util.drawText(canvas, Util.stars(3), starFontMini, textColorMini, 1, 150, canvas.getHeight() - 69);
            Util.drawText(canvas, Util.stars(3), starFontMini, textColorMini, 1, canvas.getWidth() - 149, canvas.getHeight() - 69);
        }

        canvas.getGraphics().dispose();

        return canvas;
    }
}
