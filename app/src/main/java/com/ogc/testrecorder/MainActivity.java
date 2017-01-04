package com.ogc.testrecorder;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
    int amountOfBooks, lastVersion;
    boolean screenIsSection;

    //一時的
    boolean isUpdatingAndAddingBook;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mySQLiteOpenHelper = new MySQLiteOpenHelper(getApplicationContext());
        database = mySQLiteOpenHelper.getWritableDatabase();

        listView = (ListView)findViewById(R.id.listView);
        newBookButton = (Button)findViewById(R.id.newBookButton);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        lastVersion = preferences.getInt(Preference_lastVersion, Version);
        preferences.edit().putInt(Preference_lastVersion, Version).apply();

        if(lastVersion < 3){
            // TODO: 2017/01/02 本の名前を決めさせ、セクションを本に入れる
            isUpdatingAndAddingBook = true;
            showUpdateDialog();
            while(isUpdatingAndAddingBook){
                editBookTitle(true, 0);
            }

            amountOfBooks = countBooks(MySQLiteOpenHelper.BooksTableName);
            for (int i = 0; i < amountOfBooks; i++){

            }
        }

        screenIsSection = false;
        setNewBookButton();

        amountOfBooks = countBooks(MySQLiteOpenHelper.BooksTableName);
        if(amountOfBooks == 0){
            editBookTitle(true, 0);
        }else{
            setListView();
        }
    }

    public void setListView(){

        items = new ArrayList<>();

        String title;
        bookTitles = new String[amountOfBooks];
        bookTitles = getTitles();
        int n;
        for(n = 0; n < amountOfBooks; n++){
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
        amountOfBooks = countBooks(MySQLiteOpenHelper.BooksTableName);
        String[] result = new String[amountOfBooks];

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
        amountOfBooks++;
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
        amountOfBooks--;
        setListView();
    }

    public int countBooks(String tableName){
        return (int)DatabaseUtils.queryNumEntries(database, tableName);
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
                    Toast.makeText(MainActivity.this, "nullはタイトルにできません。文字列を入力してください。文字列を入力している場合はエラーですのでレビュー投稿もしくは開発者にメールしてください。", Toast.LENGTH_LONG).show();
                } else {
                    text = spannableStringBuilder.toString();

                    if (isNew){
                        if(screenIsSection){
                            insertNewBook(text, true);
                        }else{
                            insertNewBook(text, false);
                            if(isUpdatingAndAddingBook){
                                checkIsFinished();
                            }
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
                    } else {
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

    //version 4をアップデートしたら消す

    public String[] searchContentsTableOld(int id){

        Cursor cursor = null;
        amountOfBooks = countBooks(MySQLiteOpenHelper.ContentsTableNameOld);
        String[] result = new String[amountOfBooks];

        try{
            cursor = database.query(MySQLiteOpenHelper.ContentsTableNameOld, new String[]{MySQLiteOpenHelper.Table_string_bookName, MySQLiteOpenHelper.ContentsTable_integer_questionNumber, MySQLiteOpenHelper.ContentsTable_integer_timesChallenged, MySQLiteOpenHelper.ContentsTable_integer_timesCorrect}, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
            int indexBookName = cursor.getColumnIndex(MySQLiteOpenHelper.Table_string_bookName);
            int indexQuestionNumber = cursor.getColumnIndex(MySQLiteOpenHelper.ContentsTable_integer_questionNumber);
            int indexTimesChallenged = cursor.getColumnIndex(MySQLiteOpenHelper.ContentsTable_integer_timesChallenged);
            int indexTimesCorrect = cursor.getColumnIndex(MySQLiteOpenHelper.ContentsTable_integer_timesCorrect);

            while(cursor.moveToNext()){
                result[0] = cursor.getString(indexBookName);
                result[1] = String.valueOf(cursor.getInt(indexQuestionNumber));
                result[2] = String.valueOf(cursor.getInt(indexTimesChallenged));
                result[3] = String.valueOf(cursor.getInt(indexTimesCorrect));
            }
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }

        return result;
    }

    public void showUpdateDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.update_dialog_on_version_4_title));
        alertDialogBuilder.setMessage(getResources().getString(R.string.update_dialog_on_version_4_message));
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editBookTitle(true, 0);
            }
        });
        alertDialogBuilder.show();
    }

    public void checkIsFinished(){
        String[] contents = {"まだ本を追加する", "既存のデータをセクションに分ける"};
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.update_dialog_on_version_4_is_finished_dialog_title));
        alertDialogBuilder.setItems(contents, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    editBookTitle(true, 0);
                } else {
                    isUpdatingAndAddingBook = false;
                }
            }
        });
    }

    public void showBookTitleAndSetInSection(){

        Cursor cursor = null;
        amountOfBooks = countBooks(MySQLiteOpenHelper.BooksTableName);
        int amountOfOld = (int)DatabaseUtils.queryNumEntries(database, MySQLiteOpenHelper.BooksTableNameOld);
        String[] titles = new String[amountOfOld];

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
                titles[n] = cursor.getString(indexTitle);
                n++;
            }
        } finally {
            if(cursor != null){
                cursor.close();
            }
        }

        String eachBookTitle;

        for(int i = 0; i < amountOfBooks; i++){

            eachBookTitle = titles[i];

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("本（" + eachBookTitle + "）に追加するセクションを選択");
            alertDialogBuilder.setMultiChoiceItems(titles, null, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    // TODO: 2017/01/04 チェックされたwhichを配列に入れ、下のメソッドを実行
                }
            });
        }
    }

    public void searchAndSaveContentsOldInNewDatabase(int id){
        // TODO: 2017/01/04 idを受け取り、そのデータの要素をContentsTableOldのデータベースから取得し、新しいContentsTableに入れる
    }
}
