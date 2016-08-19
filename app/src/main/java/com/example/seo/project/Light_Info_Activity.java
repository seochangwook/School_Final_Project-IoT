package com.example.seo.project;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Seo on 2016-04-14.
 */
public class Light_Info_Activity extends ActionBarActivity implements TextToSpeech.OnInitListener
{
    DB_Helper db_helper; //핼퍼클래스 정의.//
    SQLiteDatabase db; //데이터베이스 기능을 사용하기 위한 SQLite 사용.//
    Cursor cursor; //탐색을 할 커서정의.//
    String sql;

    TextToSpeech myTTS;

    TextView server_info_text;

    TextView light_position_1_text;
    TextView light_position_2_text;
    TextView light_position_3_text;

    TextView light_1_info;
    TextView light_2_info;
    TextView light_3_info;

    Switch light_1_switch;
    Switch light_2_switch;
    Switch light_3_switch;

    private String server_ip_address;
    private String server_port_number;
    private int port_number;
    private String user_id;
    String anounce_data = ""; //출력할 음성 데이터.//
    String trans_data_format = ""; //전송할 데이터//
    String user_phone_number;

    int service_light_check_1;
    int service_light_check_2;
    int service_light_check_3;

    String light_location_1;
    String light_location_2;
    String light_location_3;

    ProgressBar light_1_progress;
    ProgressBar light_2_progress;
    ProgressBar light_3_progress;

    int sensor_1_check_value_light = 0;
    int sensor_1_check_value_dark = 0;
    int sensor_2_check_value_light = 0;
    int sensor_2_check_value_dark = 0;
    int sensor_3_check_value_light = 0;
    int sensor_3_check_value_dark = 0;

    //수행할 서비스//
    LED_Service_NetworkTask_1 led_sensor_task_1;
    LED_Service_NetworkTask_2 led_sensor_task_2;
    LED_Service_NetworkTask_3 led_sensor_task_3;

    FloatingActionButton info_1_button;
    FloatingActionButton info_2_button;

    public void onStop() //해당 액티비티를 벗어나면 센싱작업 종료//
    {
        super.onStop();

        led_sensor_task_1.cancel(true);
        led_sensor_task_2.cancel(true);
        led_sensor_task_3.cancel(true);

        finish();
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.light_info_activity_layout);

        db_helper = new DB_Helper(this);
        myTTS = new TextToSpeech(this, this); //TTS객체 선언.TTS엔진을 초기화.//

        server_info_text = (TextView)findViewById(R.id.server_address_info_text);
        light_position_1_text = (TextView)findViewById(R.id.light_location_1_text);
        light_position_2_text = (TextView)findViewById(R.id.light_location_2_text);
        light_position_3_text = (TextView)findViewById(R.id.light_location_3_text);
        light_1_switch = (Switch)findViewById(R.id.light_1_switch);
        light_2_switch = (Switch)findViewById(R.id.light_2_switch);
        light_3_switch = (Switch)findViewById(R.id.light_3_switch);
        light_1_progress = (ProgressBar)findViewById(R.id.progress_bar_1);
        light_2_progress = (ProgressBar)findViewById(R.id.progress_bar_2);
        light_3_progress = (ProgressBar)findViewById(R.id.progress_bar_3);
        info_1_button = (FloatingActionButton)findViewById(R.id.info_button_1);
        info_2_button = (FloatingActionButton)findViewById(R.id.info_button_2);
        light_1_info = (TextView)findViewById(R.id.light_info_1);
        light_2_info = (TextView)findViewById(R.id.light_info_2);
        light_3_info = (TextView)findViewById(R.id.light_info_3);


        //처음 탭 화면을 초기화 할 때 서비스의 설정 정보를 불러온다. (데이터베이스)//
        service_light_check_1 = Check_Service_1_On_Off();
        service_light_check_2 = Check_Service_2_On_Off();
        service_light_check_3 = Check_Service_3_On_Off();

        if(service_light_check_1 == 0) //off상태.//
        {
            light_1_switch.setChecked(false);
        }

        else if(service_light_check_1 == 1) //on상태.//
        {
            light_1_switch.setChecked(true);
        }

