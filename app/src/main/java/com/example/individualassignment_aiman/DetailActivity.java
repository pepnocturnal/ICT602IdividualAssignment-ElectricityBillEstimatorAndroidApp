package com.example.individualassignment_aiman;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    private TextView tvMonth, tvUnit, tvTotal, tvRebate, tvFinal;
    private Button   btnEdit, btnDelete;

    private BillDatabaseHelper dbHelper;
    private int billId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        bindViews();
        dbHelper = new BillDatabaseHelper(this);
        billId   = getIntent().getIntExtra("bill_id", -1);

        if (billId == -1) {
            Toast.makeText(this, "Error: Record not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadBillDetail();
        setupButtonListeners();
    }


    private void bindViews() {
        tvMonth   = findViewById(R.id.tvMonth);
        tvUnit    = findViewById(R.id.tvUnit);
        tvTotal   = findViewById(R.id.tvTotal);
        tvRebate  = findViewById(R.id.tvRebate);
        tvFinal   = findViewById(R.id.tvFinal);
        btnEdit   = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void setupButtonListeners() {
        btnDelete.setOnClickListener(v -> confirmDelete());
        btnEdit.setOnClickListener(v   -> showEditDialog());
    }

    private void loadBillDetail() {
        Cursor c = dbHelper.getBillById(billId);
        if (c.moveToFirst()) {
            tvMonth.setText("Month:  " + c.getString(
                    c.getColumnIndexOrThrow(BillDatabaseHelper.COL_MONTH)));
            tvUnit.setText("Units Used:  " + c.getDouble(
                    c.getColumnIndexOrThrow(BillDatabaseHelper.COL_UNIT)) + " kWh");
            tvTotal.setText("Total Charges:  RM " + String.format("%.2f",
                    c.getDouble(c.getColumnIndexOrThrow(BillDatabaseHelper.COL_TOTAL))));
            tvRebate.setText("Rebate Applied:  " + (int) c.getDouble(
                    c.getColumnIndexOrThrow(BillDatabaseHelper.COL_REBATE)) + "%");
            tvFinal.setText("Final Cost:  RM " + String.format("%.2f",
                    c.getDouble(c.getColumnIndexOrThrow(BillDatabaseHelper.COL_FINAL))));
        }
        c.close();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Record")
                .setMessage("Are you sure you want to permanently delete this record? This cannot be undone.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteBill(billId);
                    Toast.makeText(this, "Record deleted", Toast.LENGTH_SHORT).show();
                    finish(); // Return to ListActivity
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog() {
        android.view.View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_edit, null);

        EditText etUnit   = dialogView.findViewById(R.id.etEditUnit);
        SeekBar  sbRebate = dialogView.findViewById(R.id.sbEditRebate);
        TextView tvRebLbl = dialogView.findViewById(R.id.tvEditRebate);

        sbRebate.setMax(5);
        sbRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                tvRebLbl.setText("Rebate: " + progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar sb) { }
            @Override public void onStopTrackingTouch(SeekBar sb) { }
        });

        new AlertDialog.Builder(this)
                .setTitle("Edit Record")
                .setView(dialogView)
                .setPositiveButton("Save Changes", (dialog, which) -> saveEdits(etUnit, sbRebate))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveEdits(EditText etUnit, SeekBar sbRebate) {
        String unitStr = etUnit.getText().toString().trim();

        if (unitStr.isEmpty()) {
            Toast.makeText(this, "Please enter the units used", Toast.LENGTH_SHORT).show();
            return;
        }

        double unit;
        try {
            unit = Double.parseDouble(unitStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number entered", Toast.LENGTH_SHORT).show();
            return;
        }

        if (unit < 1 || unit > 1000) {
            Toast.makeText(this, "Units must be between 1 and 1000 kWh", Toast.LENGTH_SHORT).show();
            return;
        }

        String month = "";
        Cursor c = dbHelper.getBillById(billId);
        if (c.moveToFirst()) {
            month = c.getString(c.getColumnIndexOrThrow(BillDatabaseHelper.COL_MONTH));
        }
        c.close();

        double rebate = sbRebate.getProgress();
        double total  = MainActivity.calculateTotalCharges(unit);
        double fin    = total - (total * rebate / 100.0);

        dbHelper.updateBill(billId, month, unit, total, rebate, fin);
        Toast.makeText(this, "✅ Record updated!", Toast.LENGTH_SHORT).show();
        loadBillDetail(); // Refresh the displayed data
    }
}