package com.florianmski.airportcodes.data.rx;

import android.util.Base64;

import com.florianmski.airportcodes.data.models.Airport;
import com.florianmski.airportcodes.data.models.File;
import com.google.gson.Gson;

import rx.Observable;
import rx.Subscriber;

public class AirportOperator implements Observable.Operator<Airport, File>
{
    private static Gson gson = new Gson();

    @Override
    public Subscriber<? super File> call(final Subscriber<? super Airport> subscriber)
    {
        return new Subscriber<File>()
        {
            @Override
            public void onCompleted()
            {
                subscriber.onCompleted();
            }

            @Override
            public void onError(Throwable e)
            {
                subscriber.onError(e);
            }

            @Override
            public void onNext(File file)
            {
                try
                {
                    byte[] data = Base64.decode(file.content, Base64.DEFAULT);
                    String text = new String(data, "UTF-8");
                    Airport a = gson.fromJson(text, Airport.class);
                    subscriber.onNext(a);
                }
                catch(Exception e)
                {
                    subscriber.onError(e);
                }
            }
        };
    }
}