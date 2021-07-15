package com.oopsjpeg.gacha.object;

import com.oopsjpeg.gacha.Gacha;
import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.object.Card;
import com.oopsjpeg.gacha.object.user.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oopsjpeg on 3/12/2019.
 */
public enum Banner
{
    STANDARD("Standard", 1000)
            {
                @Override
                public Card get(Player player)
                {
                    Gacha gacha = player.getGacha();
                    List<Card> pool = new ArrayList<>();
                    float f = Util.RANDOM.nextFloat();

                    //if (f <= 0.0075)
                    //    pool.addAll(gacha.getCardsForStar(5));
                    if (f <= 0.0275)
                        pool.addAll(gacha.getCardsForStar(4));
                    else if (f <= 0.09)
                        pool.addAll(gacha.getCardsForStar(3));
                    else if (f <= 0.28)
                        pool.addAll(gacha.getCardsForStar(2));
                    else
                        pool.addAll(gacha.getCardsForStar(1));

                    pool.removeIf(Card::isExclusive);

                    return pool.get(Util.RANDOM.nextInt(pool.size()));
                }
            };

    private final String name;
    private final int cost;

    Banner(String name, int cost)
    {
        this.name = name;
        this.cost = cost;
    }

    public String getName()
    {
        return name;
    }

    public int getCost()
    {
        return cost;
    }

    public abstract Card get(Player player);
}