        if(service_light_check_2 == 0) //off상태.//
        {
            light_2_switch.setChecked(false);
        }

        else if(service_light_check_2 == 1) //on상태.//
        {
            light_2_switch.setChecked(true);
        }

        if(service_light_check_3 == 0) //off상태.//
        {
            light_3_switch.setChecked(false);
        }

        else if(service_light_check_3 == 1) //on상태.//
        {
            light_3_switch.setChecked(true);
        }

        Intent intent = getIntent();

        server_ip_address = intent.getStringExtra("KEY_SERVER_IP");
        server_port_number = intent.getStringExtra("KEY_PORT_NUMBER");
        user_id = intent.getStringExtra("KEY_USER_ID");

        port_number = Integer.parseInt(server_port_number);

        //메시지 서비스를 위해서 등록된 사용자의 전화번호를 가져온다//
        user_phone_number = get_user_phone_number(user_id);

        //서비스 객체 생성//
        led_sensor_task_1 = new LED_Service_NetworkTask_1(server_ip_address, port_number, user_phone_number);
        led_sensor_task_2 = new LED_Service_NetworkTask_2(server_ip_address, port_number, user_phone_number);
        led_sensor_task_3 = new LED_Service_NetworkTask_3(server_ip_address, port_number, user_phone_number);

        led_sensor_task_1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        led_sensor_task_2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        led_sensor_task_3.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        server_info_text.setText("Server IP : "+server_ip_address+" / Server PORT : "+server_port_number);

        light_location_1 = get_light_1_location(user_id);
        light_location_2 = get_light_2_location(user_id);
        light_location_3 = get_light_3_location(user_id);

        light_position_1_text.setText(light_location_1);
        light_position_2_text.setText(light_location_2);
        light_position_3_text.setText(light_location_3);

