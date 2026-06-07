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
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerMonth;
    SeekBar seekBarRebate;
    EditText etUnit;
    TextView tvRebateLabel, tvTotalCharges, tvFinalCost;
    Button btnCalculate, btnViewRecords, btnAbout;
    BillDatabaseHelper dbHelper;

    String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerMonth   = findViewById(R.id.spinnerMonth);
        seekBarRebate  = findViewById(R.id.seekBarRebate);
        tvRebateLabel  = findViewById(R.id.tvRebateLabel);
        etUnit         = findViewById(R.id.etUnit);
        tvTotalCharges = findViewById(R.id.tvTotalCharges);
        tvFinalCost    = findViewById(R.id.tvFinalCost);
        btnCalculate   = findViewById(R.id.btnCalculate);
        btnViewRecords = findViewById(R.id.btnViewRecords);
        btnAbout       = findViewById(R.id.btnAbout);

        dbHelper = new BillDatabaseHelper(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        seekBarRebate.setMax(5);
        seekBarRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvRebateLabel.setText(String.format(Locale.getDefault(), "Rebate: %d%%", progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnCalculate.setOnClickListener(v -> calculate());

        btnViewRecords.setOnClickListener(v ->
                startActivity(new Intent(this, ListActivity.class)));

        btnAbout.setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class)));
    }

    private void calculate() {
        String unitStr = etUnit.getText().toString().trim();

        if (unitStr.isEmpty()) {
            etUnit.setError("Please enter the units used (kWh)");
            etUnit.requestFocus();
            return;
        }

        double unit;
        try {
            unit = Double.parseDouble(unitStr);
        } catch (NumberFormatException e) {
            etUnit.setError("Invalid number, please enter digits only");
            etUnit.requestFocus();
            return;
        }

        if (unit < 1 || unit > 1000) {
            etUnit.setError("Units must be between 1 and 1000 kWh");
            etUnit.requestFocus();
            return;
        }

        String month  = spinnerMonth.getSelectedItem().toString();
        double rebate = seekBarRebate.getProgress();
        double total  = calculateTotal(unit);
        double finalCost = total - (total * rebate / 100);

        tvTotalCharges.setText(String.format(Locale.getDefault(), "Total Charges: RM %.2f", total));
        tvFinalCost.setText(String.format(Locale.getDefault(), "Final Cost after Rebate: RM %.2f", finalCost));

        dbHelper.insertBill(month, unit, total, rebate, finalCost);
        Toast.makeText(this, "Record saved!", Toast.LENGTH_SHORT).show();
    }

    private double calculateTotal(double unit) {
        if (unit <= 200) {
            return unit * 0.218;
        } else if (unit <= 300) {
            return (200 * 0.218) + ((unit - 200) * 0.334);
        } else if (unit <= 600) {
            return (200 * 0.218) + (100 * 0.334) + ((unit - 300) * 0.516);
        } else {
            return (200 * 0.218) + (100 * 0.334) + (300 * 0.516) + ((unit - 600) * 0.546);
        }
    }
}