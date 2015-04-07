package com.florianmski.airportcodes;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.florianmski.airportcodes.data.AirportClient;
import com.florianmski.airportcodes.data.AirportPersister;
import com.florianmski.airportcodes.data.AirportPrefs;
import com.florianmski.airportcodes.data.rx.RxBus;
import com.florianmski.airportcodes.data.models.Airport;
import com.florianmski.airportcodes.data.rx.SyncCompleteEvent;
import com.florianmski.spongeframework.utils.FileUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.functions.Func2;
import timber.log.Timber;

public class AirportService extends IntentService
{
    public AirportService()
    {
        super("AirportService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        getAirportsWithImages(AirportPrefs.instance().getLastUpdate() == 0)
                .flatMap(new Func1<List<Airport>, Observable<List<Airport>>>()
                {
                    @Override
                    public Observable<List<Airport>> call(List<Airport> airports)
                    {
                        if (airports == null)
                            return Observable.error(new Throwable("no update needed"));
                        else
                            return AirportPersister.INSTANCE.saveAirports(airports);
                    }
                })
                .doOnCompleted(new Action0()
                {
                    @Override
                    public void call()
                    {
                        AirportPrefs.instance().putLastUpdate();
                        // if debug, copy to SD so we can have a look
                        if(BuildConfig.DEBUG)
                        {
                            try
                            {
                                FileUtils.copyFileToSD(getApplicationContext(), AirportPersister.getAiportFile(getApplicationContext()));
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .subscribe(new Subscriber<List<Airport>>()
                {
                    @Override
                    public void onCompleted()
                    {
                        Timber.d("Sync complete!");
                        RxBus.INSTANCE.send(new SyncCompleteEvent());
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.i(e, "Error during sync");
                    }

                    @Override
                    public void onNext(List<Airport> airports)
                    {

                    }
                });
    }

    private Observable<List<Airport>> getAirportsWithImages(boolean firstTime)
    {
        return Observable.zip(
                AirportClient.INSTANCE.getAirports().onErrorResumeNext(AirportClient.<List<Airport>>on304ReturnNullFunc()),
                AirportClient.INSTANCE.getImages().onErrorResumeNext(AirportClient.<Map<String, String>>on304ReturnNullFunc()),
                new Func2<List<Airport>, Map<String, String>, List<Airport>>()
                {
                    @Override
                    public List<Airport> call(List<Airport> airports, Map<String, String> images)
                    {
                        if(airports == null && images == null)
                            return null;

                        if(airports == null)
                            airports = AirportPersister.INSTANCE.getAirports().toBlocking().first();

                        if(images != null)
                        {
                            for (Airport airport : airports)
                            {
                                String name = images.get(airport.id);
                                if(name != null)
                                {
                                    airport.imageCard = getImageUrl("card", name);
                                    airport.imageSmall = getImageUrl("small", name);
                                    airport.imageMedium = getImageUrl("medium", name);
                                    airport.imageLarge = getImageUrl("large", name);
                                }
                            }
                        }

                        return airports;
                    }
                });
    }

    private String getImageUrl(String type, String name)
    {
        return String.format("https://raw.githubusercontent.com/lynnandtonic/airport-codes/master/assets/images/%s/%s", type, name);
    }

    public static void start(Context context)
    {
        context.startService(new Intent(context, AirportService.class));
    }
}
