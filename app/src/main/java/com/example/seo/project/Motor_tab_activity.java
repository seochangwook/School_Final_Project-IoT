package com.example.seo.project;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created by Seo on 2016-03-17.
 */
public class Motor_tab_activity extends Fragment implements TextToSpeech.OnInitListener
{
    DB_Helper db_helper; //핼퍼클래스 정의.//
    SQLiteDatabase db; //데이터베이스 기능을 사용하기 위한 SQLite 사용.//
    Cursor cursor; //탐색을 할 커서정의.//
    String sql;

    TextToSpeech myTTS;

    private String server_ip_address;
    private int port_number;
    private String user_id;
    String anounce_data = ""; //출력할 음성 데이터.//
    //서비스가 3개이므로 각각의 확인변수를 둔다.//
    int service_motor_check_1;

    String motor_1_location;

    TextView server_info_text;
    TextView motor_location_1_text;

    Switch motor_1_service_switch;

    ImageView door_image;

    FloatingActionButton info_1_button;
    FloatingActionButton info_2_button;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.motor_tab_activity_layout, container, false);

        server_info_text = (TextView)rootView.findViewById(R.id.server_address_info_text);
        motor_1_service_switch = (Switch)rootView.findViewById(R.id.motor_1_service);
        info_1_button = (FloatingActionButton)rootView.findViewById(R.id.info_button_1);
        info_2_button = (FloatingActionButton)rootView.findViewById(R.id.info_button_2);
        door_image = (ImageView)rootView.findViewById(R.id.door_image);

        myTTS = new TextToSpeech(getActivity(), this); //TTS객체 선언.TTS엔진을 초기화.//
        db_helper = new DB_Helper(getActivity());

        //처음 탭 화면을 초기화 할 때 서비스의 설정 정보를 불러온다. (데이터베이스)//
        service_motor_check_1 = Check_Service_1_On_Off();

        if(service_motor_check_1 == 0) //off상태.//
        {
            motor_1_service_switch.setChecked(false);

            door_image.setImageResource(R.drawable.door_close);
        }

        else if(service_motor_check_1 == 1) //on상태.//
        {
            motor_1_service_switch.setChecked(true);

            door_image.setImageResource(R.drawable.door_open);
        }

        Bundle receive_data = getArguments(); //액티비티에서 넘어온 값을 받기 위해서 Bundle설정.//

        server_ip_address = receive_data.getString("SERVER_IP_ADDRESS_KEY");
        port_number = receive_data.getInt("PORT_NUMBER_KEY");
        user_id = receive_data.getString("USER_ID");

        server_info_text.setText("Server IP : "+server_ip_address+" / Server PORT : "+port_number);

        //데이터베이스에서 사용자가 지정한 모터의 위치 데이터를 불러온다.//
        motor_1_location = get_Motor_1_location_name(user_id);

        motor_1_service_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) //토글버튼이 ON상태일 경우.//
                {
                    anounce_data = motor_1_location.toString() + "이 열립니다.";
                    myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
                    anounce_data = "";

                    door_image.setImageResource(R.drawable.door_open);

                    //서비스를 on했다는 정보를 DB에 갱신.//
                    Update_DB_motor_service_1_on();

                    Intent service_intent = new Intent(getActivity(), Service_Activity_Motor_1_ON.class);

                    service_intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
                    service_intent.putExtra("KEY_PORT_NUMBER", "" + port_number);
                    service_intent.putExtra("KEY_SERVICE_NUMBER", "1");

                    getActivity().startService(service_intent);

                    SmsManager mSmsManager = SmsManager.getDefault();

                    mSmsManager.sendTextMessage("01042084757", null, "문이 열렸습니다!!", null, null);

                    //모터는 서비스를 바로 끝내주므로 종료를 한다.//
                    getActivity().stopService(service_intent);

                } else //토글버튼이 OFF상태일 경우.//
                {
                    anounce_data = motor_1_location.toString() + "이 닫힙니다.";
                    myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
                    anounce_data = "";

                    door_image.setImageResource(R.drawable.door_close);

                    Update_DB_motor_service_1_off();

                    Intent service_intent = new Intent(getActivity(), Service_Activity_Motor_1_OFF.class);

                    service_intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
                    service_intent.putExtra("KEY_PORT_NUMBER", "" + port_number);
                    service_intent.putExtra("KEY_SERVICE_NUMBER", "1");

                    getActivity().startService(service_intent);

                    SmsManager mSmsManager = SmsManager.getDefault();

                    mSmsManager.sendTextMessage("01042084757", null, "문이 닫혔습니다!!", null, null);

                    //모터는 서비스를 바로 끝내주므로 종료를 한다.//
                    getActivity().stopService(service_intent);
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

        return rootView;
    }

    private String get_Motor_1_location_name(String user_id)
    {
        String motor_1_location_name = null;

        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "SELECT motor_1_location FROM home_info WHERE user_id = '"+user_id+"';"; //전체출력 SQL.//

        cursor = db.rawQuery(sql, null);

        while(cursor.moveToNext())
        {
            motor_1_location_name  = cursor.getString(cursor.getColumnIndex("motor_1_location"));

            //Toast.makeText(getActivity(), "check 1 : "+check_value, Toast.LENGTH_SHORT).show();
        }

        return motor_1_location_name;
    }

    public int Check_Service_1_On_Off()
    {
        int check_value = 0; //센서가 꺼져있다는 가정//

        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "SELECT service_on_off FROM service_info WHERE service_number = 3;"; //전체출력 SQL.//

        cursor = db.rawQuery(sql, null);

        while(cursor.moveToNext())
        {
            check_value = Integer.parseInt(cursor.getString(cursor.getColumnIndex("service_on_off")));

            //Toast.makeText(getActivity(), "check 1 : "+check_value, Toast.LENGTH_SHORT).show();
        }

        return check_value;
    }

    public void Update_DB_motor_service_1_on()
    {
        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "UPDATE service_info SET service_on_off = 1 WHERE service_number = 3";

        db.execSQL(sql); //쿼리문 수행.//
    }

    public void Update_DB_motor_service_1_off()
    {
        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "UPDATE service_info SET service_on_off = 0 WHERE service_number = 3";

        db.execSQL(sql); //쿼리문 수행.//
    }

    @Override
    public void onInit(int status)
    {

    }
}
