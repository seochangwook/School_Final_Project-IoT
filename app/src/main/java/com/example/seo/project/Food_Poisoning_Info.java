package com.example.seo.project;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

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
public class Food_Poisoning_Info extends Activity
{
    String server_ip_address;
    String server_port_number;
    String area_code;

    TextView date_text;
    TextView h3_text;
    TextView h6_text;
    TextView h9_text;

    ImageView h3_alarm_image;
    ImageView h6_alarm_image;
    ImageView h9_alarm_image;

    Service_NetworkTask service_1_task; //서비스 작업 1(백그라운드로 수행)//

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.food_poisoning_activity_layout);

        date_text = (TextView)findViewById(R.id.date_text);
        h3_text = (TextView)findViewById(R.id.h3_text);
        h6_text = (TextView)findViewById(R.id.h6_text);
        h9_text = (TextView)findViewById(R.id.h9_text);
        h3_alarm_image = (ImageView)findViewById(R.id.alarm_h3_image);
        h6_alarm_image = (ImageView)findViewById(R.id.alarm_h6_image);
        h9_alarm_image = (ImageView)findViewById(R.id.alarm_h9_image);

        Intent intent = getIntent();

        server_ip_address = intent.getStringExtra("KEY_IP_ADDRESS");
        server_port_number = intent.getStringExtra("KEY_PORT_NUMBER");
        area_code = intent.getStringExtra("KEY_AREA_CODE");

        int port_number = Integer.parseInt(server_port_number.toString());

        service_1_task = new Service_NetworkTask(server_ip_address, port_number);

        service_1_task.execute(); //네트워크 작업을 수행.//
    }

    public class Service_NetworkTask extends AsyncTask<Void, Void, Void>
    {

        String dstAddress;
        int dstPort;
        String response;

        //IP와 PORT번호를 설정.//
        Service_NetworkTask(String addr, int port)
        {
            dstAddress = addr;
            dstPort = port;

            Log.i("IP / PORT :", "" + dstAddress + "/" + dstPort);
        }

        @Override
        protected Void doInBackground(Void... arg0) //백그라운드로 데이터 송수신 작업 진행.//
        {
            try
            {
                Socket socket = new Socket(dstAddress, dstPort); //서버소켓에 연결.//

                //데이터를 송수신할 스트림들을 정의.//
                InputStream inputStream = socket.getInputStream();
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];

                //데이터를 전송.서버로 부터 에코기능을 수행. 서비스 번호는 7//
                out.println("7/"+area_code);

                //데이터를 받을때까지 기다림.//
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1)
                {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }

                socket.close();

                //받아온 데이터 타입의 인코딩 방식을 결정.//
                response = byteArrayOutputStream.toString("UTF-8");
            }

            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }

            catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            int str_size = response.length();

            String time_value_array[] = response.split("/"); //구분자로 분리.//

            date_text.setText(time_value_array[0].trim());

            //각 시간대별로 제어.//
            for(int i=1; i<time_value_array.length; i++)
            {
                if(i==1)
                {
                    h3_text.setText(time_value_array[i].trim());

                    int value = Integer.parseInt(time_value_array[i].trim().toString());

                    //해당 값에 따른 위험도 이미지 표현.//
                    if(value > 0 && value <= 30) //정상범위.//
                    {
                        h3_alarm_image.setImageResource(R.drawable.nomal_alarm);
                    }

                    else if(value > 35 && value <= 70) //보통 범위.//
                    {
                        h3_alarm_image.setImageResource(R.drawable.middle_alarm);
                    }

                    else if(value > 70 && value <= 95) //위험범위.//
                    {
                        h3_alarm_image.setImageResource(R.drawable.high_alarm);
                    }
                }

                if(i==2)
                {
                    h6_text.setText(time_value_array[i].trim());

                    int value = Integer.parseInt(time_value_array[i].trim().toString());

                    //해당 값에 따른 위험도 이미지 표현.//
                    if(value > 0 && value <= 30) //정상범위.//
                    {
                        h6_alarm_image.setImageResource(R.drawable.nomal_alarm);
                    }

                    else if(value > 35 && value <= 70) //보통 범위.//
                    {
                        h6_alarm_image.setImageResource(R.drawable.middle_alarm);
                    }

                    else if(value > 70 && value <= 95) //위험범위.//
                    {
                        h6_alarm_image.setImageResource(R.drawable.high_alarm);
                    }
                }

                if(i==3)
                {
                    h9_text.setText(time_value_array[i].trim());

                    int value = Integer.parseInt(time_value_array[i].trim().toString());

                    //해당 값에 따른 위험도 이미지 표현.//
                    if(value > 0 && value <= 30) //정상범위.//
                    {
                        h9_alarm_image.setImageResource(R.drawable.nomal_alarm);
                    }

                    else if(value > 35 && value <= 70) //보통 범위.//
                    {
                        h9_alarm_image.setImageResource(R.drawable.middle_alarm);
                    }

                    else if(value > 70 && value <= 95) //위험범위.//
                    {
                        h9_alarm_image.setImageResource(R.drawable.high_alarm);
                    }
                }
            }

            super.onPostExecute(result);
        }
    }
}
