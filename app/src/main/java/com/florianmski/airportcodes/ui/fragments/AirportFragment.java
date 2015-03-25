package com.florianmski.airportcodes.ui.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.florianmski.airportcodes.R;
import com.florianmski.airportcodes.data.AirportPersister;
import com.florianmski.airportcodes.data.models.Airport;
import com.florianmski.spongeframework.ui.fragments.RxFragment;
import com.florianmski.spongeframework.utils.IntentUtils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.dex.movingimageviewlib.DexMovingImageView;
import rx.Observable;

public class AirportFragment extends RxFragment<Airport, View>
{
    public final static String BUNDLE_ID = "id";

    private final Pattern initialsPattern = Pattern.compile("\\*(.*?)\\*");

    private String id;

    private DexMovingImageView iv;
    private TextView tvCode;
    private TextView tvName;
    private TextView tvLocation;
    private TextView tvDescription;
    private TextView tvImageCredit;

    public static AirportFragment newInstance(String id)
    {
        AirportFragment f = new AirportFragment();
        Bundle args = new Bundle();
        args.putString(BUNDLE_ID, id);
        f.setArguments(args);
        return f;
    }

    @Override
    protected int getViewLayoutId()
    {
        return R.layout.fragment_airport;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setInstantLoad(true);

        id = getArguments().getString(BUNDLE_ID);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState)
    {
        super.onViewCreated(v, savedInstanceState);

        iv = (DexMovingImageView) v.findViewById(R.id.imageView);
        tvCode = (TextView) v.findViewById(R.id.textViewCode);
        tvName = (TextView) v.findViewById(R.id.textViewName);
        tvLocation = (TextView) v.findViewById(R.id.textViewLocation);
        tvDescription = (TextView) v.findViewById(R.id.textViewDescription);
        tvImageCredit = (TextView) v.findViewById(R.id.textViewImageCredit);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        menu.add(Menu.NONE, R.id.action_bar_location, Menu.NONE, "Location")
                .setIcon(R.drawable.ic_place_white_24dp)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        if(data != null)
            IntentUtils.setupShareText(getToolbar().getContext(), menu, getShareText(data.code));
    }

    private String getShareText(String code)
    {
        return String.format("Making sense of those three-letter airport codes. %s: http://airportcod.es/#airport/%s",
                code.toUpperCase(),
                code.toLowerCase());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.action_bar_location:
                IntentUtils.goToAddress(getActivity(), data.name);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Observable<Airport> createObservable()
    {
        return AirportPersister.INSTANCE.getAirport(id);
    }

    @Override
    protected void refreshView(final Airport airport)
    {
        getActivity().supportInvalidateOptionsMenu();

        Picasso.with(getActivity())
                .load(airport.imageLarge)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE)
                .into(iv);

        tvCode.setText(airport.code);
        tvName.setText(airport.name);
        tvDescription.setText(applyMarkdown(airport.description));

        String location = airport.city;
        if(!TextUtils.isEmpty(airport.stateShort))
            location += ", " + airport.stateShort;
        location += " - " + airport.country;
        tvLocation.setText(location);

        String photoBy = "photo by ";
        SpannableStringBuilder imageCredit = new SpannableStringBuilder(photoBy);
        imageCredit.append(airport.imageCredit);
        imageCredit.setSpan(new StyleSpan(Typeface.BOLD), photoBy.length(), imageCredit.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvImageCredit.setText(imageCredit);
        tvImageCredit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                IntentUtils.goToUrl(getActivity(), airport.imageCreditLink);
            }
        });

        getToolbar().setTitle(airport.code.toUpperCase());
        getToolbar().setSubtitle(airport.city);
    }

    private Spanned applyMarkdown(String text)
    {
        SpannableStringBuilder spannableString = new SpannableStringBuilder(text);
        int diff = 0;

        Matcher m = initialsPattern.matcher(text);
        while(m.find())
        {
            String entireMatch = m.group(0);
            String capturedGroup = m.group(1);

            int start = m.start() - diff;
            int end = m.end() - diff;

            spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.replace(start, end, capturedGroup.toUpperCase());

            diff += entireMatch.length() - capturedGroup.length();
        }

        return spannableString;
    }

    @Override
    protected boolean isEmpty(Airport data)
    {
        return data == null;
    }
}
