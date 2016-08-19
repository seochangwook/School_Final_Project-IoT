package com.example.seo.project;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Seo on 2016-03-31.
 */
public class Enroll_Final_Activity extends ActionBarActivity
{
    DB_Helper db_helper; //핼퍼클래스 정의.//
    SQLiteDatabase db; //데이터베이스 기능을 사용하기 위한 SQLite 사용.//
    Cursor cursor; //탐색을 할 커서정의.//
    String sql;

    FloatingActionButton info_enroll_button;

    Button enroll_button;
    EditText light_location_1, light_location_2, light_location_3, motor_location_1;

    String user_id;
    String mail_password;
    String user_name;
    String user_phone_number;
    String user_address;
    String light_sensor_1_location;
    String light_sensor_2_location;
    String light_sensor_3_location;
    String motor_sensor_1_location;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enroll_final_activity_layout);

        light_location_1 = (EditText)findViewById(R.id.light_location_1_edit);
        light_location_2 = (EditText)findViewById(R.id.light_location_2_edit);
        light_location_3 = (EditText)findViewById(R.id.light_location_3_edit);
        enroll_button = (Button)findViewById(R.id.enroll_button);
        info_enroll_button = (FloatingActionButton)findViewById(R.id.info_button);
        motor_location_1 = (EditText)findViewById(R.id.motor_location_1_edit);

        db_helper = new DB_Helper(this);

        Intent intent = getIntent();

        user_id = intent.getStringExtra("KEY_USER_ID");
        user_name = intent.getStringExtra("KEY_USER_NAME");
        user_phone_number = intent.getStringExtra("KEY_USER_PHONE_NUMBER");
        user_address = intent.getStringExtra("KEY_USER_ADDRESS");
        mail_password = intent.getStringExtra("KEY_MAIL_PASSWORD");

        enroll_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                light_sensor_1_location = light_location_1.getText().toString();
                light_sensor_2_location = light_location_2.getText().toString();
                light_sensor_3_location = light_location_3.getText().toString();
                motor_sensor_1_location = motor_location_1.getText().toString();

                //데이터베이스에 해당 항목들을 저장.//
                Insert_DB(user_id,mail_password,user_name,user_phone_number,user_address,light_sensor_1_location,light_sensor_2_location,
                        light_sensor_3_location,motor_sensor_1_location);

                /*Toast.makeText(getApplicationContext(),
                        user_id+"\n"+
                        mail_password+"\n"+
                        user_name+"\n"+
                        user_phone_number+"\n"+
                        user_address+"\n"+
                        light_sensor_1_location+"\n"+
                        light_sensor_2_location+"\n"+
                        light_sensor_3_location+"\n"
                        ,Toast.LENGTH_SHORT).show();*/

                Toast.makeText(getApplicationContext(), "가입완료!!", Toast.LENGTH_SHORT).show();
            }
        });

        info_enroll_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Print_DB();

                Snackbar.make(view,
                        "1. 센서들의 위치를 정확히 입력." + "\n" +
                        "2. 문의사항 : home@naver.com",
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
    }

    private void Insert_DB(String user_id, String mail_password, String user_name, String user_phone_number, String user_address, String light_sensor_1_location,
                           String light_sensor_2_location, String light_sensor_3_location, String motor_sensor_1_location)
    {
        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "INSERT INTO home_info(user_id,mail_password,user_name,user_phone_number,user_address,light_1_location,light_2_location,light_3_location,motor_1_location" +
                ") VALUES('"+user_id+"','"+mail_password+"','"+user_name+"','"+user_phone_number+"','"+user_address+"','"+light_sensor_1_location+"','"+
                light_sensor_2_location+"','"+light_sensor_3_location+"','"+motor_sensor_1_location+"');";

        db.execSQL(sql); //SQL문장을 실행해서 디비작업 진행.//
    }

    private void Print_DB()
    {
        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "SELECT * FROM home_info"; //전체출력 SQL.//

        cursor = db.rawQuery(sql, null);

        while(cursor.moveToNext())
        {
            Toast.makeText(getApplicationContext(), ""+cursor.getString(cursor.getColumnIndex("user_name"))+","+cursor.getString(cursor.getColumnIndex("mail_password"))+","+
                    cursor.getString(cursor.getColumnIndex("user_id"))+ ","+cursor.getString(cursor.getColumnIndex("user_phone_number"))+cursor.getString(cursor.getColumnIndex("user_address"))
                    , Toast.LENGTH_SHORT).show();
        }

        sql = "";

        sql = "SELECT * FROM service_info;"; //전체출력 SQL.//

        cursor = db.rawQuery(sql, null);

        while(cursor.moveToNext())
        {
            Toast.makeText(getApplicationContext(), ""+cursor.getString(cursor.getColumnIndex("service_number"))+","+cursor.getString(cursor.getColumnIndex("service_name"))+","+
                    cursor.getString(cursor.getColumnIndex("service_on_off")), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_2, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
