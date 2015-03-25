package com.florianmski.airportcodes;

import com.florianmski.airportcodes.data.AirportPersister;
import com.florianmski.airportcodes.data.AirportPrefs;
import com.florianmski.spongeframework.SpongeApplication;

public class AirportApplication extends SpongeApplication
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        AirportPrefs.create(this);
        AirportPersister.create(this);
        AirportService.start(this);
    }

    @Override
    public boolean isDebug()
    {
        return BuildConfig.DEBUG;
    }
}
