package com.ogc.testrecorder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    static final String Database = "TestRecorderDatabase.db";
    static final int DatabaseVersion = 1;

    static final String BooksTableName = "GroupNamesTable";
    static final String ContentsTableName = "ContentsTable";

    static final String Table_id = "id";
    static final String Table_bookName = "book_name";
    static final String Table_bookId = "book_id";
    static final String ContentsTable_questionNumber = "question_number";
    static final String ContentsTable_timesChallenged = "times_challenged";
    static final String ContentsTable_timesCorrect = "times_correct";

    public MySQLiteOpenHelper(Context context){
        super(context, Database, null, DatabaseVersion);
    }

    public void onCreate(SQLiteDatabase database){
        database.execSQL("create table " + BooksTableName + " (" + Table_id + " integer primary key autoincrement not null, " + Table_bookName + " string not null, " + Table_bookId + " integer not null)");
        database.execSQL("create table " + ContentsTableName + " (" + Table_id + " integer primary key autoincrement not null, " + Table_bookName + " string not null, " + Table_bookId + " integer not null, " + ContentsTable_questionNumber + " integer not null, " + ContentsTable_timesChallenged + " integer not null, " + ContentsTable_timesCorrect + " integer not null)");
    }

    public void onUpgrade (SQLiteDatabase database, int oldVersion, int newVersion){

    }
}
