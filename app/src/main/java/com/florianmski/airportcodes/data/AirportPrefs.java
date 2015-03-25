package com.florianmski.airportcodes.data;

import com.florianmski.spongeframework.utils.PrefUtils;

public class AirportPrefs extends PrefUtils
{
    public final String LAST_UPDATE = getKey("lastUpdate");

    private static AirportPrefs instance;

    private AirportPrefs() {}

    public static AirportPrefs instance()
    {
        if(instance == null)
            instance = new AirportPrefs();
        return instance;
    }

    public void putLastUpdate()
    {
        putLong(LAST_UPDATE, System.currentTimeMillis());
    }

    public long getLastUpdate()
    {
        return getLong(LAST_UPDATE, 0);
    }
}
