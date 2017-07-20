package com.sargent.mark.todolist;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;


import com.sargent.mark.todolist.data.Contract;
import com.sargent.mark.todolist.data.DBHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AddToDoFragment.OnDialogCloseListener, UpdateToDoFragment.OnUpdateDialogCloseListener{

    private RecyclerView rv;
    private FloatingActionButton button;
    private DBHelper helper;
    private Cursor cursor;
    private SQLiteDatabase db;
    ToDoListAdapter adapter;
    ArrayList<CharSequence> filters = new ArrayList<>();
    private final String TAG = "mainactivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "oncreate called in main activity");
        button = (FloatingActionButton) findViewById(R.id.addToDo);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                AddToDoFragment frag = new AddToDoFragment();
                frag.show(fm, "addtodofragment");
            }
        });
        rv = (RecyclerView) findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (db != null) db.close();
        if (cursor != null) cursor.close();
    }

    @Override
    protected void onStart() {
        super.onStart();

        helper = new DBHelper(this);
        db = helper.getWritableDatabase();
        cursor = getAllItems(db);

        adapter = new ToDoListAdapter(cursor, new ToDoListAdapter.ItemClickListener() {

            @Override
            public void onItemClick(int pos, String description, String duedate, long id, String category) {
                Log.d(TAG, "item click id: " + id);
                String[] dateInfo = duedate.split("-");
                int year = Integer.parseInt(dateInfo[0].replaceAll("\\s",""));
                int month = Integer.parseInt(dateInfo[1].replaceAll("\\s",""));
                int day = Integer.parseInt(dateInfo[2].replaceAll("\\s",""));

                FragmentManager fm = getSupportFragmentManager();

                UpdateToDoFragment frag = UpdateToDoFragment.newInstance(year, month, day, description, id, category);
                frag.show(fm, "updatetodofragment");
            }
        },db);

        rv.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                long id = (long) viewHolder.itemView.getTag();
                Log.d(TAG, "passing id: " + id);
                removeToDo(db, id);
                adapter.swapCursor(getAllItems(db));
            }
        }).attachToRecyclerView(rv);

       /* new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                long id = (long) viewHolder.itemView.getTag();
                Log.d(TAG, "passing id: " + id);
                cursor = db.rawQuery(" SELECT "+ Contract.TABLE_TODO.COLUMN_NAME_CHECK +" FROM " + Contract.TABLE_TODO.TABLE_NAME + " WHERE " + Contract.TABLE_TODO._ID + "=" + id, null);
                int check = cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_CHECK);
                Log.d(TAG,"id" + check);
                if(check == 1) {
                    ContentValues cv = new ContentValues();
                    cv.put(Contract.TABLE_TODO.COLUMN_NAME_CHECK, 1);
                    db.update(Contract.TABLE_TODO.TABLE_NAME, cv, Contract.TABLE_TODO._ID + "=" + id, null);
                    cursor = getAllItems(db);
                    adapter.swapCursor(cursor);
                }
                else{
                    ContentValues cv = new ContentValues();
                    cv.put(Contract.TABLE_TODO.COLUMN_NAME_CHECK, 0);
                    db.update(Contract.TABLE_TODO.TABLE_NAME, cv, Contract.TABLE_TODO._ID + "=" + id, null);
                    cursor = getAllItems(db);
                    adapter.swapCursor(cursor);
                }
            }
        }).attachToRecyclerView(rv);
*/

    }

    //added additional parameter category
    public void closeDialog(int year, int month, int day, String description, String category) {
        addToDo(db, description, formatDate(year, month, day),category);
        cursor = getAllItems(db);
        adapter.swapCursor(cursor);
    }

    public String formatDate(int year, int month, int day) {
        return String.format("%04d-%02d-%02d", year, month + 1, day);
    }



    private Cursor getAllItems(SQLiteDatabase db) {
        return db.query(
                Contract.TABLE_TODO.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE
        );
    }

    private long addToDo(SQLiteDatabase db, String description, String duedate, String category) {
        ContentValues cv = new ContentValues();
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION, description);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE, duedate);
        //inserting the value category in DB
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY, category);
        return db.insert(Contract.TABLE_TODO.TABLE_NAME, null, cv);
    }

    private boolean removeToDo(SQLiteDatabase db, long id) {
        Log.d(TAG, "deleting id: " + id);
        return db.delete(Contract.TABLE_TODO.TABLE_NAME, Contract.TABLE_TODO._ID + "=" + id, null) > 0;
    }



    //added additional parameter category
    private int updateToDo(SQLiteDatabase db, int year, int month, int day, String description, long id, String category){

        String duedate = formatDate(year, month - 1, day);

        ContentValues cv = new ContentValues();
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION, description);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE, duedate);
        //updating the value category in DB
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY, category);
        return db.update(Contract.TABLE_TODO.TABLE_NAME, cv, Contract.TABLE_TODO._ID + "=" + id, null);
    }

    //added additional parameter category
    @Override
    public void closeUpdateDialog(int year, int month, int day, String description, long id, String category) {
        updateToDo(db, year, month, day, description, id,category);
        adapter.swapCursor(getAllItems(db));
    }

    //Inflating the Menu and its items in the view
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /*
    //Tracking the changes in checkbox
    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        ContentValues cv = new ContentValues();
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_CHECK,checked);
        //db.update(Contract.TABLE_TODO.TABLE_NAME,cv,Contract.TABLE_TODO._ID + "=" + id,null);
    }
    */

    //Modifying the recycler view based on the menu item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String query = " SELECT * FROM " + Contract.TABLE_TODO.TABLE_NAME +
                " WHERE " + Contract.TABLE_TODO.COLUMN_NAME_CATEGORY + " = ? ORDER BY " +
                Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE;
        // Handle item selection
        switch (item.getItemId()) {

            case R.id.All:
                cursor = db.rawQuery(" SELECT * FROM " + Contract.TABLE_TODO.TABLE_NAME + " ORDER BY " +
                        Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE, null);
                adapter.swapCursor(cursor);
                return true;

            case R.id.Class:
                cursor = db.rawQuery(query, new String[]{"Class"});
                adapter.swapCursor(cursor);
                return true;

            case R.id.Assignment:
                cursor = db.rawQuery(query, new String[]{"Assignment"});
                adapter.swapCursor(cursor);
                return true;

            case R.id.Project:
                cursor = db.rawQuery(query, new String[]{"Project"});
                adapter.swapCursor(cursor);
                return true;

            case R.id.Quiz:
                cursor = db.rawQuery(query, new String[]{"Quiz"});
                adapter.swapCursor(cursor);

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
