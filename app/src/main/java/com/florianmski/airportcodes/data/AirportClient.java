package com.florianmski.airportcodes.data;

import android.content.SharedPreferences;

import com.florianmski.airportcodes.BuildConfig;
import com.florianmski.airportcodes.data.models.Airport;
import com.florianmski.airportcodes.data.models.File;
import com.florianmski.airportcodes.data.rx.AirportOperator;
import com.florianmski.airportcodes.data.rx.FlattenOperator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;
import rx.functions.Func1;

public enum AirportClient
{
    INSTANCE;

    private AirportService airportService;
    private GithubService githubService;

    private FlattenOperator<File> flattenOperator = new FlattenOperator<>();
    private AirportOperator airportOperator = new AirportOperator();

    private AirportClient()
    {
        RequestInterceptor ifModifiedSinceRequestInterceptor = new IfModifiedSinceRequestInterceptor();

        // raw.githubusercontent.com doesn't support the ifModifiedSince header
        RestAdapter restAdapterAirport = new RestAdapter.Builder()
                .setEndpoint("https://raw.githubusercontent.com")
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .build();

        RestAdapter restAdapterGithub = new RestAdapter.Builder()
                .setEndpoint("https://api.github.com")
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .setRequestInterceptor(ifModifiedSinceRequestInterceptor)
                .build();

        airportService = restAdapterAirport.create(AirportService.class);
        githubService = restAdapterGithub.create(GithubService.class);
    }

    public Observable<Airport> getRawAirport(String code)
    {
        return airportService.airport(code);
    }

    public Observable<Airport> getAirport(String code)
    {
        return githubService.airport(code).lift(airportOperator);
    }

    public Observable<List<Airport>> getAirports()
    {
        return githubService
                .airports()
                .lift(flattenOperator)
                .filter(new Func1<File, Boolean>()
                {
                    @Override
                    public Boolean call(File file)
                    {
                        // keep only the json files
                        return file.name.endsWith(".json");
                    }
                })
                .map(new Func1<File, String>()
                {
                    @Override
                    public String call(File file)
                    {
                        // remove file extension
                        return file.name.replace(".json", "");
                    }
                })
                .flatMap(new Func1<String, Observable<Airport>>()
                {
                    @Override
                    public Observable<Airport> call(String code)
                    {
                        return getRawAirport(code).onErrorResumeNext(AirportClient.<Airport>on304ReturnNullFunc());
                    }
                })
                .filter(new Func1<Airport, Boolean>()
                {
                    @Override
                    public Boolean call(Airport airport)
                    {
                        return airport != null && airport.id != null;
                    }
                })
                .toList();
    }

    public Observable<Map<String, String>> getImages()
    {
        return githubService
                .images()
                .lift(flattenOperator)
                .map(new Func1<File, String>()
                {
                    @Override
                    public String call(File file)
                    {
                        return file.name;
                    }
                })
                .toMap(new Func1<String, String>()
                {
                    @Override
                    public String call(String s)
                    {
                        // map the airport id to the filename
                        return s.split("-")[0];
                    }
                });
    }

    public interface GithubService
    {
        @GET("/repos/lynnandtonic/airport-codes/contents/data")
        Observable<List<File>> airports();

        @GET("/repos/lynnandtonic/airport-codes/contents/data/{code}.json")
        Observable<File> airport(@Path("code") String code);

        @GET("/repos/lynnandtonic/airport-codes/contents/assets/images/card")
        Observable<List<File>> images();
    }

    // Have to do this to not explode Github's 60rq/hour limit
    // Do this the first time so we can import all the airports
    // Then use Github API so we can update only the airports who need too
    // UPDATE: doesn't work as "Last-Modified" seems to always be the time of the last commit
    // (for every files, regardless if they have been changed in the last commit or not, weird)
    public interface AirportService
    {
        @GET("/lynnandtonic/airport-codes/master/data/{code}.json")
        Observable<Airport> airport(@Path("code") String code);
    }

    // Use conditional requests so we don't update if nothing has changed
    private class IfModifiedSinceRequestInterceptor implements RequestInterceptor
    {
        private SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        private String ifModifiedSince = getIfModifiedSinceString();

        public IfModifiedSinceRequestInterceptor()
        {
            AirportPrefs.instance().getPrefs().registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener()
            {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
                {
                    // if lastUpdate change, update value
                    if(key.equals(AirportPrefs.instance().LAST_UPDATE))
                        ifModifiedSince = getIfModifiedSinceString();
                }
            });
        }

        @Override
        public void intercept(RequestFacade request)
        {
            request.addHeader("If-Modified-Since", ifModifiedSince);
        }

        private String getIfModifiedSinceString()
        {
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            return formatter.format(new Date(AirportPrefs.instance().getLastUpdate()));
//            return formatter.format(new Date(1426475638000L));
        }
    }

    public static <T> Func1<Throwable, Observable<T>> on304ReturnNullFunc()
    {
        return new Func1<Throwable, Observable<T>>()
        {
            @Override
            public Observable<T> call(Throwable throwable)
            {
                if(throwable instanceof RetrofitError)
                {
                    if(((RetrofitError)throwable).getResponse().getStatus() == 304)
                        return Observable.just(null);
                }

                return Observable.error(throwable);
            }
        };
    }
}
