package com.ogc.testrecorder;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listView;

    listCustomAdapter customAdapter;
    List<listItem> items;

    MySQLiteOpenHelper mySQLiteOpenHelper;
    SQLiteDatabase database;

    static final String Intent_bookName = "bookName";

    String bookTitles[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mySQLiteOpenHelper = new MySQLiteOpenHelper(getApplicationContext());
        database = mySQLiteOpenHelper.getWritableDatabase();

        int countOfBooks = countBooks();
        if(countOfBooks == 0){
            insertNewBook("book1");
        }

        bookTitles = new String[countOfBooks];

        listView = (ListView)findViewById(R.id.listView);
        setListView();
    }

    public void setListView(){

        items = new ArrayList<>();

        String title;
        int n = 0;
        bookTitles = searchBookTitle();
        while(bookTitles[n] != null){
            title = bookTitles[n];
            listItem item = new listItem(title);
            items.add(item);
            n++;
        }

        customAdapter = new listCustomAdapter(this, R.layout.main_listview_layout, items);
        listView.setAdapter(customAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, CountActivity.class);
                intent.putExtra(Intent_bookName, bookTitles[position]);
                startActivity(intent);
            }
        });
    }

    public String[] searchBookTitle(){

        int recordsAmount = countBooks();

        Cursor cursor = null;
        String[] result = new String[recordsAmount];

        try{
            cursor = database.query(MySQLiteOpenHelper.BooksTableName, new String[]{MySQLiteOpenHelper.Table_bookName}, null, null, null, null, null);
            int indexBookName = cursor.getColumnIndex(MySQLiteOpenHelper.Table_bookName);

            int n = 0;
            while(cursor.moveToNext()){
                result[n] = cursor.getString(indexBookName);
                n++;
            }
        } finally {
            if(cursor != null){
                cursor.close();
            }
        }

        return result;
    }

    public void insertNewBook(String title){

        ContentValues values = new ContentValues();
        values.put(MySQLiteOpenHelper.Table_bookName, title);
        database.insert(MySQLiteOpenHelper.BooksTableName, null, values);
    }

    public int countBooks(){
        return (int)DatabaseUtils.queryNumEntries(database, MySQLiteOpenHelper.BooksTableName);
    }

    public void newBook(View view){
        int amountOfBooks = countBooks();
        insertNewBook("book" + (amountOfBooks + 1));
    }
}
