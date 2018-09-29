package com.limkee.payment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.limkee.R;
import com.limkee.entity.Customer;
import com.limkee.order.ConfirmOrderActivity;
import com.stripe.android.view.CardInputWidget;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import io.reactivex.disposables.CompositeDisposable;

public class PaymentFragment extends Fragment {
    private com.limkee.payment.PaymentFragment.OnFragmentInteractionListener mListener;

    CompositeDisposable compositeDisposable;
    private View view;
    private Customer customer;
    private CardInputWidget mCardInputWidget;
    private Context context;
    private String totalPayable;
    private String isEnglish;
    private String paperBagNeeded;
    static PaymentFragment fragment;

    public PaymentFragment() {
        // Required empty public constructor
    }

    public static PaymentFragment newInstance(String param1, String param2) {
        fragment = new PaymentFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((PaymentActivity)getActivity()).setActionBarTitle("付款");
        compositeDisposable = new CompositeDisposable();

        Bundle bundle = getArguments();
        customer = bundle.getParcelable("customer");
        totalPayable = String.valueOf(bundle.getDouble("totalPayable"));
        isEnglish = bundle.getString("language");
        paperBagNeeded = bundle.getString("paperBagRequired");

        if (getActivity() instanceof PaymentActivity) {
            if (isEnglish.equals("Yes")) {
                ((PaymentActivity) getActivity()).setActionBarTitle("Payment");
            } else {
                ((PaymentActivity) getActivity()).setActionBarTitle("付款");
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            String resultDisplayStr;
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

                // Never log a raw card number. Avoid displaying it, but if necessary use getFormattedCardNumber()
                resultDisplayStr = "Card Number: " + scanResult.getRedactedCardNumber() + "\n";

                if (scanResult.isExpiryValid()) {
                    resultDisplayStr += "Expiration Date: " + scanResult.expiryMonth + "/" + scanResult.expiryYear + "\n";
                }

                if (scanResult.cvv != null) {
                    resultDisplayStr += "CVV has " + scanResult.cvv.length() + " digits.\n";
                }

            }
            else {
                resultDisplayStr = "Scan was cancelled.";
            }
            System.out.println("resultDisplayStr" + resultDisplayStr);

            Intent intent = new Intent(getActivity().getBaseContext(), ScanActivity.class);
            intent.putExtra("language", isEnglish);
            getActivity().startActivity(intent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_payment, container, false);
        Button scan = view.findViewById(R.id.onScanPress);
        scan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent scanIntent = new Intent(getActivity(), CardIOActivity.class);
                scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false); // default: false
                scanIntent.putExtra(CardIOActivity.EXTRA_SCAN_EXPIRY, true);
                startActivityForResult(scanIntent, 1);
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            mListener = (PaymentFragment.OnFragmentInteractionListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
