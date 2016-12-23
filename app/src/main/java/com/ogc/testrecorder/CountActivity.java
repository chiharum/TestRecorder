package com.ogc.testrecorder;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class CountActivity extends AppCompatActivity {

    TextView bookNameTextView, questionNumberTextView, correctTimeTextView;

    String bookName;
    int screenQuestionNumber, screenCorrectTimes, screenChallengedTimes;

    static final String Intent_bookTitle = "bookTitle";

    MySQLiteOpenHelper mySQLiteOpenHelper;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count);

        mySQLiteOpenHelper = new MySQLiteOpenHelper(getApplicationContext());
        database = mySQLiteOpenHelper.getWritableDatabase();

        bookNameTextView = (TextView)findViewById(R.id.bookNameText);
        questionNumberTextView = (TextView)findViewById(R.id.questionNumberText);
        correctTimeTextView = (TextView)findViewById(R.id.correctTimesText);

        bookName = getIntent().getStringExtra(MainActivity.Intent_bookName);
        bookNameTextView.setText(bookName);

        screenQuestionNumber = 1;
        setQuestion();
    }

    public void setQuestion(){
        questionNumberTextView.setText(String.valueOf(screenQuestionNumber));
        setCorrectTimeTextView();
    }

    public int[] getCorrectTimes(){

        Cursor cursor = null;
        int[] result = new int[2];

        try{
            cursor = database.query(MySQLiteOpenHelper.ContentsTableName, new String[]{MySQLiteOpenHelper.ContentsTable_timesCorrect, MySQLiteOpenHelper.ContentsTable_timesChallenged}, MySQLiteOpenHelper.Table_bookName + " = ? and " + MySQLiteOpenHelper.ContentsTable_questionNumber + " = ?", new String[]{bookName, String.valueOf(screenQuestionNumber)}, null, null, null);

            int indexCorrectTimes = cursor.getColumnIndex(MySQLiteOpenHelper.ContentsTable_timesCorrect);
            int indexChallengedTimes = cursor.getColumnIndex(MySQLiteOpenHelper.ContentsTable_timesChallenged);

            while(cursor.moveToNext()){
                int correctTimes = cursor.getInt(indexCorrectTimes);
                int challengedTimes = cursor.getInt(indexChallengedTimes);
                result[0] = correctTimes;
                result[1] = challengedTimes;
            }
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }

        return result;
    }

    public void saveScreen(int correctTimesChange){
        ContentValues values = new ContentValues();
        values.put(MySQLiteOpenHelper.Table_bookName, bookName);
        values.put(MySQLiteOpenHelper.ContentsTable_questionNumber, screenQuestionNumber);
        values.put(MySQLiteOpenHelper.ContentsTable_timesChallenged, screenChallengedTimes + 1);
        values.put(MySQLiteOpenHelper.ContentsTable_timesCorrect, screenCorrectTimes + correctTimesChange);
        database.insert(MySQLiteOpenHelper.ContentsTableName, null, values);
    }

    public void setCorrectTimeTextView(){
        screenCorrectTimes = getCorrectTimes()[0];
        screenChallengedTimes = getCorrectTimes()[1];
        correctTimeTextView.setText(screenCorrectTimes + " / " + screenChallengedTimes);
    }

    public void correct(View view){
        saveScreen(1);
        screenQuestionNumber++;
        setQuestion();
    }

    public void incorrect(View view){
        saveScreen(0);
        screenQuestionNumber++;
        setQuestion();
    }

    public void showList(View view){
        Intent intent = new Intent();
        intent.putExtra(Intent_bookTitle, bookName);
        intent.setClass(CountActivity.this, ListActivity.class);
        startActivity(intent);
    }
}
