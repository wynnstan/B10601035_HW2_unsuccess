package com.example.android.HW2;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.HW2.data.WaitlistContract;
import com.example.android.HW2.data.WaitlistDbHelper;


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    //明明build成功卻不知道為甚麼開不起來QQ
    //看了別人的也找不倒錯在哪裡QQ
    private GuestListAdapter waitlistAdapter;
    private SQLiteDatabase mDb;
    RecyclerView waitlistRecyclerView;

    @ColorInt
    private static int shapeColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        waitlistRecyclerView = (RecyclerView) this.findViewById(R.id.all_guests_list_view);
        waitlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupSharedPreferences();

        WaitlistDbHelper dbHelper = new WaitlistDbHelper(this);
        mDb = dbHelper.getWritableDatabase();

        Cursor cursor = getAllGuests();
        waitlistAdapter = new GuestListAdapter(this, cursor);
        waitlistRecyclerView.setAdapter(waitlistAdapter);


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                final RecyclerView.ViewHolder viewHolder1 = viewHolder;
                final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("WARN");
                alertDialog.setMessage("Are you sure to delete the item?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        long id = (long) viewHolder1.itemView.getTag();
                        removeGuest(id);
                        waitlistAdapter.swapCursor(getAllGuests());
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    waitlistAdapter.swapCursor(getAllGuests());
                    dialogInterface.dismiss();
                }
                });
                alertDialog.show();
            }
        }).attachToRecyclerView(waitlistRecyclerView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settings) {
            Intent startSettings = new Intent(this, SettingsActivity.class);
            startActivity(startSettings);
            return true;
        } else if (id == R.id.add_guest) {
            Intent startAddGuest = new Intent(this, addGuestActivity.class);
            startActivity(startAddGuest);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Cursor getAllGuests() {
        return mDb.query(WaitlistContract.WaitlistEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                WaitlistContract.WaitlistEntry.COLUMN_TIMESTAMP);
    }

    private boolean removeGuest(long id) {
        return mDb.delete(WaitlistContract.WaitlistEntry.TABLE_NAME, WaitlistContract.WaitlistEntry._ID + "=" + id, null) > 0;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.pref_color_key))) {
            loadColorFromPreferences(sharedPreferences);
        }
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        loadColorFromPreferences(sharedPreferences);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void loadColorFromPreferences(SharedPreferences sharedPreferences) {
        setColor(sharedPreferences.getString(getString(R.string.pref_color_key), getString(R.string.pref_color_red_value)));
    }

    public void setColor(String newColorKey) {
        if (newColorKey.equals(waitlistRecyclerView.getContext().getString(R.string.pref_color_blue_value))) {
            shapeColor = ContextCompat.getColor(waitlistRecyclerView.getContext(), R.color.shapeBlue);
        } else if (newColorKey.equals(waitlistRecyclerView.getContext().getString(R.string.pref_color_green_value))) {
            shapeColor = ContextCompat.getColor(waitlistRecyclerView.getContext(), R.color.shapeGreen);
        } else if (newColorKey.equals(waitlistRecyclerView.getContext().getString(R.string.pref_color_purple_value))) {
            shapeColor = ContextCompat.getColor(waitlistRecyclerView.getContext(), R.color.shapePurple);
        } else {
            shapeColor = ContextCompat.getColor(waitlistRecyclerView.getContext(), R.color.shapeRed);
        }
    }

    public static int getShapeColor() {
        return shapeColor;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }
}