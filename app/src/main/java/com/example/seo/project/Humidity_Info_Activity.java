package com.example.seo.project;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
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
 * Created by Seo on 2016-04-15.
 */
public class Humidity_Info_Activity extends ActionBarActivity
{
    TextView server_info_text;
    TextView current_humi;

    TextView info_1;
    TextView info_2;
    TextView info_3;

    ProgressBar progressBar;

    private String server_ip_address;
    private String server_port_number;
    int port_number;

    String trans_data_format = "";

    //서비스//
    Service_NetworkTask sensor_task;

    FloatingActionButton info_1_button;
    FloatingActionButton info_2_button;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.humidity_info_activity_layout);

        server_info_text = (TextView)findViewById(R.id.server_address_info_text);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        info_1_button = (FloatingActionButton)findViewById(R.id.info_button_1);
        info_2_button = (FloatingActionButton)findViewById(R.id.info_button_2);
        current_humi = (TextView)findViewById(R.id.current_humi);
        info_1 = (TextView)findViewById(R.id.info_1);
        info_2 = (TextView)findViewById(R.id.info_2);
        info_3 = (TextView)findViewById(R.id.info_3);

        Intent intent = getIntent();

        server_ip_address = intent.getStringExtra("KEY_SERVER_IP");
        server_port_number = intent.getStringExtra("KEY_PORT_NUMBER");

        server_info_text.setText("Server IP : "+server_ip_address+" / Server PORT : "+server_port_number);

        port_number = Integer.parseInt(server_port_number);

        server_info_text.setText("Server IP : "+server_ip_address+" / Server PORT : "+server_port_number);

        sensor_task = new Service_NetworkTask(server_ip_address, port_number);

        sensor_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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

    public class Service_NetworkTask extends AsyncTask<Void, String, Void>
    {
        String dstAddress;
        int dstPort;
        String response;

        Service_NetworkTask(String addr, int port)
        {
            dstAddress = addr;
            dstPort = port;

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

                    //5번 서비스 이용, 습도센서를 이용한 데이터 센싱//
                    trans_data_format += "5" +"/" + "3" + "/" + "" + "3" + "/" +"G" +".";

                    out.println(trans_data_format);

                    trans_data_format = "";

                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != -1)
                    {
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                    }

                    socket.close();

                    response = byteArrayOutputStream.toString("UTF-8");

                    publishProgress(response);

                    Thread.sleep(5000); //14초에 한번씩 온도 갱신//
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

                progressBar.setProgress(sensor_value);

                current_humi.setText(sensor_value+"%");

                //해당 센서의 데이터값에 따른 조건분기//
                if(sensor_value >= 30 && sensor_value <= 40)
                {
                    info_1.setText("적절한 실내습도 입니다");
                    info_2.setText("쾌적함");
                    info_3.setText("1. 현재 습도를 유지하세요");
                }

                else
                {
                    info_1.setText("습도 계산중..");
                    info_2.setText("습도 계산중..");
                    info_3.setText("습도 계산중..");
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

    public void onStop()
    {
        super.onStop();

        sensor_task.cancel(true);

        finish();
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
}
