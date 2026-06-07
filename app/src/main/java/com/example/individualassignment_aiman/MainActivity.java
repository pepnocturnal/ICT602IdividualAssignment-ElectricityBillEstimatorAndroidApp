package com.example.individualassignment_aiman;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private Spinner  spinnerMonth;
    private SeekBar  seekBarRebate;
    private EditText etUnit;
    private TextView tvRebateLabel, tvTotalCharges, tvFinalCost;
    private Button   btnCalculate, btnViewRecords, btnAbout;

    private BillDatabaseHelper dbHelper;

    private static final double MIN_UNIT = 1.0;
    private static final double MAX_UNIT = 1000.0;

    private static final String[] MONTHS = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };


    private static final double RATE_BLOCK_1 = 0.218; // First 200 kWh
    private static final double RATE_BLOCK_2 = 0.334; // 201–300 kWh
    private static final double RATE_BLOCK_3 = 0.516; // 301–600 kWh
    private static final double RATE_BLOCK_4 = 0.546; // 601–1000 kWh


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        dbHelper = new BillDatabaseHelper(this);
        setupMonthSpinner();
        setupRebateSeekBar();
        setupButtonListeners();
    }

    private void bindViews() {
        spinnerMonth   = findViewById(R.id.spinnerMonth);
        seekBarRebate  = findViewById(R.id.seekBarRebate);
        tvRebateLabel  = findViewById(R.id.tvRebateLabel);
        etUnit         = findViewById(R.id.etUnit);
        tvTotalCharges = findViewById(R.id.tvTotalCharges);
        tvFinalCost    = findViewById(R.id.tvFinalCost);
        btnCalculate   = findViewById(R.id.btnCalculate);
        btnViewRecords = findViewById(R.id.btnViewRecords);
        btnAbout       = findViewById(R.id.btnAbout);
    }

    private void setupMonthSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, MONTHS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);
    }

    private void setupRebateSeekBar() {
        seekBarRebate.setMax(5);
        seekBarRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvRebateLabel.setText("Rebate: " + progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void setupButtonListeners() {
        btnCalculate.setOnClickListener(v -> onCalculateClicked());
        btnViewRecords.setOnClickListener(v ->
                startActivity(new Intent(this, ListActivity.class)));
        btnAbout.setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class)));
    }

    private void onCalculateClicked() {
        double unit = getValidatedUnit();
        if (unit < 0) return; // Validation failed; error already shown

        String month     = spinnerMonth.getSelectedItem().toString();
        double rebate    = seekBarRebate.getProgress();
        double total     = calculateTotalCharges(unit);
        double finalCost = applyRebate(total, rebate);

        tvTotalCharges.setText(String.format("Total Charges: RM %.2f", total));
        tvFinalCost.setText(String.format("Final Cost after Rebate: RM %.2f", finalCost));

        dbHelper.insertBill(month, unit, total, rebate, finalCost);
        Toast.makeText(this, "✅ Record saved successfully!", Toast.LENGTH_SHORT).show();
    }

    private double getValidatedUnit() {
        String unitStr = etUnit.getText().toString().trim();

        if (unitStr.isEmpty()) {
            etUnit.setError("Please enter the units used (kWh)");
            etUnit.requestFocus();
            return -1;
        }

        double unit;
        try {
            unit = Double.parseDouble(unitStr);
        } catch (NumberFormatException e) {
            etUnit.setError("Invalid number — please enter digits only");
            etUnit.requestFocus();
            return -1;
        }

        if (unit < MIN_UNIT || unit > MAX_UNIT) {
            etUnit.setError("Units must be between 1 and 1000 kWh");
            etUnit.requestFocus();
            return -1;
        }

        return unit;
    }


    public static double calculateTotalCharges(double unit) {
        if (unit <= 200) {
            return unit * RATE_BLOCK_1;
        } else if (unit <= 300) {
            return (200 * RATE_BLOCK_1)
                    + ((unit - 200) * RATE_BLOCK_2);
        } else if (unit <= 600) {
            return (200 * RATE_BLOCK_1)
                    + (100 * RATE_BLOCK_2)
                    + ((unit - 300) * RATE_BLOCK_3);
        } else {
            return (200 * RATE_BLOCK_1)
                    + (100 * RATE_BLOCK_2)
                    + (300 * RATE_BLOCK_3)
                    + ((unit - 600) * RATE_BLOCK_4);
        }
    }

    private double applyRebate(double total, double rebatePercent) {
        return total - (total * rebatePercent / 100.0);
    }
}