package com.oopsjpeg.gacha.util;

/**
 * Created by oopsjpeg on 3/12/2019.
 */
public class AmountParser
{

    public static float getFloat(String s, float total)
    {
        return (float) getDouble(s, total);
    }

    public static double getDouble(String s, double total)
    {
        if (s.equalsIgnoreCase("all"))
        {
            return total;
        }
        else if (s.equalsIgnoreCase("half"))
        {
            return total / 2;
        }
        else if (s.endsWith("%"))
        {
            return total * (Double.parseDouble(s.substring(0, s.length() - 1)) / 100);
        }
        else
        {
            return Double.parseDouble(s);
        }
    }

    public static int getInt(String s, int total)
    {
        return (int) Math.floor(getDouble(s, total));
    }

    public static long getLong(String s, long total)
    {
        return (long) Math.floor(getDouble(s, total));
    }
}
