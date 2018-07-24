package com.limkee.payment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.limkee.BaseActivity;
import com.limkee.R;

import com.limkee.entity.Customer;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.view.CardMultilineWidget;
import com.stripe.android.Stripe;
import com.stripe.android.model.Token;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class PaymentActivity extends BaseActivity implements PaymentFragment.OnFragmentInteractionListener{
    private View rootView;
    private String totalPayable;
    private PaymentFragment paymentFragment = new PaymentFragment();
    public static Bundle myBundle = new Bundle();
    private String deliveryDate;
    private Context context;
    private ProgressBar progressBar;
    private EditText nameOnCard;
    private TextView errorNameOnCard;
    private Button payButton;
    private Button selectSavedCard;
    private CheckBox saveCard;
    private Customer customer;
    private CardMultilineWidget mCardMultilineWidget;
    private RadioOnClick radioOnClick = new RadioOnClick(0);
    public static Activity activity; //used to finish this activity in backgroundPayment activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        Toolbar toolbar = findViewById(com.limkee.R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("@string/payment");
        myBundle = getIntent().getExtras();


        activity = this;
        context = getApplicationContext();
        customer = myBundle.getParcelable("customer");
        deliveryDate = myBundle.getString("deliveryDate");
        double tp = myBundle.getDouble("totalPayable");
        String isEnglish = myBundle.getString("language");

        TextView tv = (TextView)findViewById(R.id.totalPayable);
        tv.setText(String.format("$%.2f", tp));

        totalPayable = String.valueOf((int) Math.round(tp * 100));
        Bundle bundle = new Bundle();
        //bundle.putString("totalPayable",totalPayable);

        selectSavedCard = (Button)findViewById(R.id.select_saved_card);
        selectSavedCard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selectSavedCard();
            }
        });

        payButton = (Button) findViewById(R.id.btnPlaceOrder);
        payButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pay("pay_with_new_card", null);
            }
        });
        bundle.putParcelable("customer", customer);
        bundle.putString("language", isEnglish);
        paymentFragment.setArguments(bundle);
        loadFragment(paymentFragment);

    }

    public void pay(String mType, String lastFourDigit){
        //validate credentials to login
        final String type = mType;
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (type.equals("pay_with_saved_card")){
            AlertDialog ad = new AlertDialog.Builder(PaymentActivity.this).setTitle(getResources().getString(R.string.key_in_cvc)).create();
            final EditText edittext = new EditText(PaymentActivity.this);
            ad.setView(edittext);
            ad.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.confirm), new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int whichButton){
                    String CVC = edittext.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);
                    BackgroundValidation bv = (BackgroundValidation) new BackgroundValidation(
                            new AsyncResponse() {
                                @Override
                                public void processFinish(String output) {
                                    if (output != null && output.equals("success")){
                                        BackgroundPayment bp = new BackgroundPayment(context, activity);
                                        bp.saveCustomer(customer);
                                        bp.saveDeliveryDate(deliveryDate);
                                        bp.execute(type, totalPayable, lastFourDigit);
                                    }else{
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(context, getResources().getString(R.string.cvc_error), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                    ).execute(customer.getDebtorCode(), lastFourDigit, CVC);
                }
            });
            ad.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.cancel();
                }
            });
            ad.show();

        }else{
            nameOnCard = (EditText) findViewById(R.id.nameOnCard);
            errorNameOnCard = (TextView) findViewById(R.id.errNameOnCard);
            mCardMultilineWidget = (CardMultilineWidget)findViewById(R.id.card_multiline_widget);
            saveCard = (CheckBox) findViewById(R.id.saveCard);
            final Card card = mCardMultilineWidget.getCard();

            if (card == null || nameOnCard.getText().toString().isEmpty()) {
                Toast.makeText(context,
                        getResources().getString(R.string.invalid_card),
                        Toast.LENGTH_LONG
                ).show();
                if (nameOnCard.getText().toString().isEmpty()){
                    errorNameOnCard.setVisibility(View.VISIBLE);
                    nameOnCard.setBackgroundResource(R.drawable.text_underline);
                }
                else {
                    errorNameOnCard.setVisibility(View.INVISIBLE);
                    nameOnCard.setBackgroundResource(android.R.drawable.edit_text);
                }
            } else {
                progressBar.setVisibility(View.VISIBLE);
                Stripe stripe = new Stripe(context, "pk_test_FPldW3NRDq68iu1drr2o7Anb");
                stripe.createToken(
                        card,
                        new TokenCallback() {
                            public void onSuccess(Token token) {
                                BackgroundPayment bp = new BackgroundPayment(context, activity);
                                if (saveCard.isChecked()){
                                    bp.saveCard(card);
                                    bp.saveCustomer(customer);
                                    bp.saveDeliveryDate(deliveryDate);
                                    //Send card details to db
                                }
                                bp.execute(type, totalPayable, token.getId());
                                // Send token to your server
                            }
                            public void onError(Exception error) {
                                // Show localized error message
                                Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
                            }
                        }
                );
            }
        }

