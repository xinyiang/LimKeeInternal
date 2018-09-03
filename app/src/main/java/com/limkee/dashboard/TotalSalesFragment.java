package com.limkee.dashboard;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.limkee.R;
import com.limkee.constant.HttpConstant;
import com.limkee.constant.PostData;
import com.limkee.entity.Customer;
import com.limkee.order.CancelledOrderFragment;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TotalSalesFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private TotalSalesFragment.OnFragmentInteractionListener mListener;
    private View view;
    private Customer customer;
    public static Retrofit retrofit;
    private  String isEnglish;
    private Spinner spinner;
    static TotalSalesFragment fragment;
    private static final String[] years = {"Year","2016","2017","2018"};
    private String selectedYear = "";
    private ArrayList<String> custmonth = new ArrayList<>();
    private ArrayList<String> othermonth = new ArrayList<>();
    private ArrayList<Float> amounts = new ArrayList<>();
    private ArrayList<Float> avgSales = new ArrayList<>();

    public TotalSalesFragment(){}

    public static TotalSalesFragment newInstance() {
        fragment = new TotalSalesFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_total_sales, container, false);
        spinner = (Spinner)view.findViewById(R.id.spinner1);
        ArrayAdapter<String>adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, years );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(fragment);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                selectedYear = arg0.getItemAtPosition(position).toString();
                doGetCustomerSales(customer.getCompanyCode(), selectedYear);
                doGetAverageSales(selectedYear);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        return view;
    }

    public void showChart(ArrayList<String> custmonth, ArrayList<Float> amounts, String color) {
        HorizontalBarChart chart = view.findViewById(R.id.chart);
        try {
            //chart.setScaleEnabled(false);
            chart.setDoubleTapToZoomEnabled(false);
            //chart.setFitBars(false);
            BarDataSet set1 = new BarDataSet(getDataSet(amounts), "Total amount of each item sold");
            set1.setColors(Color.parseColor(color));
            set1.setValueTextSize(15f);

            BarData data = new BarData(set1);
            data.setValueFormatter(new ValueFormatter());
            IAxisValueFormatter axisFormatter = new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return "" + ((int) value);
                }
            };
            data.setBarWidth(0.5f);

            YAxis left = chart.getAxisLeft();
            left.setValueFormatter(axisFormatter);
            left.setGranularity(1f);
            left.setTextSize(15f);
            left.setAxisMinimum(0f);

            YAxis right = chart.getAxisRight();
            right.setDrawLabels(false);
            right.setDrawGridLines(false);

            // X-axis labels
            String[] values = custmonth.toArray(new String[custmonth.size()]);
            System.out.println("Items are " + values[0]);
            XAxis xAxis = chart.getXAxis();
            xAxis.setValueFormatter(new MyXAxisValueFormatter(values));
            xAxis.setGranularity(1f);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setTextSize(15f);
            xAxis.setAxisMaximum(amounts.size() - 0.5f);
            xAxis.setAxisMinimum(0.5f);
            xAxis.setLabelRotationAngle(-15);

            chart.setData(data);

            Description description = new Description();
            description.setText("");
            description.setTextSize(15);
            chart.setDescription(description);

            chart.getLegend().setEnabled(true);
            chart.getLegend().setTextSize(15f);
            chart.animateY(1000);
            chart.invalidate();

            //chart.setVisibleYRangeMaximum(300, YAxis.AxisDependency.LEFT);
            chart.setVisibleXRangeMaximum(5);
            chart.setVisibleXRangeMinimum(5);
            chart.moveViewTo(amounts.size() - 1, 0, YAxis.AxisDependency.LEFT);
        }catch (Exception e){
            chart.setData(null);
            chart.invalidate();
        }
    }

    public class MyXAxisValueFormatter implements IAxisValueFormatter {
        private String[] mValues;

        public MyXAxisValueFormatter(String[] values) {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            if (mValues.length == 0) {
                return "";
            } else {
                if ((int) value < mValues.length) {
                    return mValues[(int) value];
                }
                return "";
            }
        }
    }

    private ArrayList<BarEntry> getDataSet(ArrayList<Float> floats) {
        ArrayList<BarEntry> valueSet1 = new ArrayList<>();
        for (int i = 0; i < floats.size(); i++) {
            BarEntry v1e1 = new BarEntry(i, floats.get(i));
            valueSet1.add(v1e1);
        }
        return valueSet1;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isEnglish.equals("Yes")) {
        } else {
        }
    }

    private void doGetCustomerSales(String companyCode, String selectedYear) {
        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(HttpConstant.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        PostData service = retrofit.create(PostData.class);
        Call<Map<Integer,Double>> call = service.getFilteredCustomerSales(companyCode, selectedYear);
        call.enqueue(new Callback<Map<Integer,Double>>() {
            @Override
            public void onResponse(Call<Map<Integer,Double>> call, Response<Map<Integer,Double>> response) {
                Map<Integer,Double> data = response.body();

                custmonth = new ArrayList<>();
                amounts = new ArrayList<>();

                if (data.size() == 0) {
                    showChart(custmonth, amounts, "#F78B5D");
                } else {
                    Iterator entries = data.entrySet().iterator();
                    while (entries.hasNext()) {
                        Map.Entry entry = (Map.Entry) entries.next();
                        int mth = (Integer)entry.getKey();
                        double amt = data.get(mth);
                        System.out.println("month " + mth + " for customer have sales amt of $ " + amt);
                        custmonth.add(Integer.toString(mth));
                        amounts.add((float) amt);
                    }
                    showChart(custmonth,amounts, "#F78B5D");
                }
            }

            @Override
            public void onFailure(Call<Map<Integer,Double>> call, Throwable t) {
                System.out.println("error " + t.getMessage());
            }
        });
    }

    private void doGetAverageSales(String selectedYear) {
        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(HttpConstant.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        PostData service = retrofit.create(PostData.class);
        Call<Map<Integer, Double>> call = service.getAverageSales(selectedYear);
        call.enqueue(new Callback<Map<Integer, Double>>() {
            @Override
            public void onResponse(Call<Map<Integer, Double>> call, Response<Map<Integer, Double>> response) {
                Map<Integer, Double> data = response.body();

                othermonth = new ArrayList<>();
                avgSales = new ArrayList<>();

                if (data.size() == 0) {
                    showChart(othermonth, avgSales, "#A0C25A");
                } else {
                    Iterator entries = data.entrySet().iterator();
                    while (entries.hasNext()) {
                        Map.Entry entry = (Map.Entry) entries.next();
                        int mth = (Integer) entry.getKey();
                        double amt = data.get(mth);
                        System.out.println("month " + mth + " average sales amt is $ " + amt);
                        othermonth.add(Integer.toString(mth));
                        avgSales.add((float) amt);
                    }
                    showChart(othermonth,avgSales, "#A0C25A");
                }
            }
            @Override
            public void onFailure(Call<Map<Integer, Double>> call, Throwable t) {
                System.out.println("error : " + t.getMessage());
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CancelledOrderFragment.OnFragmentInteractionListener) {
            mListener = (TotalSalesFragment.OnFragmentInteractionListener) context;
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
            case 0:
                // Whatever you want to happen when the first item gets selected
                break;
            case 1:
                // Whatever you want to happen when the second item gets selected
                break;
            case 2:
                // Whatever you want to happen when the thrid item gets selected
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}