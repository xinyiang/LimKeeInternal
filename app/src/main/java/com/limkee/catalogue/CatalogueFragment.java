package com.limkee.catalogue;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.limkee.R;
import com.limkee.constant.HttpConstant;
import com.limkee.constant.PostData;
import com.limkee.dao.CatalogueDAO;
import com.limkee.entity.Customer;
import com.limkee.entity.Product;
import com.limkee.navigation.NavigationActivity;
import com.limkee.order.ConfirmOrderActivity;
import android.support.annotation.Nullable;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CatalogueFragment extends Fragment {
    private CatalogueFragment.OnFragmentInteractionListener mListener;
    public static View view;
    private CatalogueAdapter mAdapter;
    private ProgressBar progressBar;
    public static RecyclerView recyclerView;
    public static Button confirmOrder;
    public static TextView subtotalAmt;
    public static TextView lbl_subtotal;
    public static double subtotal;
    public static ArrayList<Product> tempOrderList = new ArrayList<>();
    private String isEnglish;
    private AlertDialog.Builder builder;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    public static Retrofit retrofit;
    private Customer customer;
    private String deliveryShift;
    private static CatalogueFragment fragment;

    public CatalogueFragment(){
    }

    public static CatalogueFragment newInstance() {
        fragment = new CatalogueFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        isEnglish = bundle.getString("language");
        customer = bundle.getParcelable("customer");
        deliveryShift = bundle.getString("deliveryShift");

        if (isEnglish.equals("Yes")){
            ((NavigationActivity)getActivity()).setActionBarTitle("Order Slip");
        } else {
            ((NavigationActivity)getActivity()).setActionBarTitle("订单表");
        }

        builder= new AlertDialog.Builder(getContext());
        loginPreferences = getContext().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();
        String cutoffTime = loginPreferences.getString("cutofftime", "");
        loginPrefsEditor.commit();

        //format cut off time to remove seconds
        if(isEnglish.equals("Yes")){
            builder.setMessage("Please place order before " + cutoffTime.substring(0,cutoffTime.length()-3) + " AM for today's delivery");
        } else {
            builder.setMessage("今日订单请在" + getChineseTime(cutoffTime.substring(0,cutoffTime.length()-3)) + "前下单");
        }
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();
               // doGetCatalogue();
            }
        });
        final AlertDialog ad = builder.create();
        ad.show();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_catalogue, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerView = view.findViewById(R.id.recyclerView);
        subtotalAmt = view.findViewById(R.id.subtotalAmt);
        confirmOrder = (Button) view.findViewById(R.id.btnNext);
        lbl_subtotal = (TextView) view.findViewById(R.id.lblSubtotalAmt);

        if (isEnglish.equals("Yes")) {
            lbl_subtotal.setText("Sub Total");
            confirmOrder.setText("Next");
        } else {
            lbl_subtotal.setText("小计");
            confirmOrder.setText("下订单");
        }

        mAdapter = new CatalogueAdapter(this, isEnglish);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        new CountDownTimer(400, 100) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }.start();

        doGetCatalogue();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //proceed to confirm order
        confirmOrder = (Button) view.findViewById(R.id.btnNext);
        confirmOrder.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                recyclerView.setItemViewCacheSize(tempOrderList.size());

                tempOrderList = CatalogueAdapter.getOrderList();
                final ArrayList <Product> orderList = new ArrayList<>();

                //remove products that has 0 quantity
                for (Product p : tempOrderList){
                    if (p.getDefaultQty() != 0) {
                        orderList.add(p);
                    }
                }

                //check if subtotal hits minimum requirements
                if(calculateSubtotal(orderList) < 30){
                    if (isEnglish.equals("Yes")){
                        new AlertDialog.Builder(getContext())
                                .setMessage("Minimum order is $30.00.")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //finish();
                                    }
                                })
                                .show();
                    } else {
                        new AlertDialog.Builder(getContext())
                                .setMessage("订单总额最少要 $30.00")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //finish();
                                    }
                                })
                                .show();
                    }
                } else {
                    DecimalFormat df = new DecimalFormat("#0.00");
                    subtotalAmt = view.findViewById(R.id.subtotalAmt);
                    subtotal =  calculateSubtotal(orderList);
                    subtotalAmt.setText("$" + df.format(subtotal));

                    // //updateSubtotal(orderList);
                    CatalogueDAO.order_list = orderList;

                    //store all products with qty > 1 into a temporary arraylist of products
                    Intent intent = new Intent(view.getContext(), ConfirmOrderActivity.class);
                    intent.putParcelableArrayListExtra("orderList", orderList);
                    intent.putExtra("language",isEnglish);
                    intent.putExtra("orderList",orderList);
                    intent.putExtra("customer", customer);
                    intent.putExtra("deliveryShift", deliveryShift);
                    getActivity().startActivity(intent);

                }
                }
        });
    }

    public static String getChineseTime(String time){
        String minutes = time.substring(3,time.length());
        String chineseHour = "";
        String chineseTime;

        time = time.substring(0,2);
        //check hour
        if (time.equals("04")){
            chineseHour = "四";
        }  else if (time.equals("05")){
            chineseHour = "五";
        } else if (time.equals("06")){
            chineseHour = "六";
        } else if (time.equals("07")){
            chineseHour = "七";
        } else if (time.equals("08")){
            chineseHour = "八";
        } else if (time.equals("09")){
            chineseHour = "九";
        } else if (time.equals("10")) {
            chineseHour = "十";
        } else {
            chineseHour = "";
        }

        //check if got mins
        if (minutes.equals("00")){
            chineseTime = chineseHour + "点";
        } else if (minutes.equals("30")){
            chineseTime = chineseHour + "点半";
        } else{
            chineseTime = chineseHour + "点" + getNumber(minutes) + "分";
        }
        return chineseTime;
    }

    public static String getNumber(String number){
        String chineseNumber = "";

        if (number.equals("05")){
            chineseNumber = "零五";
        } else if (number.equals("10")){
            chineseNumber = "十";
        } else if (number.equals("15")){
            chineseNumber = "十五";
        } else if (number.equals("20")){
            chineseNumber = "二十";
        } else if (number.equals("25")){
            chineseNumber = "二十五";
        } else if (number.equals("35")){
            chineseNumber = "三十五";
        } else if (number.equals("40")){
            chineseNumber = "四十";
        } else if (number.equals("45")){
            chineseNumber = "四十五";
        } else if (number.equals("50")){
            chineseNumber = "五十";
        } else if (number.equals("55")){
            chineseNumber = "五十五";
        }  else {
            chineseNumber = "零";
        }
        return chineseNumber;
    }

    private void doGetCatalogue() {
        if (retrofit == null) {

            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(HttpConstant.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        PostData service = retrofit.create(PostData.class);
        Call<ArrayList<Product>> call = service.getCatalogue();
        call.enqueue(new Callback<ArrayList<Product>>() {

            @Override
            public void onResponse(Call<ArrayList<Product>> call, Response<ArrayList<Product>> response) {
                ArrayList<Product> data = response.body();
                CatalogueDAO.catalogue_list = data;

                recyclerView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                recyclerView = (RecyclerView) view.findViewById(com.limkee.R.id.recyclerView);

                //by default, let order list be the same as catalogue. if there is any change in qty, it will be updated.
                tempOrderList = CatalogueDAO.catalogue_list;
                String[] qtyDataSet= new String[tempOrderList.size()];
                for (int i = 0; i <tempOrderList.size(); i++) {
                    Product p = tempOrderList.get(i);
                    qtyDataSet[i] = Integer.toString(p.getDefaultQty());
                }

                mAdapter.update(qtyDataSet, CatalogueDAO.catalogue_list, tempOrderList);
                recyclerView.setItemViewCacheSize(qtyDataSet.length);


                DecimalFormat df = new DecimalFormat("#0.00");
                subtotalAmt.setText("$" + df.format(calculateSubtotal(CatalogueDAO.catalogue_list)));


            }

            @Override
            public void onFailure(Call<ArrayList<Product>> call, Throwable t) {
                System.out.println(t.getMessage());
            }
        });

    }

    private double calculateSubtotal(ArrayList<Product> orderList) {
        double subtotal = 0;
        for(Product p : orderList) {
            int qty = p.getDefaultQty();
            subtotal += p.getUnitPrice() * qty;
        }
        return subtotal;
    }

    public static void updateSubtotal(ArrayList<Product> orderList) {
        double subtotal = 0;
        for(Product p : orderList) {
            int qty = p.getDefaultQty();
            subtotal += p.getUnitPrice() * qty;
        }

        DecimalFormat df = new DecimalFormat("#0.00");
        subtotalAmt = view.findViewById(R.id.subtotalAmt);
        subtotalAmt.setText("$" + df.format(subtotal));
        recyclerView.setItemViewCacheSize(orderList.size());
    }

        @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NavigationActivity) {
            mListener = (CatalogueFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

}
