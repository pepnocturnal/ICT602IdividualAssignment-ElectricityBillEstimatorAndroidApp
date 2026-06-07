package com.example.individualassignment_aiman;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    TextView tvMonth, tvUnit, tvTotal, tvRebate, tvFinal;
    Button btnEdit, btnDelete;
    BillDatabaseHelper dbHelper;
    int billId;

    String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        tvMonth  = findViewById(R.id.tvMonth);
        tvUnit   = findViewById(R.id.tvUnit);
        tvTotal  = findViewById(R.id.tvTotal);
        tvRebate = findViewById(R.id.tvRebate);
        tvFinal  = findViewById(R.id.tvFinal);
        btnEdit  = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        dbHelper = new BillDatabaseHelper(this);
        billId = getIntent().getIntExtra("bill_id", -1);

        loadDetail();

        btnDelete.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Delete Record")
                        .setMessage("Are you sure you want to delete this record?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            dbHelper.deleteBill(billId);
                            Toast.makeText(this, "Record deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .setNegativeButton("No", null)
                        .show()
        );

        btnEdit.setOnClickListener(v -> showEditDialog());
    }

    private void loadDetail() {
        Cursor c = dbHelper.getBillById(billId);
        if (c.moveToFirst()) {
            tvMonth.setText("Month: " + c.getString(c.getColumnIndexOrThrow(BillDatabaseHelper.COL_MONTH)));
            tvUnit.setText("Units: " + c.getDouble(c.getColumnIndexOrThrow(BillDatabaseHelper.COL_UNIT)) + " kWh");
            tvTotal.setText("Total Charges: RM " + String.format(Locale.getDefault(), "%.2f",
                    c.getDouble(c.getColumnIndexOrThrow(BillDatabaseHelper.COL_TOTAL))));
            tvRebate.setText("Rebate: " + (int) c.getDouble(c.getColumnIndexOrThrow(BillDatabaseHelper.COL_REBATE)) + "%");
            tvFinal.setText("Final Cost: RM " + String.format(Locale.getDefault(), "%.2f",
                    c.getDouble(c.getColumnIndexOrThrow(BillDatabaseHelper.COL_FINAL))));
        }
        c.close();
    }

    private void showEditDialog() {
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_edit, null);

        Spinner spinnerMonth = view.findViewById(R.id.spinnerEditMonth);
        EditText etUnit      = view.findViewById(R.id.etEditUnit);
        SeekBar sbRebate     = view.findViewById(R.id.sbEditRebate);
        TextView tvRebLbl    = view.findViewById(R.id.tvEditRebate);

        // Set up month spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        // Pre-select the current month
        Cursor c = dbHelper.getBillById(billId);
        if (c.moveToFirst()) {
            String currentMonth = c.getString(c.getColumnIndexOrThrow(BillDatabaseHelper.COL_MONTH));
            for (int i = 0; i < months.length; i++) {
                if (months[i].equals(currentMonth)) {
                    spinnerMonth.setSelection(i);
                    break;
                }
            }
        }
        c.close();

        sbRebate.setMax(5);
        sbRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                tvRebLbl.setText(String.format(Locale.getDefault(), "Rebate: %d%%", progress));
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        new AlertDialog.Builder(this)
                .setTitle("Edit Record")
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    String unitStr = etUnit.getText().toString().trim();

                    if (unitStr.isEmpty()) {
                        Toast.makeText(this, "Please enter units", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double unit;
                    try {
                        unit = Double.parseDouble(unitStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (unit < 1 || unit > 1000) {
                        Toast.makeText(this, "Units must be between 1 and 1000", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String month = spinnerMonth.getSelectedItem().toString();
                    double reb   = sbRebate.getProgress();
                    double total = calculateTotal(unit);
                    double fin   = total - (total * reb / 100);

                    dbHelper.updateBill(billId, month, unit, total, reb, fin);
                    Toast.makeText(this, "Record updated!", Toast.LENGTH_SHORT).show();
                    loadDetail();
                })
                .setNegativeButton("Cancel", null)
                .show();
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