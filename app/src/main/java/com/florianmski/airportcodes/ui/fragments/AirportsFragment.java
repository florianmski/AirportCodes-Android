package com.florianmski.airportcodes.ui.fragments;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.florianmski.airportcodes.ListAirportAdapter;
import com.florianmski.airportcodes.R;
import com.florianmski.airportcodes.data.AirportPersister;
import com.florianmski.airportcodes.data.models.Airport;
import com.florianmski.airportcodes.data.rx.FlattenOperator;
import com.florianmski.airportcodes.data.rx.RxBus;
import com.florianmski.airportcodes.data.rx.SyncCompleteEvent;
import com.florianmski.airportcodes.ui.activities.AboutActivity;
import com.florianmski.airportcodes.ui.activities.AirportActivity;
import com.florianmski.spongeframework.adapters.RecyclerAdapter;
import com.florianmski.spongeframework.ui.fragments.RecyclerFragment;
import com.florianmski.spongeframework.utils.FileUtils;
import com.florianmski.spongeframework.utils.SearchUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class AirportsFragment extends RecyclerFragment<Airport> implements RecyclerAdapter.OnItemClickListener, SearchView.OnQueryTextListener
{
    private SearchUtils search = new SearchUtils(this, this);

    public static AirportsFragment newInstance()
    {
        return new AirportsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setInstantLoad(true);
        search.onCreate(savedInstanceState, null);
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager()
    {
        return new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.grid_columns));
    }

    @Override
    protected RecyclerAdapter<Airport, ?> createAdapter(List<Airport> items)
    {
        return new ListAirportAdapter(getActivity(), items, this);
    }

    @Override
    protected Observable<List<Airport>> createObservable()
    {
        copyLocalFileIfNone();

        return AirportPersister.INSTANCE.getAirports()
                .lift(new FlattenOperator<Airport>())
                .toSortedList(new Func2<Airport, Airport, Integer>()
                {
                    @Override
                    public Integer call(Airport airport1, Airport airport2)
                    {
                        return airport1.code.compareTo(airport2.code);
                    }
                });
    }

    private void copyLocalFileIfNone()
    {
        // copy file from assets to files dir
        // this is because the way I get the data is not reliable (github repo might change)
        // this way I'm sure the user always have some default airports
        // this also let me some time to fix the app if the repo structure change
        try
        {
            File airportFile = AirportPersister.getAiportFile(getActivity());
            if(!airportFile.exists())
            {
                InputStream isAirports = FileUtils.fromAssets(getActivity(), AirportPersister.FILE_NAME_AIRPORTS);
                FileUtils.copyFile(isAirports, airportFile);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        subscriptions.add(RxBus.INSTANCE.toObserverable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>()
                {
                    @Override
                    public void call(Object event)
                    {
                        if (event instanceof SyncCompleteEvent)
                            refresh(false);
                    }
                }));
    }

    @Override
    public void onItemClick(View v, int position)
    {
        AirportActivity.launch(getActivity(), getAdapter().getItem2(position).id);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        search.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        search.onCreateOptionsMenu(menu, "Which airport/city/country?", false);

        menu.add(Menu.NONE, R.id.action_bar_about, Menu.NONE, "About")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_bar_about:
                AboutActivity.launch(getActivity());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        search.onSaveInstanceState(outState);
    }

    @Override
    public boolean onQueryTextSubmit(String s)
    {
        search(s.toLowerCase());
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s)
    {
        search(s.toLowerCase());
        return true;
    }

    private void search(final String query)
    {
        Observable
                .from(data)
                .filter(new Func1<Airport, Boolean>()
                {
                    @Override
                    public Boolean call(Airport airport)
                    {
                        String[] values = new String[] {
                                airport.code,
                                airport.city,
                                airport.city2,
                                airport.city3,
                                airport.country };

                        for (String value : values)
                        {
                            if (value != null && value.toLowerCase().contains(query))
                                return true;
                        }
                        return false;
                    }
                })
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Airport>>()
                {
                    @Override
                    public void call(List<Airport> airports)
                    {
                        getAdapter().refresh(airports);
                    }
                });
    }
}
