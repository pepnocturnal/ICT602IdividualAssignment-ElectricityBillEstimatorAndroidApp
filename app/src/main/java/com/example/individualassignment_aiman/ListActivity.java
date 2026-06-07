package com.example.individualassignment_aiman;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Locale;

public class ListActivity extends AppCompatActivity {

    ListView listView;
    TextView tvEmptyState;
    BillDatabaseHelper dbHelper;
    ArrayList<Integer> billIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        listView = findViewById(R.id.listView);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        dbHelper = new BillDatabaseHelper(this);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("bill_id", billIds.get(position));
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        billIds.clear();
        ArrayList<String> items = new ArrayList<>();

        Cursor cursor = dbHelper.getAllBills();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(BillDatabaseHelper.COL_ID));
            String month = cursor.getString(cursor.getColumnIndexOrThrow(BillDatabaseHelper.COL_MONTH));
            double finalCost = cursor.getDouble(cursor.getColumnIndexOrThrow(BillDatabaseHelper.COL_FINAL));

            items.add(month + "  —  RM " + String.format(Locale.getDefault(), "%.2f", finalCost));
            billIds.add(id);
        }
        cursor.close();

        if (items.isEmpty()) {
            listView.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
            listView.setAdapter(adapter);
        }
    }
}