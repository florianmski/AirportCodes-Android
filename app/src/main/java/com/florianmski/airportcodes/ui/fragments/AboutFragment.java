package com.florianmski.airportcodes.ui.fragments;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.florianmski.airportcodes.R;
import com.florianmski.spongeframework.ui.fragments.ScrollViewFragment;

import rx.Observable;

public class AboutFragment extends ScrollViewFragment<Void>
{
    public static AboutFragment newInstance()
    {
        return new AboutFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setInstantLoad(true);
    }

    @Override
    protected int getContentLayoutId()
    {
        return R.layout.fragment_about;
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState)
    {
        super.onViewCreated(v, savedInstanceState);

        ((TextView)v.findViewById(R.id.textViewCredits)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected Observable<Void> createObservable()
    {
        return Observable.just(null);
    }

    @Override
    protected void refreshView(Void data) {}

    @Override
    protected boolean isEmpty(Void data)
    {
        return false;
    }
}
