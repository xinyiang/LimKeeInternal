package com.limkee1.order;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.limkee1.R;
import com.limkee1.entity.Customer;
import com.limkee1.entity.Product;
import com.limkee1.payment.PaymentActivity;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import io.reactivex.disposables.CompositeDisposable;

public class QuickReorderConfirmOrderFragment extends Fragment {

    private QuickReorderConfirmOrderFragment.OnFragmentInteractionListener mListener;
    private View view;
    private EditText deliveryDate;
    Calendar mCurrentDate;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private Button next;
    private TextView subtotalAmt;
    private double subtotal;
    private double taxAmt;
    private double totalPayable;
    private QuickReorderConfirmOrderAdapter mAdapter;
    private ArrayList<Product> orderList;
    private String isEnglish;
    private Customer customer;
    private String deliveryShift;
    CompositeDisposable compositeDisposable;
    private String ETADeliveryDate;
    private int day;
    private int month;
    private int year;
    private String cutoffTime;
    private String paperBagNeeded;

    public QuickReorderConfirmOrderFragment() {
        // Required empty public constructor
    }

    public static QuickReorderConfirmOrderFragment newInstance() {
        QuickReorderConfirmOrderFragment cf = new QuickReorderConfirmOrderFragment();
        Bundle args = new Bundle();
        cf.setArguments(args);
        return cf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();

        orderList = bundle.getParcelableArrayList("orderList");
        customer = bundle.getParcelable("customer");
        isEnglish = bundle.getString("language");
        deliveryShift = bundle.getString("deliveryShift");
        cutoffTime = bundle.getString("cutoffTime");
        if (getActivity() instanceof QuickReorderConfirmOrderActivity) {
            if (isEnglish.equals("Yes")) {
                ((QuickReorderConfirmOrderActivity) getActivity()).setActionBarTitle("Confirm Order");
            } else {
                ((QuickReorderConfirmOrderActivity) getActivity()).setActionBarTitle("确认订单");
            }
        }

        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_quick_reorder_confirm_order, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerView = view.findViewById(R.id.recyclerView);

        //if english, set label in english language
        TextView lblSubtotal = view.findViewById(R.id.lbl_subtotal_amt);
        TextView lblTax = view.findViewById(R.id.lbl_tax_amt);
        TextView lblFinalTotal = view.findViewById(R.id.lbl_total_amt);
        TextView lblPaperBagRequired = view.findViewById(R.id.lbl_paperBag);
        CheckBox paperBagRequired = view.findViewById(R.id.checkBox);

        if (isEnglish.equals("Yes")) {
            lblSubtotal.setText("Sub Total");
            lblTax.setText("7% GST");
            lblFinalTotal.setText("Total Amount");
            lblPaperBagRequired.setText("Paper Bag");
            paperBagRequired.setText("Yes");
        } else {
            lblSubtotal.setText("小计");
            lblTax.setText("7% 税");
            lblFinalTotal.setText("总额");
            lblPaperBagRequired.setText("纸袋");
            paperBagRequired.setText("需要");
        }

        recyclerView = (RecyclerView) view.findViewById(com.limkee1.R.id.recyclerView);
        mAdapter = new QuickReorderConfirmOrderAdapter(this, orderList, isEnglish);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdapter);
        // recyclerView.setNestedScrollingEnabled(false);
        // mAdapter.notifyDataSetChanged();

