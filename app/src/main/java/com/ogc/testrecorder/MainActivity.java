package com.ogc.testrecorder;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    Button newBookButton;

    listCustomAdapter customAdapter;
    List<listItem> items;

    SharedPreferences preferences;

    MySQLiteOpenHelper mySQLiteOpenHelper;
    SQLiteDatabase database;

    static final String Intent_bookName = "bookName";
    static final String Preference_lastVersion = "lastVersion";
    static final String Intent_sectionName = "sectionName";
    static final int Version = 3;

    String bookTitles[], bookTitle;
    int recordsCounts, lastVersion;
    boolean screenIsSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mySQLiteOpenHelper = new MySQLiteOpenHelper(getApplicationContext());
        database = mySQLiteOpenHelper.getWritableDatabase();

        // TODO: 2016/12/28 データの引き継ぎ

        listView = (ListView)findViewById(R.id.listView);
        newBookButton = (Button)findViewById(R.id.newBookButton);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        lastVersion = preferences.getInt(Preference_lastVersion, Version);
        preferences.edit().putInt(Preference_lastVersion, Version).apply();

        if(lastVersion < 3){
            // TODO: 2016/12/28 データ引き継ぎ http://miquniqu.blogspot.jp/2012/01/androidsqlite.htmlとか？
        }

        screenIsSection = false;
        setNewBookButton();

        recordsCounts = countBooks();
        if(recordsCounts == 0){
            editBookTitle(true, 0);
        }else{
            setListView();
        }
    }

    public void setListView(){

        items = new ArrayList<>();

        String title;
        bookTitles = new String[recordsCounts];
        bookTitles = getTitles();
        int n;
        for(n = 0; n < recordsCounts; n++){
            title = bookTitles[n];
            listItem item = new listItem(title);
            items.add(item);
        }

        customAdapter = new listCustomAdapter(this, R.layout.main_listview_layout, items);
        listView.setAdapter(customAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                showBookMenu(position);
            }
        });
    }

    public String[] getTitles(){

        Cursor cursor = null;
        recordsCounts = countBooks();
        String[] result = new String[recordsCounts];

        try{
            cursor = database.query(MySQLiteOpenHelper.BooksTableName, new String[]{MySQLiteOpenHelper.Table_string_bookName, MySQLiteOpenHelper.BooksTable_boolean_isSection}, null, null, null, null, null);
            int indexTitle;
            if(screenIsSection){
                indexTitle = cursor.getColumnIndex(MySQLiteOpenHelper.Table_string_bookName);
            }else{
                indexTitle = cursor.getColumnIndex(MySQLiteOpenHelper.ContentsTable_string_sectionName);
            }

            int n = 0;
            while(cursor.moveToNext()){
                result[n] = cursor.getString(indexTitle);
                n++;
            }
        } finally {
            if(cursor != null){
                cursor.close();
            }
        }

        return result;
    }

    public void insertNewBook(String title, boolean isSection){
        ContentValues values = new ContentValues();
        values.put(MySQLiteOpenHelper.Table_string_bookName, title);
        values.put(MySQLiteOpenHelper.BooksTable_boolean_isSection, isSection);
        database.insert(MySQLiteOpenHelper.BooksTableName, null, values);
        recordsCounts++;
        setListView();
    }

    public void updateBook(int position, String title){

        ContentValues values = new ContentValues();
        values.put(MySQLiteOpenHelper.Table_string_bookName, title);
        database.update(MySQLiteOpenHelper.BooksTableName, values, "id = ?", new String[]{String.valueOf(position)});
        setListView();
    }

    public void deleteBook(int id){
        database.delete(MySQLiteOpenHelper.BooksTableName, "id = ?", new String[]{String.valueOf(id)});
        recordsCounts--;
        setListView();
    }

    public int countBooks(){
        return (int)DatabaseUtils.queryNumEntries(database, MySQLiteOpenHelper.BooksTableName);
    }

    public void editBookTitle(final boolean isNew, final int id){

        LayoutInflater layoutInflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View alertDialogLayout = layoutInflater.inflate(R.layout.edit_text_dialog_layout, null);

        final EditText editText = (EditText)alertDialogLayout.findViewById(R.id.editBookTitleEditText);
        if(!isNew){
            editText.setText(bookTitles[id - 1]);
        }

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("本の名前を入力してください");
        alertDialog.setView(alertDialogLayout);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String text;

                SpannableStringBuilder spannableStringBuilder = (SpannableStringBuilder) editText.getText();
                if (spannableStringBuilder == null) {
                    Toast.makeText(MainActivity.this, "nullはタイトルにできません。文字列を入力してください。", Toast.LENGTH_LONG).show();
                } else {
                    text = spannableStringBuilder.toString();

                    if (isNew){
                        if(screenIsSection){
                            insertNewBook(text, true);
                        }else{
                            insertNewBook(text, false);
                        }
                    }else{
                        if (id == 0){
                            Toast.makeText(MainActivity.this, "idの値が不正です。(id = 0)", Toast.LENGTH_SHORT).show();
                        }else{
                            updateBook(id, text);
                        }
                    }
                }
            }
        });
        alertDialog.setNegativeButton("キャンセル", null);
        alertDialog.show();
    }

    public void showBookMenu(final int position){

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle("オプション");
        alertDialogBuilder.setItems(getResources().getStringArray(R.array.book_choosing_dialog_menus), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //開く
                    if (screenIsSection) {
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, CountActivity.class);
                        intent.putExtra(Intent_bookName, bookTitles);
                        intent.putExtra(Intent_sectionName, bookTitles[position]);
                        startActivity(intent);
                    }else{
                        bookTitle = bookTitles[position];
                        screenIsSection = true;
                        setListView();
                    }

                } else if (which == 1) {
                    //編集
                    editBookTitle(false, position + 1);
                } else if (which == 2) {
                    //削除
                    deleteBook(position + 1);
                }
            }
        });
        alertDialogBuilder.show();
    }

    public void setNewBookButton(){
        if(screenIsSection){
            newBookButton.setText(getResources().getString(R.string.new_section));
        }else{
            newBookButton.setText(getResources().getString(R.string.new_book));
        }
    }

    public void newBook(View view){
        editBookTitle(true, 0);
    }
}
