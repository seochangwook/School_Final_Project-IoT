package com.example.seo.project;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Seo on 2016-04-17.
 */
public class Voice_Activity extends Activity implements TextToSpeech.OnInitListener
{
    DB_Helper db_helper; //핼퍼클래스 정의.//
    SQLiteDatabase db; //데이터베이스 기능을 사용하기 위한 SQLite 사용.//
    Cursor cursor; //탐색을 할 커서정의.//
    String sql;

    TextToSpeech myTTS;

    TextView server_info_text;
    TextView user_id_text;
    TextView light_1_text;
    TextView light_2_text;
    TextView light_3_text;
    TextView motor_1_text;
    TextView fire_sensor_text;
    TextView motion_sensor_text;

    Button mike_button;

    private String server_ip_address;
    private String server_port_number;
    private String user_id;
    String anounce_data = ""; //출력할 음성 데이터.//

    private final int MY_UI=1001; //초기화 정수값 설정.//
    private ArrayList<String> mResult; //음성인식 결과가 저장되는 동적배열.//
    private String mSelectedString; //사용자가 선택한 문자열.//
    private String result_text; //결과문자열 저장.//

    String light_location_1;
    String light_location_2;
    String light_location_3;
    String motor_location_1;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_activity_layout);

        server_info_text = (TextView)findViewById(R.id.server_address_info_text);
        user_id_text = (TextView)findViewById(R.id.user_id_text);
        mike_button = (Button)findViewById(R.id.voice_button);
        light_1_text = (TextView)findViewById(R.id.light_1);
        light_2_text = (TextView)findViewById(R.id.light_2);
        light_3_text = (TextView)findViewById(R.id.light_3);
        motor_1_text = (TextView)findViewById(R.id.motor_1);
        fire_sensor_text = (TextView)findViewById(R.id.fire_sensor);
        motion_sensor_text = (TextView)findViewById(R.id.motion_sensor);

        myTTS = new TextToSpeech(this, this); //TTS객체 선언.TTS엔진을 초기화.//
        db_helper = new DB_Helper(this);

        anounce_data = "음성 서비스를 시작합니다. 주의사항을 반드시 숙지하세요.";
        myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
        anounce_data = "";

        Intent intent = getIntent();

        server_ip_address = intent.getStringExtra("KEY_SERVER_IP");
        server_port_number = intent.getStringExtra("KEY_PORT_NUMBER");
        user_id = intent.getStringExtra("KEY_USER_ID");

        server_info_text.setText("* Server IP : "+server_ip_address+" / Server PORT : "+server_port_number);
        user_id_text.setText("* USER ID : "+user_id);

        //각 센서들의 위치정보 초기화. 프로토타입 수준이므로 한글의 자음수준으로 정의함.//
        light_location_1 = get_Sensor_location(1);
        light_location_2 = get_Sensor_location(2);
        light_location_3 = get_Sensor_location(3);
        motor_location_1 = get_Sensor_location(4);

        light_1_text.setText("* '가' : "+light_location_1+" ON / '나' : "+light_location_1+" OFF");
        light_2_text.setText("* '다' : "+light_location_2+" ON / '라' : "+light_location_2+" OFF");
        light_3_text.setText("* '마' : "+light_location_3+" ON / '바' : "+light_location_3+" OFF");
        fire_sensor_text.setText("* '사' : 화재감지 센서 동작 / '아' : 화재감지 센서 동작 해제");
        motion_sensor_text.setText("* '자' : 모션감지 센서 동작 / '차' : 모션감지 센서 동작 해제");

        if(motor_location_1.equals("현관문"))
        {
            motor_1_text.setText("* '카' : " + motor_location_1 + " ON / '타' : " + motor_location_1 + " OFF" + " (주의)");
        }

        else
        {
            motor_1_text.setText("* '카' : " + motor_location_1 + " ON / '타' : " + motor_location_1 + " OFF");
        }

        mike_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //음성인식 기능 적용.//
                startActivityForResult(new Intent(Voice_Activity.this, CustomUIActivity.class), MY_UI);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if( resultCode == RESULT_OK  && requestCode == MY_UI ) //MY_UI액티비티에서 온 결과이고, 유효한 결과가 올 경우.//
        {
            //결과가 있으면
            show_Recognition_results(requestCode, data); //결과를 출력.//
        }

        else //결과가 유효하지 않을 경우.//
        {
            //결과가 없으면 에러 메시지 출력
            String msg = null;

            //오류코드 분류.//
            switch(resultCode)
            {
                case SpeechRecognizer.ERROR_AUDIO:
                    msg = "오디오 입력 중 오류가 발생했습니다.";
                    break;

                case SpeechRecognizer.ERROR_CLIENT:
                    msg = "단말에서 오류가 발생했습니다.";
                    break;

                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    msg = "권한이 없습니다.";
                    break;

                case SpeechRecognizer.ERROR_NETWORK:

                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    msg = "네트워크 오류가 발생했습니다.";
                    break;

                case SpeechRecognizer.ERROR_NO_MATCH:
                    //msg = "일치하는 항목이 없습니다.";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    msg = "음성인식 서비스가 과부하 되었습니다.";
                    break;

                case SpeechRecognizer.ERROR_SERVER:
                    msg = "서버에서 오류가 발생했습니다.";
                    break;

                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    msg = "입력이 없습니다.";
                    break;
            }

            if(msg != null)	//오류 메시지가 null이 아니면 메시지 출력
            {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    //결과 list 출력하는 다이얼로그 생성
    private void show_Recognition_results(int requestCode, Intent data)
    {
        String key = "";

        if(requestCode == MY_UI)
        {
            key = SpeechRecognizer.RESULTS_RECOGNITION;	//키값 설정
        }

        mResult = data.getStringArrayListExtra(key);//인식된 데이터 list 받아옴.
        String[] result = new String[mResult.size()];	//배열생성. 다이얼로그에서 출력하기 위해
        mResult.toArray(result);						//	list 배열로 변환

        Log.i("음성인식 결과 :", "" + result[0]); //첫번째로 인식된 결과가 가장 유사도가 높은 결과이므로 첫번째 출력을 중심으로 함.//

        if(result[0].equals("가")) //인식된 문자로 서비스 비교. -> 다음에 분기문을 가지고 여러 서비스를 실행.//
        {
            anounce_data = light_location_1+" 불이 켜집니다.";
            myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
            anounce_data = "";

            //서비스를 on했다는 정보를 DB에 갱신.//
            Update_DB_light_service_1_on();

            //점등 서비스 실행. LED ON/OFF//
            Intent service_intent = new Intent(Voice_Activity.this, Service_Activity_LED_1_ON.class);

            service_intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
            service_intent.putExtra("KEY_PORT_NUMBER", "" + server_port_number);
            service_intent.putExtra("KEY_SERVICE_NUMBER", "1");

            startService(service_intent);

            //LED 서비스는 서비스를 바로 끝내주므로 종료를 한다.//
            stopService(service_intent);
        }

        else if(result[0].equals("나"))
        {
            anounce_data = light_location_1+ " 불이 꺼집니다.";
            myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
            anounce_data = "";

            Update_DB_light_service_1_off();

            Intent service_intent = new Intent(Voice_Activity.this, Service_Activity_LED_1_OFF.class);

            service_intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
            service_intent.putExtra("KEY_PORT_NUMBER", "" + server_port_number);
            service_intent.putExtra("KEY_SERVICE_NUMBER", "1");

            startService(service_intent);

            //LED 서비스는 서비스를 바로 끝내주므로 종료를 한다.//
            stopService(service_intent);
        }

        else if(result[0].equals("다"))
        {
            anounce_data = light_location_2+ " 불이 켜집니다.";
            myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
            anounce_data = "";

            //서비스를 on했다는 정보를 DB에 갱신.//
            Update_DB_light_service_2_on();

            //점등 서비스 실행. LED ON/OFF//
            Intent service_intent = new Intent(Voice_Activity.this, Service_Activity_LED_2_ON.class);

            service_intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
            service_intent.putExtra("KEY_PORT_NUMBER", "" + server_port_number);
            service_intent.putExtra("KEY_SERVICE_NUMBER", "1");

            startService(service_intent);

            //LED 서비스는 서비스를 바로 끝내주므로 종료를 한다.//
            stopService(service_intent);
        }

        else if(result[0].equals("라"))
        {
            anounce_data = light_location_2+ " 불이 꺼집니다.";
            myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
            anounce_data = "";

            Update_DB_light_service_2_off();

            //점등 서비스 실행. LED ON/OFF//
            Intent service_intent = new Intent(Voice_Activity.this, Service_Activity_LED_2_OFF.class);

            service_intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
            service_intent.putExtra("KEY_PORT_NUMBER", "" + server_port_number);
            service_intent.putExtra("KEY_SERVICE_NUMBER", "1");

            startService(service_intent);

            //LED 서비스는 서비스를 바로 끝내주므로 종료를 한다.//
            stopService(service_intent);
        }

        else if(result[0].equals("마"))
        {
            anounce_data = light_location_3+ " 불이 켜집니다.";
            myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
            anounce_data = "";

            //서비스를 on했다는 정보를 DB에 갱신.//
            Update_DB_light_service_3_on();

            //점등 서비스 실행. LED ON/OFF//
            Intent service_intent = new Intent(Voice_Activity.this, Service_Activity_LED_3_ON.class);

            service_intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
            service_intent.putExtra("KEY_PORT_NUMBER", "" + server_port_number);
            service_intent.putExtra("KEY_SERVICE_NUMBER", "1");

            startService(service_intent);

            //LED 서비스는 서비스를 바로 끝내주므로 종료를 한다.//
            stopService(service_intent);
        }

        else if(result[0].equals("바"))
        {
            anounce_data = light_location_3+" 불이 꺼집니다.";
            myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
            anounce_data = "";

            Update_DB_light_service_3_off();

            //점등 서비스 실행. LED ON/OFF//
            Intent service_intent = new Intent(Voice_Activity.this, Service_Activity_LED_3_OFF.class);

            service_intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
            service_intent.putExtra("KEY_PORT_NUMBER", "" + server_port_number);
            service_intent.putExtra("KEY_SERVICE_NUMBER", "1");

            startService(service_intent);

            //LED 서비스는 서비스를 바로 끝내주므로 종료를 한다.//
            stopService(service_intent);
        }

        else if(result[0].equals("사"))
        {
            anounce_data = "화재감지 센서 작동";
            myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
            anounce_data = "";

            Update_DB_service_2_on();

            Intent service_intent = new Intent(Voice_Activity.this, Service_Activity_Fire.class);

            service_intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
            service_intent.putExtra("KEY_PORT_NUMBER", ""+server_port_number);
            service_intent.putExtra("KEY_SERVICE_NUMBER", "1");

            startService(service_intent);
        }

        else if(result[0].equals("아"))
        {
            anounce_data = "화재감지 센서 해제";
            myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
            anounce_data = "";

            Update_DB_service_2_off();

            Intent service_intent = new Intent(Voice_Activity.this, Service_Activity_Fire.class);

            stopService(service_intent);
        }

        else if(result[0].equals("자"))
        {
            anounce_data = "현관문 방범센서 작동";
            myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
            anounce_data = "";

            //서비스를 on했다는 정보를 DB에 갱신.//
            Update_DB_service_1_on();

            Intent service_intent = new Intent(Voice_Activity.this, Service_Activity_Motion.class);

            service_intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
            service_intent.putExtra("KEY_PORT_NUMBER", ""+server_port_number);
            service_intent.putExtra("KEY_SERVICE_NUMBER", "1");

            startService(service_intent);
        }

        else if(result[0].equals("차"))
        {
            anounce_data = "현관문 방범센서 해제";
            myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
            anounce_data = "";

            Update_DB_service_1_off();

            Intent service_intent = new Intent(Voice_Activity.this, Service_Activity_Motion.class);

            stopService(service_intent);
        }

        else if(result[0].equals("카"))
        {
            anounce_data = motor_location_1+ "이 열립니다.";
            myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
            anounce_data = "";

            //서비스를 on했다는 정보를 DB에 갱신.//
            Update_DB_motor_service_1_on();

            Intent service_intent = new Intent(Voice_Activity.this, Service_Activity_Motor_1_ON.class);

            service_intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
            service_intent.putExtra("KEY_PORT_NUMBER", "" + server_port_number);
            service_intent.putExtra("KEY_SERVICE_NUMBER", "1");

            startService(service_intent);

            //모터는 서비스를 바로 끝내주므로 종료를 한다.//
            stopService(service_intent);
        }

        else if(result[0].equals("타"))
        {
            anounce_data = motor_location_1+ "이 닫힙니다.";
            myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
            anounce_data = "";

            Update_DB_motor_service_1_off();

            Intent service_intent = new Intent(Voice_Activity.this, Service_Activity_Motor_1_OFF.class);

            service_intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
            service_intent.putExtra("KEY_PORT_NUMBER", "" + server_port_number);
            service_intent.putExtra("KEY_SERVICE_NUMBER", "1");

            startService(service_intent);

            //모터는 서비스를 바로 끝내주므로 종료를 한다.//
            stopService(service_intent);
        }
    }

    public String get_Sensor_location(int sensor_number)
    {
        String location_name = null;

        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        if(sensor_number == 1)
        {
            sql = "SELECT light_1_location FROM home_info WHERE user_id = '"+user_id+"';"; //전체출력 SQL.//

            cursor = db.rawQuery(sql, null);

            while(cursor.moveToNext())
            {
                location_name  = cursor.getString(cursor.getColumnIndex("light_1_location"));

                //Toast.makeText(getActivity(), "check 1 : "+check_value, Toast.LENGTH_SHORT).show();
            }
        }

        else if(sensor_number == 2)
        {
            sql = "SELECT light_2_location FROM home_info WHERE user_id = '"+user_id+"';"; //전체출력 SQL.//

            cursor = db.rawQuery(sql, null);

            while(cursor.moveToNext())
            {
                location_name  = cursor.getString(cursor.getColumnIndex("light_2_location"));

                //Toast.makeText(getActivity(), "check 1 : "+check_value, Toast.LENGTH_SHORT).show();
            }
        }

        else if(sensor_number == 3)
        {
            sql = "SELECT light_3_location FROM home_info WHERE user_id = '"+user_id+"';"; //전체출력 SQL.//

            cursor = db.rawQuery(sql, null);

            while(cursor.moveToNext())
            {
                location_name  = cursor.getString(cursor.getColumnIndex("light_3_location"));

                //Toast.makeText(getActivity(), "check 1 : "+check_value, Toast.LENGTH_SHORT).show();
            }
        }

        else if(sensor_number == 4)
        {
            sql = "SELECT motor_1_location FROM home_info WHERE user_id = '"+user_id+"';"; //전체출력 SQL.//

            cursor = db.rawQuery(sql, null);

            while(cursor.moveToNext())
            {
                location_name  = cursor.getString(cursor.getColumnIndex("motor_1_location"));

                //Toast.makeText(getActivity(), "check 1 : "+check_value, Toast.LENGTH_SHORT).show();
            }
        }

        return location_name;
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

    public boolean dispatchTouchEvent(MotionEvent ev) //다이얼로그 뒷배경 터치시 꺼짐 방지.//
    {
        Rect dialogBounds = new Rect();
        getWindow().getDecorView().getHitRect(dialogBounds);

        if(!dialogBounds.contains((int)ev.getX(), (int)ev.getY()))
        {
            return false;
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onInit(int status)
    {

    }
}
