package com.florianmski.airportcodes.data;

import android.content.Context;

import com.florianmski.airportcodes.data.models.Airport;
import com.florianmski.airportcodes.data.rx.FlattenOperator;
import com.florianmski.spongeframework.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import au.com.gridstone.grex.GRexPersister;
import au.com.gridstone.grex.converters.GsonConverter;
import rx.Observable;
import rx.functions.Func1;

public enum AirportPersister
{
    INSTANCE;

    private static final String DIR_NAME = "persistence";
    public static final String FILE_NAME_AIRPORTS = "airports.json";

    private static GRexPersister persister;
    private FlattenOperator<Airport> flattenOperator = new FlattenOperator<>();

    public static void create(Context context)
    {
        persister = new GRexPersister(context, DIR_NAME, new GsonConverter());
    }

    public Observable<List<Airport>> saveAirports(List<Airport> airports)
    {
        return persister.putList(FILE_NAME_AIRPORTS, airports, Airport.class);
    }

    public Observable<List<Airport>> getAirports()
    {
        return persister
                .getList(FILE_NAME_AIRPORTS, Airport.class);
    }

    public Observable<Airport> getAirport(final String id)
    {
        return getAirports()
                .lift(flattenOperator)
                .first(new Func1<Airport, Boolean>()
                {
                    @Override
                    public Boolean call(Airport airport)
                    {
                        return airport.id.equals(id);
                    }
                });
    }

    public static File getAiportFile(Context context) throws IOException
    {
        return FileUtils.fromInternalDir(context,
                AirportPersister.DIR_NAME,
                AirportPersister.FILE_NAME_AIRPORTS);
    }
}
