package com.amagesoftware.vestibio.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amagesoftware.vestibio.R;
import com.travijuu.numberpicker.library.NumberPicker;

import butterknife.BindView;

/**
 * Created by jkhinda on 22/06/18.
 */
public class DisclaimerFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    @BindView(R.id.disclaimerId)
    TextView disclaimer;
    @BindView(R.id.privacyId)
    TextView privacy;
    @BindView(R.id.note)
    TextView note;

    public DisclaimerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DisclaimerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DisclaimerFragment newInstance(String param1, String param2) {
        DisclaimerFragment fragment = new DisclaimerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_disclaimer, container, false);

        SpannableString privacyPolicy = new SpannableString(getString(R.string.privacy_policy_link));
        String urlPrivacy = getString(R.string.privacy_policy_url);
        privacyPolicy.setSpan(new URLSpan(urlPrivacy), 16, privacyPolicy.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString disclaimerPolicy = new SpannableString(getString(R.string.disclaimer_link));
        String urlDisclaimer = getString(R.string.privacy_disclaimer_url);
        disclaimerPolicy.setSpan(new URLSpan(urlDisclaimer), 16, disclaimerPolicy.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView disclaimer = (TextView) view.findViewById(R.id.disclaimerId);
        disclaimer.setText(disclaimerPolicy);
        disclaimer.setMovementMethod(LinkMovementMethod.getInstance()); // enable clicking on url span

        TextView privacy = (TextView) view.findViewById(R.id.privacyId);
        privacy.setText(privacyPolicy);
        privacy.setMovementMethod(LinkMovementMethod.getInstance()); // enable clicking on url span

        return view;

    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


}
