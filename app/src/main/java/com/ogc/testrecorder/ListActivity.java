package com.ogc.testrecorder;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    ListView questionsListView;

    List<questionsListItem> items;
    questionsListCustomAdapter customAdapter;

    MySQLiteOpenHelper mySQLiteOpenHelper;
    SQLiteDatabase database;

    int screenRecordsNumber;
    String screenBookTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mySQLiteOpenHelper = new MySQLiteOpenHelper(getApplicationContext());
        database = mySQLiteOpenHelper.getWritableDatabase();

        questionsListView = (ListView)findViewById(R.id.questionsListView);

        screenBookTitle = getIntent().getStringExtra(CountActivity.Intent_bookTitle);
        screenRecordsNumber = (int) DatabaseUtils.queryNumEntries(database, MySQLiteOpenHelper.ContentsTableName, MySQLiteOpenHelper.Table_bookName + " = ?", new String[]{screenBookTitle});

        setQuestionsListView();
    }

    public void setQuestionsListView(){

        items = new ArrayList<>();

        for (int n = 0; n < screenRecordsNumber; n = n + 1){
            questionsListItem item = new questionsListItem(n + 1, searchQuestions(n + 1)[0], searchQuestions(n + 1)[1]);
            items.add(item);
        }

        customAdapter = new questionsListCustomAdapter(this, R.layout.list_listview_layout, items);
        questionsListView.setAdapter(customAdapter);
    }

    public int[] searchQuestions(int questionNumber){

        Cursor cursor = null;
        int[] result = new int[2];

        try{
            cursor = database.query(MySQLiteOpenHelper.ContentsTableName, new String[]{MySQLiteOpenHelper.ContentsTable_timesCorrect, MySQLiteOpenHelper.ContentsTable_timesChallenged}, MySQLiteOpenHelper.Table_bookName + " = ? and " + MySQLiteOpenHelper.ContentsTable_questionNumber + " = ?", new String[]{screenBookTitle, String.valueOf(questionNumber)}, null, null, null);

            int indexTimesCorrect = cursor.getColumnIndex(MySQLiteOpenHelper.ContentsTable_timesCorrect);
            int indexTimesChallenged = cursor.getColumnIndex(MySQLiteOpenHelper.ContentsTable_timesChallenged);

            while(cursor.moveToNext()){
                result[0] = cursor.getInt(indexTimesCorrect);
                result[1] = cursor.getInt(indexTimesChallenged);
            }
        } finally {
            if(cursor != null){
                cursor.close();
            }
        }

        return result;
    }
}