        light_1_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) //토글버튼이 ON상태일 경우.//
                {
                    anounce_data = light_position_1_text.getText().toString() + " 불이 켜집니다.";
                    myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
                    anounce_data = "";

                    //서비스를 on했다는 정보를 DB에 갱신.//
                    Update_DB_light_service_1_on();

                    //점등 서비스 실행. LED ON/OFF//
                    Intent service_intent = new Intent(Light_Info_Activity.this, Service_Activity_LED_1_ON.class);

                    service_intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
                    service_intent.putExtra("KEY_PORT_NUMBER", "" + server_port_number);
                    service_intent.putExtra("KEY_SERVICE_NUMBER", "1");

                    startService(service_intent);

                    //LED 서비스는 서비스를 바로 끝내주므로 종료를 한다.//
                    stopService(service_intent);
                }

                else //토글버튼이 OFF상태일 경우.//
                {
                    anounce_data = light_position_1_text.getText().toString() + " 불이 꺼집니다.";
                    myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
                    anounce_data = "";

                    Update_DB_light_service_1_off();

                    Intent service_intent = new Intent(Light_Info_Activity.this, Service_Activity_LED_1_OFF.class);

                    service_intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
                    service_intent.putExtra("KEY_PORT_NUMBER", "" + server_port_number);
                    service_intent.putExtra("KEY_SERVICE_NUMBER", "1");

                    startService(service_intent);

                    //LED 서비스는 서비스를 바로 끝내주므로 종료를 한다.//
                    stopService(service_intent);
                }
            }
        });

        light_3_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) //토글버튼이 ON상태일 경우.//
                {
                    anounce_data = light_position_3_text.getText().toString() + " 불이 켜집니다.";
                    myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
                    anounce_data = "";

                    //서비스를 on했다는 정보를 DB에 갱신.//
                    Update_DB_light_service_3_on();

                    //점등 서비스 실행. LED ON/OFF//
                    Intent service_intent = new Intent(Light_Info_Activity.this, Service_Activity_LED_3_ON.class);

                    service_intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
                    service_intent.putExtra("KEY_PORT_NUMBER", "" + server_port_number);
                    service_intent.putExtra("KEY_SERVICE_NUMBER", "1");

                    startService(service_intent);

                    //LED 서비스는 서비스를 바로 끝내주므로 종료를 한다.//
                    stopService(service_intent);
                }

                else //토글버튼이 OFF상태일 경우.//
                {
                    anounce_data = light_position_3_text.getText().toString() + " 불이 꺼집니다.";
                    myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
                    anounce_data = "";

                    Update_DB_light_service_3_off();

                    //점등 서비스 실행. LED ON/OFF//
                    Intent service_intent = new Intent(Light_Info_Activity.this, Service_Activity_LED_3_OFF.class);

                    service_intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
                    service_intent.putExtra("KEY_PORT_NUMBER", "" + server_port_number);
                    service_intent.putExtra("KEY_SERVICE_NUMBER", "1");

                    startService(service_intent);

                    //LED 서비스는 서비스를 바로 끝내주므로 종료를 한다.//
                    stopService(service_intent);
                }
            }
        });

        light_2_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) //토글버튼이 ON상태일 경우.//
                {
                    anounce_data = light_position_2_text.getText().toString() + " 불이 켜집니다.";
                    myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
                    anounce_data = "";

                    //서비스를 on했다는 정보를 DB에 갱신.//
                    Update_DB_light_service_2_on();

                    //점등 서비스 실행. LED ON/OFF//
                    Intent service_intent = new Intent(Light_Info_Activity.this, Service_Activity_LED_2_ON.class);

                    service_intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
                    service_intent.putExtra("KEY_PORT_NUMBER", "" + server_port_number);
                    service_intent.putExtra("KEY_SERVICE_NUMBER", "1");

                    startService(service_intent);

                    //LED 서비스는 서비스를 바로 끝내주므로 종료를 한다.//
                    stopService(service_intent);

                } else //토글버튼이 OFF상태일 경우.//
                {
                    anounce_data = light_position_2_text.getText().toString() + " 불이 꺼집니다.";
                    myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
                    anounce_data = "";

                    Update_DB_light_service_2_off();

                    //점등 서비스 실행. LED ON/OFF//
                    Intent service_intent = new Intent(Light_Info_Activity.this, Service_Activity_LED_2_OFF.class);

                    service_intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
                    service_intent.putExtra("KEY_PORT_NUMBER", "" + server_port_number);
                    service_intent.putExtra("KEY_SERVICE_NUMBER", "1");

                    startService(service_intent);

                    //LED 서비스는 서비스를 바로 끝내주므로 종료를 한다.//
                    stopService(service_intent);
                }
            }
        });

        info_1_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view,
                        "1. 센서의 특징설명 입니다." + "\n",
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        info_2_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view,
                        "1. 센서의 주의사항입니다." + "\n",
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
    }

    public String get_user_phone_number(String user_id)
    {
        String user_phone_number = null;

        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "SELECT user_phone_number FROM home_info WHERE user_id = '"+user_id+"';"; //전체출력 SQL.//

        cursor = db.rawQuery(sql, null);

        while(cursor.moveToNext())
        {
            user_phone_number = cursor.getString(cursor.getColumnIndex("user_phone_number"));

            //Toast.makeText(getApplicationContext(), user_phone_number, Toast.LENGTH_SHORT).show();
        }

        return user_phone_number;
    }

    public void Update_DB_light_service_1_on()
    {
        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "UPDATE service_info SET service_on_off = 1 WHERE service_number = 6";

        db.execSQL(sql); //쿼리문 수행.//
    }

    public void Update_DB_light_service_1_off()
    {
        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "UPDATE service_info SET service_on_off = 0 WHERE service_number = 6";

        db.execSQL(sql); //쿼리문 수행.//
    }

    public void Update_DB_light_service_2_on()
    {
        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "UPDATE service_info SET service_on_off = 1 WHERE service_number = 7";

        db.execSQL(sql); //쿼리문 수행.//
    }

    public void Update_DB_light_service_2_off()
    {
        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "UPDATE service_info SET service_on_off = 0 WHERE service_number = 7";

        db.execSQL(sql); //쿼리문 수행.//
    }

    public void Update_DB_light_service_3_on()
    {
        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "UPDATE service_info SET service_on_off = 1 WHERE service_number = 8";

        db.execSQL(sql); //쿼리문 수행.//
    }

    public void Update_DB_light_service_3_off()
    {
        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "UPDATE service_info SET service_on_off = 0 WHERE service_number = 8";

        db.execSQL(sql); //쿼리문 수행.//
    }

    public int Check_Service_1_On_Off()
    {
        int check_value = 0; //센서가 꺼져있다는 가정//

        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "SELECT service_on_off FROM service_info WHERE service_number = 6;"; //전체출력 SQL.//

        cursor = db.rawQuery(sql, null);

        while(cursor.moveToNext())
        {
            check_value = Integer.parseInt(cursor.getString(cursor.getColumnIndex("service_on_off")));

            //Toast.makeText(getActivity(), "check 1 : "+check_value, Toast.LENGTH_SHORT).show();
        }

        return check_value;
    }

    public int Check_Service_2_On_Off()
    {
        int check_value = 0; //센서가 꺼져있다는 가정//

        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "SELECT service_on_off FROM service_info WHERE service_number = 7;"; //전체출력 SQL.//

        cursor = db.rawQuery(sql, null);

        while(cursor.moveToNext())
        {
            check_value = Integer.parseInt(cursor.getString(cursor.getColumnIndex("service_on_off")));

            //Toast.makeText(getActivity(), "check 1 : "+check_value, Toast.LENGTH_SHORT).show();
        }

        return check_value;
    }

    public int Check_Service_3_On_Off()
    {
        int check_value = 0; //센서가 꺼져있다는 가정//

        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "SELECT service_on_off FROM service_info WHERE service_number = 8;"; //전체출력 SQL.//

        cursor = db.rawQuery(sql, null);

        while(cursor.moveToNext())
        {
            check_value = Integer.parseInt(cursor.getString(cursor.getColumnIndex("service_on_off")));

            //Toast.makeText(getActivity(), "check 1 : "+check_value, Toast.LENGTH_SHORT).show();
        }

        return check_value;
    }

    public String get_light_1_location(String user_id)
    {
        String location_name = null;

        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "SELECT light_1_location FROM home_info WHERE user_id = '"+user_id+"';"; //전체출력 SQL.//

        cursor = db.rawQuery(sql, null);

        while(cursor.moveToNext())
        {
            location_name  = cursor.getString(cursor.getColumnIndex("light_1_location"));

            //Toast.makeText(getActivity(), "check 1 : "+check_value, Toast.LENGTH_SHORT).show();
        }

        return location_name;
    }

    public String get_light_2_location(String user_id)
    {
        String location_name = null;

        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "SELECT light_2_location FROM home_info WHERE user_id = '"+user_id+"';"; //전체출력 SQL.//

        cursor = db.rawQuery(sql, null);

        while(cursor.moveToNext())
        {
            location_name  = cursor.getString(cursor.getColumnIndex("light_2_location"));

            //Toast.makeText(getActivity(), "check 1 : "+check_value, Toast.LENGTH_SHORT).show();
        }

        return location_name;
    }

    public String get_light_3_location(String user_id)
    {
        String location_name = null;

        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "SELECT light_3_location FROM home_info WHERE user_id = '"+user_id+"';"; //전체출력 SQL.//

        cursor = db.rawQuery(sql, null);

        while(cursor.moveToNext())
        {
            location_name  = cursor.getString(cursor.getColumnIndex("light_3_location"));

            //Toast.makeText(getActivity(), "check 1 : "+check_value, Toast.LENGTH_SHORT).show();
        }

        return location_name;
    }

    //비동기 태스트 작업 수행//
    public class LED_Service_NetworkTask_1 extends AsyncTask<Void, String, Void>
    {
        String dstAddress;
        int dstPort;
        String user_phone_number;
        String response;

        LED_Service_NetworkTask_1(String addr, int port, String user_phone_number)
        {
            dstAddress = addr;
            dstPort = port;
            this.user_phone_number = user_phone_number;

            Log.i("IP / PORT :", ""+dstAddress+"/"+dstPort);
        }

        protected void onPreExecute()
        {
            //Toast.makeText(getApplicationContext(), "Start passing Sensor 1 value information", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            try
            {
                while(true)
                {
                    if(isCancelled())
                    {
                        break;
                    }

                    Socket socket = new Socket(dstAddress, dstPort);

                    InputStream inputStream = socket.getInputStream();
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                    byte[] buffer = new byte[1024];

                    //5번 서비스 이용. 1번 아두이노의 1번 센서(조도 1센서)에서 데이터 센싱.//
                    trans_data_format += "5" +"/" + "1" + "/" + "" + "1" + "/" +"G" +".";

                    out.println(trans_data_format);

                    trans_data_format = "";

                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != -1)
                    {
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                    }

                    socket.close();

                    response = byteArrayOutputStream.toString("UTF-8");

                    publishProgress(response); //사용자 UI로 전송//

                    Thread.sleep(10000); //5초간격으로 센싱//
                }
            }

            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }

            catch (IOException e)
            {
                e.printStackTrace();
            }

            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        protected void onProgressUpdate(String... values)
        {
            int sensor_value;

            try
            {
                String new_sensor_value = values[0].substring(0, values[0].length()-1);

                sensor_value = Integer.parseInt(new_sensor_value.toString());

                light_1_progress.setProgress(sensor_value);

                light_1_info.setText(sensor_value+"%");

				if(sensor_value > 100 && sensor_1_check_value_dark == 0)
				{
					SmsManager mSmsManager = SmsManager.getDefault();

					mSmsManager.sendTextMessage(this.user_phone_number, null, light_position_1_text.getText()+"에 불이 꺼졌습니다.", null, null);

					sensor_1_check_value_dark = 1;
					sensor_1_check_value_light = 0;
				}

				else if(sensor_value < 100 && sensor_1_check_value_light == 0)
				{
					SmsManager mSmsManager = SmsManager.getDefault();

					mSmsManager.sendTextMessage(this.user_phone_number, null, light_position_1_text.getText()+"에 불이 켜졌습니다.", null, null);

					sensor_1_check_value_dark = 0;
					sensor_1_check_value_light = 1;
				}
            }

            catch(StringIndexOutOfBoundsException e)
            {
                Log.i("ignore error", ""+e.getMessage());
            }

            catch(NumberFormatException e)
            {
                Log.i("ignore error", ""+e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(Void result)
        {

        }

        protected void onCancelled(Void result)
        {
            //Toast.makeText(getApplicationContext(), "Real-time sensor 1 values collected shutdown...", Toast.LENGTH_SHORT).show();
        }
    }

    public class LED_Service_NetworkTask_2 extends AsyncTask<Void, String, Void>
    {
        String dstAddress;
        int dstPort;
        String user_phone_number;
        String response;

        LED_Service_NetworkTask_2(String addr, int port, String user_phone_number)
        {
            dstAddress = addr;
            dstPort = port;
            this.user_phone_number = user_phone_number;

            Log.i("IP / PORT :", ""+dstAddress+"/"+dstPort);
        }

        protected void onPreExecute()
        {
            //Toast.makeText(getApplicationContext(), "Start passing Sensor 1 value information", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            try
            {
                while(true)
                {
                    if(isCancelled())
                    {
                        break;
                    }

                    Socket socket = new Socket(dstAddress, dstPort);

                    InputStream inputStream = socket.getInputStream();
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                    byte[] buffer = new byte[1024];

                    //5번 서비스 이용. 1번 아두이노의 2번 센서(조도 2센서)에서 데이터 센싱.//
                    trans_data_format += "5" +"/" + "1" + "/" + "" + "2" + "/" +"G" +".";

                    out.println(trans_data_format);

                    trans_data_format = "";

                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != -1)
                    {
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                    }

                    socket.close();

                    response = byteArrayOutputStream.toString("UTF-8");

                    publishProgress(response); //사용자 UI로 전송//

                    Thread.sleep(15000); //7초간격으로 센싱. 조도센서 1보다 2초 뒤//
                }
            }

            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }

            catch (IOException e)
            {
                e.printStackTrace();
            }

            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        protected void onProgressUpdate(String... values)
        {
            int sensor_value;

            try
            {
                String new_sensor_value = values[0].substring(0, values[0].length()-1);

                sensor_value = Integer.parseInt(new_sensor_value.toString());

                light_2_progress.setProgress(sensor_value);

                light_2_info.setText(sensor_value+"%");

                if(sensor_value > 100 && sensor_2_check_value_dark == 0)
                {
                    SmsManager mSmsManager = SmsManager.getDefault();

                    mSmsManager.sendTextMessage(this.user_phone_number, null, light_position_2_text.getText()+"에 불이 꺼졌습니다.", null, null);

                    sensor_2_check_value_dark = 1;
                    sensor_2_check_value_light = 0;
                }

                else if(sensor_value < 100 && sensor_2_check_value_light == 0)
                {
                    SmsManager mSmsManager = SmsManager.getDefault();

                    mSmsManager.sendTextMessage(this.user_phone_number, null, light_position_2_text.getText()+"에 불이 켜졌습니다.", null, null);

                    sensor_2_check_value_dark = 0;
                    sensor_2_check_value_light = 1;
                }
            }

            catch(StringIndexOutOfBoundsException e)
            {
                Log.i("ignore error", ""+e.getMessage());
            }

            catch(NumberFormatException e)
            {
                Log.i("ignore error", ""+e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(Void result)
        {

        }

        protected void onCancelled(Void result)
        {
            //Toast.makeText(getApplicationContext(), "Real-time sensor 1 values collected shutdown...", Toast.LENGTH_SHORT).show();
        }
    }

    public class LED_Service_NetworkTask_3 extends AsyncTask<Void, String, Void>
    {
        String dstAddress;
        int dstPort;
        String user_phone_number;
        String response;

        LED_Service_NetworkTask_3(String addr, int port, String user_phone_number)
        {
            dstAddress = addr;
            dstPort = port;
            this.user_phone_number = user_phone_number;

            Log.i("IP / PORT :", ""+dstAddress+"/"+dstPort);
        }

        protected void onPreExecute()
        {
            //Toast.makeText(getApplicationContext(), "Start passing Sensor 1 value information", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            try
            {
                while(true)
                {
                    if(isCancelled())
                    {
                        break;
                    }

                    Socket socket = new Socket(dstAddress, dstPort);

                    InputStream inputStream = socket.getInputStream();
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                    byte[] buffer = new byte[1024];

                    //5번 서비스 이용. 1번 아두이노의 3번 센서(조도 3센서)에서 데이터 센싱.//
                    trans_data_format += "5" +"/" + "1" + "/" + "" + "3" + "/" +"G" +".";

                    out.println(trans_data_format);

                    trans_data_format = "";

                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != -1)
                    {
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                    }

                    socket.close();

                    response = byteArrayOutputStream.toString("UTF-8");

                    publishProgress(response); //사용자 UI로 전송//

                    Thread.sleep(20000); //5초간격으로 센싱//
                }
            }

            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }

            catch (IOException e)
            {
                e.printStackTrace();
            }

            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        protected void onProgressUpdate(String... values)
        {
            int sensor_value;

            try
            {
                String new_sensor_value = values[0].substring(0, values[0].length()-1);

                sensor_value = Integer.parseInt(new_sensor_value.toString());

                light_3_progress.setProgress(sensor_value);

                light_3_info.setText(sensor_value+"%");

                if(sensor_value > 100 && sensor_3_check_value_dark == 0)
                {
                    SmsManager mSmsManager = SmsManager.getDefault();

                    mSmsManager.sendTextMessage(this.user_phone_number, null, light_position_3_text.getText()+"에 불이 꺼졌습니다.", null, null);

                    sensor_3_check_value_dark = 1;
                    sensor_3_check_value_light = 0;
                }

                else if(sensor_value < 100 && sensor_3_check_value_light == 0)
                {
                    SmsManager mSmsManager = SmsManager.getDefault();

                    mSmsManager.sendTextMessage(this.user_phone_number, null, light_position_3_text.getText()+"에 불이 켜졌습니다.", null, null);

                    sensor_3_check_value_dark = 0;
                    sensor_3_check_value_light = 1;
                }
            }

            catch(StringIndexOutOfBoundsException e)
            {
                Log.i("ignore error", ""+e.getMessage());
            }

            catch(NumberFormatException e)
            {
                Log.i("ignore error", ""+e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(Void result)
        {

        }

        protected void onCancelled(Void result)
        {
            //Toast.makeText(getApplicationContext(), "Real-time sensor 1 values collected shutdown...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId(); //액션바에 선택된 아이템의 id를 가져온다.//

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInit(int status)
    {

    }
}
