package com.limkee.order;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.limkee.R;
import com.limkee.constant.HttpConstant;
import com.limkee.constant.PostData;
import com.limkee.dao.OrderDAO;
import com.limkee.entity.Customer;
import com.limkee.entity.Order;
import com.limkee.navigation.NavigationActivity;

import java.util.ArrayList;

import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class OrderHistoryFragment extends Fragment {

    private OrderHistoryFragment.OnFragmentInteractionListener mListener;
    private OrderHistoryAdapter mAdapter;
    private View view;
    private RecyclerView recyclerView;
    private Customer customer;
    private String companyCode;
    public static Retrofit retrofit;
    private String isEnglish;
    private Button goToCancelledOrders;
    private Bundle myBundle = new Bundle();
    TextView lbl_noOrders;

    public OrderHistoryFragment(){}

    public static OrderHistoryFragment newInstance() {
        OrderHistoryFragment fragment = new OrderHistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        customer = bundle.getParcelable("customer");
        companyCode = customer.getCompanyCode();
        isEnglish = bundle.getString("language");

        myBundle.putParcelable("customer", customer);
        myBundle.putString("language", isEnglish);

        if (isEnglish.equals("Yes")){
            ((NavigationActivity)getActivity()).setActionBarTitle("Order History");
        } else {
            ((NavigationActivity)getActivity()).setActionBarTitle("订单历史");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_order_history, container, false);

        TextView lbl_orderIDHeader, lbl_numItemsHeader, lbl_deliveryDateHeader;

        lbl_orderIDHeader = (TextView) view.findViewById(R.id.lbl_orderIDHeader);
        lbl_deliveryDateHeader = (TextView) view.findViewById(R.id.lbl_deliveryDateHeader);
        lbl_numItemsHeader = (TextView) view.findViewById(R.id.lbl_numItemsHeader);
        goToCancelledOrders = (Button) view.findViewById(R.id.btnCancelledOrders);

        goToCancelledOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View rootView) {
                Intent intent = new Intent(view.getContext(), CancelledOrderActivity.class);
                intent.putExtra("customer", customer);
                intent.putExtra("language", isEnglish);
                getActivity().startActivity(intent);
            }
        });

        if (isEnglish.equals("Yes")) {
            lbl_orderIDHeader.setText("Order ID");
            lbl_deliveryDateHeader.setText("Delivery Date");
            lbl_numItemsHeader.setText("Total No.");
        }

        recyclerView = view.findViewById(R.id.orderHistoryRecyclerView);
        recyclerView = (RecyclerView) view.findViewById(R.id.orderHistoryRecyclerView);
        mAdapter = new OrderHistoryAdapter(this, OrderDAO.historyOrdersList, customer, isEnglish);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdapter);
        doGetOrderHistory(companyCode);

        return view;
    }

    private void doGetOrderHistory(String companyCode) {

        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(HttpConstant.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        PostData service = retrofit.create(PostData.class);
        Call<ArrayList<Order>> call = service.getOrderHistory(companyCode);
        call.enqueue(new Callback<ArrayList<Order>>() {

            @Override
            public void onResponse(Call<ArrayList<Order>> call, Response<ArrayList<Order>> response) {
                ArrayList<Order> data = response.body();
                OrderDAO.historyOrdersList = data;

                mAdapter.update(OrderDAO.historyOrdersList);

                if (data.size() == 0) {
                    if (isEnglish.equals("Yes")) {
                        lbl_noOrders = view.findViewById(R.id.lbl_noOrders);
                        view.findViewById(R.id.lbl_noOrders).setVisibility(View.VISIBLE);
                        lbl_noOrders.setText("No order history");
                    } else {
                        lbl_noOrders = view.findViewById(R.id.lbl_noOrders);
                        lbl_noOrders.setText("没有历史订单");
                        view.findViewById(R.id.lbl_noOrders).setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Order>> call, Throwable t) {
                System.out.println(t.getMessage());
            }
        });

    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NavigationActivity) {
            mListener = (OrderHistoryFragment.OnFragmentInteractionListener) context;
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
