package com.example.individualassignment_aiman;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class BillDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "bills.db";
    private static final int    DB_VERSION = 1;

    public static final String TABLE     = "bills";
    public static final String COL_ID    = "id";
    public static final String COL_MONTH = "month";
    public static final String COL_UNIT  = "unit";
    public static final String COL_TOTAL  = "total_charges";
    public static final String COL_REBATE = "rebate";
    public static final String COL_FINAL  = "final_cost";

    public BillDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE + " ("
                + COL_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_MONTH + " TEXT NOT NULL, "
                + COL_UNIT  + " REAL NOT NULL, "
                + COL_TOTAL  + " REAL NOT NULL, "
                + COL_REBATE + " REAL NOT NULL, "
                + COL_FINAL  + " REAL NOT NULL)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    // ── CRUD Operations ────────────────────────────────────────────────────

    public long insertBill(String month, double unit, double total,
                           double rebate, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv  = buildContentValues(month, unit, total, rebate, finalCost);
        return db.insert(TABLE, null, cv);
    }

    public Cursor getAllBills() {
        return getReadableDatabase()
                .rawQuery("SELECT * FROM " + TABLE + " ORDER BY id DESC", null);
    }

    public Cursor getBillById(int id) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM " + TABLE + " WHERE id=?",
                new String[]{String.valueOf(id)});
    }

    public int updateBill(int id, String month, double unit,
                          double total, double rebate, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv  = buildContentValues(month, unit, total, rebate, finalCost);
        return db.update(TABLE, cv, "id=?", new String[]{String.valueOf(id)});
    }

    public void deleteBill(int id) {
        getWritableDatabase().delete(TABLE, "id=?", new String[]{String.valueOf(id)});
    }

    private ContentValues buildContentValues(String month, double unit,
                                             double total, double rebate,
                                             double finalCost) {
        ContentValues cv = new ContentValues();
        cv.put(COL_MONTH,  month);
        cv.put(COL_UNIT,   unit);
        cv.put(COL_TOTAL,  total);
        cv.put(COL_REBATE, rebate);
        cv.put(COL_FINAL,  finalCost);
        return cv;
    }
}