package com.example.seo.project;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Seo on 2016-03-17.
 */
public class service_tab_activity extends Fragment implements TextToSpeech.OnInitListener
{
    DB_Helper db_helper; //핼퍼클래스 정의.//
    SQLiteDatabase db; //데이터베이스 기능을 사용하기 위한 SQLite 사용.//
    Cursor cursor; //탐색을 할 커서정의.//
    String sql;

    TextToSpeech myTTS;

    private String server_ip_address;
    private int port_number;
    String anounce_data = ""; //출력할 음성 데이터.//

    TextView server_info_text;

    Switch motion_service_switch;
    Switch fire_service_switch;
    Button cctv_button;

    FloatingActionButton info_1_button;
    FloatingActionButton info_2_button;

    //서비스가 2개이므로 각각의 확인변수를 둔다.//
    int service_on_off_check_1;
    int service_on_off_check_2;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.service_setting_activity_layout, container, false);

        server_info_text = (TextView) rootView.findViewById(R.id.server_address_info_text);
        info_1_button = (FloatingActionButton)rootView.findViewById(R.id.info_button_1);
        info_2_button = (FloatingActionButton)rootView.findViewById(R.id.info_button_2);
        motion_service_switch = (Switch)rootView.findViewById(R.id.motion_service);
        fire_service_switch = (Switch)rootView.findViewById(R.id.fire_service);
        cctv_button = (Button)rootView.findViewById(R.id.cctv_button);

        myTTS = new TextToSpeech(getActivity(), this); //TTS객체 선언.TTS엔진을 초기화.//
        db_helper = new DB_Helper(getActivity());

        //처음 탭 화면을 초기화 할 때 서비스의 설정 정보를 불러온다. (데이터베이스)//
        service_on_off_check_1 = Check_Service_1_On_Off();
        service_on_off_check_2 = Check_Service_2_On_Off();

        if(service_on_off_check_1 == 0) //off상태라면.//
        {
            //Toast.makeText(getActivity(), "서비스1이 off상태", Toast.LENGTH_SHORT).show();

            motion_service_switch.setChecked(false);
        }

        else if(service_on_off_check_1 == 1)
        {
            //Toast.makeText(getActivity(), "서비스1이 on상태", Toast.LENGTH_SHORT).show();

            motion_service_switch.setChecked(true);
        }

        if(service_on_off_check_2 == 0)
        {
            //Toast.makeText(getActivity(), "서비스2이 off상태", Toast.LENGTH_SHORT).show();

            fire_service_switch.setChecked(false);
        }

        else if(service_on_off_check_2 == 1)
        {
            //Toast.makeText(getActivity(), "서비스2이 on상태", Toast.LENGTH_SHORT).show();

            fire_service_switch.setChecked(true);
        }

        Bundle receive_data = getArguments(); //액티비티에서 넘어온 값을 받기 위해서 Bundle설정.//

        server_ip_address = receive_data.getString("SERVER_IP_ADDRESS_KEY");
        port_number = receive_data.getInt("PORT_NUMBER_KEY");

        server_info_text.setText("Server IP : "+server_ip_address+" / Server PORT : "+port_number);

        motion_service_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                // TODO Auto-generated method stub
                if (isChecked) //토글버튼이 ON상태일 경우.//
                {
                    anounce_data = "현관문 방범센서 작동";
                    myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
                    anounce_data = "";

                    //서비스를 on했다는 정보를 DB에 갱신.//
                    Update_DB_service_1_on();

                    Intent service_intent = new Intent(getActivity(), Service_Activity_Motion.class);

                    service_intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
                    service_intent.putExtra("KEY_PORT_NUMBER", ""+port_number);
                    service_intent.putExtra("KEY_SERVICE_NUMBER", "1");

                    getActivity().startService(service_intent);

                } else //토글버튼이 OFF상태일 경우.//
                {
                    anounce_data = "현관문 방범센서 해제";
                    myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
                    anounce_data = "";

                    Update_DB_service_1_off();

                    Intent service_intent = new Intent(getActivity(), Service_Activity_Motion.class);

                    getActivity().stopService(service_intent);
                }
            }
        });

        fire_service_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                // TODO Auto-generated method stub
                if (isChecked) //토글버튼이 ON상태일 경우.//
                {
                    anounce_data = "화재감지 센서 작동";
                    myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
                    anounce_data = "";

                    Update_DB_service_2_on();

                    Intent service_intent = new Intent(getActivity(), Service_Activity_Fire.class);

                    service_intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
                    service_intent.putExtra("KEY_PORT_NUMBER", ""+port_number);
                    service_intent.putExtra("KEY_SERVICE_NUMBER", "1");

                    getActivity().startService(service_intent);
                }

                else //토글버튼이 OFF상태일 경우.//
                {
                    anounce_data = "화재감지 센서 해제";
                    myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
                    anounce_data = "";

                    Update_DB_service_2_off();

                    Intent service_intent = new Intent(getActivity(), Service_Activity_Fire.class);

                    getActivity().stopService(service_intent);
                }
            }
        });

        cctv_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //외부앱 실행을 위한 포트번호를 알려주는 다이얼로그 띄우기//
                Intent intent = new Intent(getActivity(), CCTV_Connection_Activity.class);

                getActivity().startActivity(intent);
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
            public void onClick(View view)
            {
                Snackbar.make(view,
                        "1. 센서의 주의사항입니다." + "\n" ,
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        return rootView;
    }

    public int Check_Service_1_On_Off()
    {
        int check_value = 0; //센서가 꺼져있다는 가정//

        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "SELECT service_on_off FROM service_info WHERE service_number = 1;"; //전체출력 SQL.//

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

        sql = "SELECT service_on_off FROM service_info WHERE service_number = 2;"; //전체출력 SQL.//

        cursor = db.rawQuery(sql, null);

        while(cursor.moveToNext())
        {
            check_value = Integer.parseInt(cursor.getString(cursor.getColumnIndex("service_on_off")));

            //Toast.makeText(getActivity(), "check 2 : "+check_value, Toast.LENGTH_SHORT).show();
        }

        return check_value;
    }

    public void Update_DB_service_1_on()
    {
        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "UPDATE service_info SET service_on_off = 1 WHERE service_number = 1";

        db.execSQL(sql); //쿼리문 수행.//
    }

    public void Update_DB_service_1_off()
    {
        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "UPDATE service_info SET service_on_off = 0 WHERE service_number = 1";

        db.execSQL(sql); //쿼리문 수행.//
    }

    public void Update_DB_service_2_on()
    {
        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "UPDATE service_info SET service_on_off = 1 WHERE service_number = 2";

        db.execSQL(sql); //쿼리문 수행.//
    }

    public void Update_DB_service_2_off()
    {
        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "UPDATE service_info SET service_on_off = 0 WHERE service_number = 2";

        db.execSQL(sql); //쿼리문 수행.//
    }

    @Override
    public void onInit(int status)
    {

    }
}
