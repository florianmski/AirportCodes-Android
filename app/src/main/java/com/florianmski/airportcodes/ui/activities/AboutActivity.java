package com.florianmski.airportcodes.ui.activities;

import android.app.Activity;
import android.os.Bundle;

import com.florianmski.airportcodes.R;
import com.florianmski.airportcodes.ui.fragments.AboutFragment;
import com.florianmski.spongeframework.ui.activities.TranslucentActivity;

public class AboutActivity extends TranslucentActivity
{
    @Override
    protected int getContentViewId()
    {
        return R.layout.activity_about;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null)
        {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_about, AboutFragment.newInstance())
                    .commit();
        }
    }

    public static void launch(Activity a)
    {
        Bundle args = new Bundle();
        launchActivity(a, AboutActivity.class, args);
    }
}