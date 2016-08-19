package com.example.seo.project;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Seo on 2016-03-22.
 */
public class DB_Helper extends SQLiteOpenHelper
{
    String sql_table_create_1;
    String sql_table_create_2;
    String sql_service_insert_1;
    String sql_service_insert_2;
    String motor_service_insert_1;
    String light_1_state;
    String light_2_state;
    String light_3_state;

    final static String dbName = "project.db"; //기본적인 데이터베이스의 이름을 정의.//
    final static int dbVersion = 1; //데이터베이스의 버전을 정의.//

    public DB_Helper(Context context) //생성자.//
    {
        //생성자로 현재 이 DB가 사용될 액티비티에 대해서 불러온다.//
        super(context, dbName, null, dbVersion); //생성자를 이용하여서 db의 기본정보를 설정한다. -> 데이터베이스 생성.//
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        //테이블 생성은 앱이 실행되고 나서 딱 한번만 실행된다.이 부분에서 필요한 릴레이션들을 만든다.//
        sql_table_create_1 = "CREATE TABLE home_info(" +
                            "_id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                            "user_id TEXT," +
                            "mail_password TEXT,"+
                            "user_name TEXT," +
                            "user_phone_number TEXT," +
                            "user_address TEXT," +
                            "light_1_location TEXT,"+
                            "light_2_location TEXT,"+
                            "light_3_location TEXT,"+
                            "motor_1_location TEXT"+
                            ");";

        sql_table_create_2 = "CREATE TABLE service_info("+
                             "service_number INTEGER,"+
                             "service_name TEXT,"+
                             "service_on_off INTEGER," + //on - 1, off - 0//
                             "PRIMARY KEY(service_number)"+
                             ");";

        db.execSQL(sql_table_create_1); //쿼리문 수행.//
        db.execSQL(sql_table_create_2); //쿼리문 수행.//

        //기본적으로 앱을 처음 실행하면 off를 해주기 위해서 0을 대입.//
        sql_service_insert_1 = "INSERT INTO service_info(service_number, service_name, service_on_off)" +
                "VALUES (1, 'motion service', 0);";
        sql_service_insert_2 = "INSERT INTO service_info(service_number, service_name, service_on_off)" +
                "VALUES (2, 'fire service', 0);";
        motor_service_insert_1 = "INSERT INTO service_info(service_number, service_name, service_on_off)" +
                "VALUES (3, 'motor 1 service', 0);";
        light_1_state = "INSERT INTO service_info(service_number, service_name, service_on_off)" +
                "VALUES (6, 'light 1 state', 0);";
        light_2_state =  "INSERT INTO service_info(service_number, service_name, service_on_off)" +
                "VALUES (7, 'light 2 state', 0);";
        light_3_state = "INSERT INTO service_info(service_number, service_name, service_on_off)" +
                "VALUES (8, 'light 3 state', 0);";

        db.execSQL(sql_service_insert_1); //쿼리문 수행.//
        db.execSQL(sql_service_insert_2); //쿼리문 수행.//
        db.execSQL(motor_service_insert_1); //쿼리문 수행.//
        db.execSQL(light_1_state); //쿼리문 수행.//
        db.execSQL(light_2_state); //쿼리문 수행.//
        db.execSQL(light_3_state); //쿼리문 수행.//
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //버전이 업그레이드 됐을 경우 작업할 내용을 작성합니다.
        String sql="DROP TABLE IF EXISTS project"; //만약 contact테이블이 존재시 우선 제거.//

        Log.i("DB", "Database Upgrade");

        onCreate(db); //우선제거 후 다시 onCreate()호출해서 테이블 생성.//
    }

    public void close()
    {
        super.close();
    }
}