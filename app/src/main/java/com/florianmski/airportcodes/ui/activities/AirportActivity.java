package com.florianmski.airportcodes.ui.activities;

import android.app.Activity;
import android.os.Bundle;

import com.florianmski.airportcodes.R;
import com.florianmski.airportcodes.ui.fragments.AirportFragment;
import com.florianmski.spongeframework.ui.activities.TranslucentActivity;

public class AirportActivity extends TranslucentActivity
{
    @Override
    protected int getContentViewId()
    {
        return R.layout.activity_airport;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null)
        {
            String id = getIntent().getStringExtra(AirportFragment.BUNDLE_ID);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_airport, AirportFragment.newInstance(id))
                    .commit();
        }
    }

    public static void launch(Activity a, String id)
    {
        Bundle args = new Bundle();
        args.putString(AirportFragment.BUNDLE_ID, id);
        launchActivity(a, AirportActivity.class, args);
    }
}
