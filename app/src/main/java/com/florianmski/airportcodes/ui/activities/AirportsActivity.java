package com.florianmski.airportcodes.ui.activities;

import android.os.Bundle;

import com.florianmski.airportcodes.R;
import com.florianmski.airportcodes.ui.fragments.AirportsFragment;
import com.florianmski.spongeframework.ui.activities.TranslucentActivity;

public class AirportsActivity extends TranslucentActivity
{
    @Override
    protected int getContentViewId()
    {
        return R.layout.activity_airports;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getToolbar().setSubtitle("powered by airportcod.es");

        if(savedInstanceState == null)
        {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_airports, AirportsFragment.newInstance())
                    .commit();
        }
    }
}
