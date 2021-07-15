package com.oopsjpeg.gacha.util;

import java.util.List;

public class DataUtils
{

    public static String getString(List<Object> data, int index)
    {
        return getString(data.toArray(new Object[0]), index);
    }

    public static String getString(Object[] data, int index)
    {
        return index >= data.length ? "" : getString(data[index]);
    }

    public static String getString(Object o)
    {
        return o == null ? "" : String.valueOf(o);
    }

    public static int getInt(List<Object> data, int index)
    {
        return getInt(data.toArray(new Object[0]), index);
    }

    public static int getInt(Object[] data, int index)
    {
        return index >= data.length ? 0 : getInt(data[index]);
    }

    public static int getInt(Object o)
    {
        return o == null ? 0 : Double.valueOf(String.valueOf(o)).intValue();
    }
}