//        new CountDownTimer(6000, 100) {
//
//            public void onTick(long millisUntilFinished) {
//
//            }
//
//            public void onFinish() {
//                progressBar.setVisibility(View.GONE);
//            }
//        }.start();
    }

    public View onCreate(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_payment, container, false);
        return rootView;
    }

    private void loadFragment(Fragment fragment) {
        //Change screen to option selected (using fragments)
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(com.limkee.R.id.flContent, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // Back button clicked
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    public void setActionBarTitle(String title){
        TextView titleTextView = findViewById(com.limkee.R.id.toolbar_title);
        if (titleTextView != null) {
            titleTextView.setText(title);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    public void selectSavedCard() {
        //get saved card
        GetJson getSavedCards = (GetJson) new GetJson(
                new AsyncResponse() {
                    @Override
                    public void processFinish(String output) {
                        if (output == null){
                            Toast.makeText(context, R.string.no_saved_card, Toast.LENGTH_SHORT).show();
                        }else{
                            try{
                                loadIntoAlertDialog(output);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ).execute(customer.getDebtorCode());
        //getSavedCards.execute(customer.getDebtorCode());
//        if (result == null){
//           Toast.makeText(context, "没有储存的卡", Toast.LENGTH_SHORT).show();
//        }else{
//            try{
//                loadIntoAlertDialog(result);
//            }catch (JSONException e){
//                e.printStackTrace();
//            }
//        }

    }

    private void loadIntoAlertDialog(String json) throws JSONException{
        JSONArray jsonArray = new JSONArray(json);
        String[] cards = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++){
            JSONObject obj = jsonArray.getJSONObject(i);
            cards[i] = "XXXX XXXX XXXX " + obj.getString("LastFourDigit");
        }

        AlertDialog ad = new AlertDialog.Builder(PaymentActivity.this).setTitle(getResources().getString(R.string.select_card))
                .setSingleChoiceItems(cards,radioOnClick.getIndex(),radioOnClick).create();
        ad.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.confirm), new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton){
                ListView lw = ((AlertDialog)dialog).getListView();
                Object selectedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
                String selectedCard = selectedItem.toString();

                pay("pay_with_saved_card", selectedCard.substring(selectedCard.length() - 4));
                }
        });
        ad.setButton(DialogInterface.BUTTON_NEUTRAL, getResources().getString(R.string.delete_card), new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton){
                ListView lw = ((AlertDialog)dialog).getListView();
                Object selectedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
                String selectedCard = selectedItem.toString();
                deleteCard(dialog, selectedCard.substring(selectedCard.length() - 4));
                }
        });
        ad.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
            }
        });
        ad.show();
    }

    protected void deleteCard(DialogInterface singleChoiceDialog, String lastFourDigit){
        AlertDialog ad = new AlertDialog.Builder(PaymentActivity.this).setMessage(getResources().getString(R.string.confirm_delete) + lastFourDigit).create();
        ad.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.confirm), new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int i){
                BackgroundDelete bd = (BackgroundDelete) new BackgroundDelete(
                        new AsyncResponse() {
                            @Override
                            public void processFinish(String output) {
                                selectSavedCard();
                            }
                        }
                ).execute(customer.getDebtorCode(), lastFourDigit);
            }
        });
        ad.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.cancel), new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int i){
                dialog.cancel();
            }
        });
        ad.show();
    }

    class RadioOnClick implements DialogInterface.OnClickListener{
        private int index;

        public RadioOnClick (int index) {
            this.index = index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
        public void onClick(DialogInterface dialog, int whichButton){
            setIndex(whichButton);
        }
    }
}