        new CountDownTimer(400, 100) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }.start();

        subtotal = calculateSubtotal(orderList);
        DecimalFormat df = new DecimalFormat("#0.00");

        subtotalAmt = view.findViewById(R.id.subtotalAmt);
        subtotalAmt.setText("$" + df.format(subtotal));

        TextView tax = view.findViewById(R.id.taxAmt);
        taxAmt = subtotal * 0.07;
        tax.setText("$" + df.format(taxAmt));

        TextView totalAmt = view.findViewById(R.id.totalAmt);
        totalPayable = taxAmt + subtotal;
        totalAmt.setText("$" + df.format(totalPayable));

        //if english, change label to english
        if (isEnglish.equals("Yes")) {
            TextView lbl_subtotal_amt, lbl_total_amt, lbl_tax_amt;
            TextView lbl_delivery_details, lbl_name, lbl_contact, lbl_companyName, lbl_date, lbl_time, lbl_item_details, lbl_amt_details;
            Button btnNext;
            lbl_subtotal_amt = (TextView) view.findViewById(R.id.lbl_subtotal_amt);
            lbl_total_amt = (TextView) view.findViewById(R.id.lbl_total_amt);
            lbl_tax_amt = (TextView) view.findViewById(R.id.lbl_tax_amt);
            lbl_delivery_details = (TextView) view.findViewById(R.id.lbl_deliveryDetails);
            //lbl_name = (TextView) view.findViewById(R.id.lbl_name);
            lbl_contact = (TextView) view.findViewById(R.id.lbl_phone);
            lbl_companyName = (TextView) view.findViewById(R.id.lbl_companyName);
            lbl_date = (TextView) view.findViewById(R.id.lbl_date);
            //lbl_time = (TextView) view.findViewById(R.id.lbl_time);
            lbl_item_details = (TextView) view.findViewById(R.id.lbl_items);
            lbl_amt_details = (TextView) view.findViewById(R.id.lbl_amountDetails);
            btnNext = (Button) view.findViewById(R.id.btnPlaceOrder);

            lbl_subtotal_amt.setText("Sub Total");
            lbl_tax_amt.setText("Add 7% GST");
            lbl_total_amt.setText("Total Amount");
            lbl_delivery_details.setText("Delivery Details");
            //lbl_name.setText("Name");
            lbl_contact.setText("Contact No");
            lbl_companyName.setText("Company Name");
            lbl_date.setText("Date");
            //lbl_time.setText("Time");
            lbl_item_details.setText(" " + orderList.size() + "items");
            lbl_amt_details.setText("Amount details");
            btnNext.setText("Place Order");
        }

        //display delivery details data
        TextView deliveryDetails, name, contact, address, company, numItems, amtDetails;
        EditText deliveryDate;
        Button placeOrder;
        deliveryDetails = (TextView) view.findViewById(R.id.lbl_deliveryDetails);
        name = (TextView) view.findViewById(R.id.name);
        contact = (TextView) view.findViewById(R.id.phone);
        address = (TextView) view.findViewById(R.id.address);
        deliveryDate = (EditText) view.findViewById(R.id.date);
        numItems = (TextView) view.findViewById(R.id.lbl_items);
        amtDetails = (TextView) view.findViewById(R.id.lbl_amountDetails);
        company = (TextView) view.findViewById(R.id.companyName);
        placeOrder = (Button) view.findViewById(R.id.btnPlaceOrder);


        //display customer details

        if (customer.getDeliveryContact2() == null || customer.getDeliveryContact2().length() == 0) {
            contact.setText(customer.getDeliveryContact());

        } else {
            contact.setText(customer.getDeliveryContact() + " ," + customer.getDeliveryContact2());
        }

        company.setText(customer.getCompanyName());


        /*
        name.setText(customer.getDebtorName());

        String address2 = "";
        String address3 = "";
        String address4 = "";
        if (customer.getDeliverAddr2() == null || customer.getDeliverAddr2().length() == 0) {
            address2 = "";
        } else {
            address2 = customer.getDeliverAddr2();
        }

        if (customer.getDeliverAddr3() == null || customer.getDeliverAddr3().length() == 0) {
            address3 = "";
        } else {
            address3 = customer.getDeliverAddr3();
        }


        if (customer.getDeliverAddr4() == null || customer.getDeliverAddr4().length() == 0) {
            address4 = "";
        } else {
            address4 = customer.getDeliverAddr4();
        }

        address.setText(customer.getDeliverAddr1() + " " + address2 + " " + address3 + " " + address4);
        */

        if (isEnglish.equals("Yes")) {
            deliveryDetails.setText(" Delivery details");

            if (orderList.size() == 1) {
                numItems.setText(" Product details (" + orderList.size() + " item)");
            } else {
                numItems.setText(" Product details (" + orderList.size() + " items)");
            }

            amtDetails.setText(" Amount details");
            deliveryDate.setText("DD/MM/YYYY");
            placeOrder.setText("Place Order");

        } else {
            deliveryDetails.setText(" 送货详情");
            numItems.setText(" 订单样品 (" + orderList.size() + " 样)");
            amtDetails.setText(" 价钱详情");
            deliveryDate.setText("日/月/年");
            placeOrder.setText("确认订单");
        }

        /*
        if (deliveryShift.equals("AM")) {
            deliveryTime.setText("4.30am - 6.30am");
        } else {
            deliveryTime.setText("7.50am - 12.30pm");
        }
        */

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        deliveryDate = (EditText) view.findViewById(R.id.date);

        //choose delivery date
        deliveryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCurrentDate = Calendar.getInstance();
                year = mCurrentDate.get(Calendar.YEAR);
                month = mCurrentDate.get(Calendar.MONTH);
                day = mCurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {

                        year = selectedYear;
                        month = selectedMonth;
                        day = selectedDay;

                        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
                        Date date = new Date(selectedYear, selectedMonth, selectedDay - 1);
                        String dayOfWeek = sdf.format(date);

                        if (dayOfWeek.equals("Sunday") || dayOfWeek.equals("Sun")) {
                            //show error msg for no delivery
                            if (isEnglish.equals("Yes")) {
                                new AlertDialog.Builder(view.getContext())
                                        .setMessage("There is no delivery on Sunday. Please choose another date.")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //finish();
                                                deliveryDate.setText("DD/MM/YYYY");
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(view.getContext())
                                        .setMessage("星期日没有送货，请选其他送货日期")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //finish();
                                                deliveryDate.setText("日/月/年");
                                            }
                                        })
                                        .show();
                            }

                        } else {

                            Calendar c = Calendar.getInstance();

                            // set the calendar to start of today
                            c.set(Calendar.HOUR_OF_DAY, 0);
                            c.set(Calendar.MINUTE, 0);
                            c.set(Calendar.SECOND, 0);
                            c.set(Calendar.MILLISECOND, 0);

                            //and get that as a Date
                            Date today = c.getTime();

                            //reuse the calendar to set user specified's delivery date
                            c.set(Calendar.YEAR, year);
                            c.set(Calendar.MONTH, month);
                            c.set(Calendar.DAY_OF_MONTH, day);
                            Date dateSpecified = c.getTime();

                            if (dateSpecified.before(today)) {

                                if (isEnglish.equals("Yes")) {

                                    new AlertDialog.Builder(view.getContext())
                                            .setMessage("Invalid delivery date! Please select another delivery date.")
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //finish();
                                                    deliveryDate.setText("DD/MM/YYYY");
                                                }
                                            })
                                            .show();
                                } else {
                                    new AlertDialog.Builder(view.getContext())
                                            .setMessage("送货日期错误, 请选送货日期")
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //finish();
                                                    deliveryDate.setText("日/月/年");
                                                }
                                            })
                                            .show();
                                }
                            }

                            SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd/MM/yyyy");
                            SimpleDateFormat expectedPattern = new SimpleDateFormat("dd/MM/yyyy");

                            //month index start from 0, so + 1 to get correct actual month number
                            selectedMonth += 1;

                            try {
                                Date datetime = expectedPattern.parse(selectedDay + "/" + selectedMonth + "/" + selectedYear);
                                String timestamp = formatter.format(datetime);
                                deliveryDate.setText(timestamp);
                            } catch (Exception e){
                                deliveryDate.setText(selectedDay + "/" + selectedMonth + "/" + selectedYear);
                            }

                            if (selectedMonth < 10){
                                ETADeliveryDate = selectedYear + "-0" + selectedMonth + "-" + selectedDay;
                            } else {
                                ETADeliveryDate = selectedYear + "-" + selectedMonth + "-" + selectedDay;
                            }

                            mCurrentDate.set(selectedDay, selectedMonth, selectedYear);
                        }

                    }
                }, year, month, day);
                mDatePicker.show();
            }
        });

        next = view.findViewById(R.id.btnPlaceOrder);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View rootView) {

                //check if delivery date is selected
                String deliveryDateText = deliveryDate.getText().toString();
                if (deliveryDateText.equals("DD/MM/YYYY") || deliveryDateText.equals("日/月/年")) {
                    if (isEnglish.equals("Yes")) {

                        new AlertDialog.Builder(view.getContext())
                                .setMessage("Please select delivery date")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //finish();
                                    }
                                })
                                .show();
                    } else {
                        new AlertDialog.Builder(view.getContext())
                                .setMessage("请选送货日期")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //finish();
                                    }
                                })
                                .show();
                    }

                } else {

                    //check if paper bag is required
                    CheckBox paperBagRequired = (CheckBox)view.findViewById(R.id.checkBox);
                    if (paperBagRequired.isChecked()) {
                        paperBagNeeded = "yes";
                    } else {
                        paperBagNeeded = "no";
                    }

                    //check if today's delivery is before cut off time
                    try{
                        Date currentTimestamp = new Date();

                        //cut off time stamp takes ETADeliveryDate
                        String deliveryDateTime = ETADeliveryDate + " " + cutoffTime;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                        Date cutoffTimestamp = sdf.parse(deliveryDateTime);

                        //compare delivery datetime is < cut off time
                        if (currentTimestamp.before(cutoffTimestamp)) {
                            System.out.println("delivery time before cut off");

                            //go to payment activity
                            Intent intent = new Intent(view.getContext(), PaymentActivity.class);
                            intent.putParcelableArrayListExtra("orderList", orderList);
                            intent.putExtra("customer", customer);
                            intent.putExtra("subtotal", subtotal);
                            intent.putExtra("taxAmt", taxAmt);
                            intent.putExtra("deliveryDate", ETADeliveryDate);
                            intent.putExtra("totalPayable", totalPayable);
                            intent.putExtra("language", isEnglish);
                            intent.putExtra("paperBagRequired", paperBagNeeded);
                            getActivity().startActivity(intent);
                        } else {
                            System.out.println("delivery time after cut off");
                            //show error message
                            if (isEnglish.equals("Yes")) {

                                new AlertDialog.Builder(view.getContext())
                                        .setMessage("Today's delivery is over. Please choose another delivery date.")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(view.getContext())
                                        .setMessage("今日送货已结束，请选其他送货日期")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //finish();
                                            }
                                        })
                                        .show();
                            }
                        }

                    } catch (ParseException e) {
                        System.out.println(e.getMessage());
                    }


                }
            }
        });
    }


    private double calculateSubtotal(ArrayList<Product> orderList) {
        double subtotal = 0;
        for (Product p : orderList) {
            int qty = p.getDefaultQty();
            subtotal += p.getUnitPrice() * qty;
        }
        return subtotal;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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