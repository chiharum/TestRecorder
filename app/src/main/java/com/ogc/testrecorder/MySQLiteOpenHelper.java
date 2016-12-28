package com.ogc.testrecorder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    static final String Database = "TestRecorderDatabase.db";
    static final int DatabaseVersion = 2;

    static final String BooksTableName = "BooksTitles2";
    static final String ContentsTableName = "ContentsTable2";

    static final String Table_integer_id = "id";
    static final String Table_string_bookName = "book_name";
    static final String BooksTable_boolean_isSection = "is_section";
    static final String ContentsTable_string_sectionName = "section_name";
    static final String ContentsTable_integer_questionNumber = "question_number";
    static final String ContentsTable_integer_timesChallenged = "times_challenged";
    static final String ContentsTable_integer_timesCorrect = "times_correct";

    public MySQLiteOpenHelper(Context context){
        super(context, Database, null, DatabaseVersion);
    }

    public void onCreate(SQLiteDatabase database){
        database.execSQL("create table " + BooksTableName + " (" + Table_integer_id + " integer primary key autoincrement not null, " + Table_string_bookName + " string not null" + BooksTable_boolean_isSection + " boolean not null)");
        database.execSQL("create table " + ContentsTableName + " (" + Table_integer_id + " integer primary key autoincrement not null, " + Table_string_bookName + " string not null, " + ContentsTable_string_sectionName + " string not null, " + ContentsTable_integer_questionNumber + " integer not null, " + ContentsTable_integer_timesChallenged + " integer not null, " + ContentsTable_integer_timesCorrect + " integer not null)");
    }

    public void onUpgrade (SQLiteDatabase database, int oldVersion, int newVersion){

        if(oldVersion == 1 && newVersion == 2){
            // TODO: 2016/12/26 バージョンをアップして本・セクションに分けるのを可能にする
            database.execSQL("create table " + BooksTableName + " (" + Table_integer_id + " integer primary key autoincrement not null, " + Table_string_bookName + " string not null" + BooksTable_boolean_isSection + " boolean not null)");
            database.execSQL("create table " + ContentsTableName + " (" + Table_integer_id + " integer primary key autoincrement not null, " + Table_string_bookName + " string not null, " + ContentsTable_string_sectionName + " string not null, " + ContentsTable_integer_questionNumber + " integer not null, " + ContentsTable_integer_timesChallenged + " integer not null, " + ContentsTable_integer_timesCorrect + " integer not null)");
        }
    }
}
