package com.example.seo.project;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
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
 * Created by Seo on 2016-04-07.
 */
public class Service_Activity_Motion extends Service
{
    int service_number;
    String service_number_str;

    public Service_NetworkTask_1 sensor_task_1;

    public String ip_address;
    public int port_number;
    public String port_number_str;
    public String trans_data_format = "";

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO Auto-generated method stub
        Log.i("Service Log", "service onBind()");

        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.i("Service Log", "service onCreate()");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        Log.i("Service Log", "service 1 onDestroy()");

        sensor_task_1.cancel(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStart(intent, startId);

        Log.i("Service Log", "service onStartCommand()");

        ip_address=(String) intent.getExtras().get("KEY_IP_ADDRESS");
        port_number_str = (String)intent.getExtras().get("KEY_PORT_NUMBER");
        service_number_str = (String)intent.getExtras().get("KEY_SERVICE_NUMBER");

        port_number = Integer.parseInt(port_number_str.toString());
        service_number = Integer.parseInt(service_number_str.toString());

        if(service_number == 1)
        {
            //Toast.makeText(getApplicationContext(), "" + service_number, Toast.LENGTH_SHORT).show();

            Log.i("Service Log", "service 1 start!!");

            sensor_task_1 = new Service_NetworkTask_1(ip_address, port_number);

            sensor_task_1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public class Service_NetworkTask_1 extends AsyncTask<Void, String, Void>
    {
        String dstAddress;
        int dstPort;
        String response;

        Service_NetworkTask_1(String addr, int port)
        {
            dstAddress = addr;
            dstPort = port;

            Log.i("IP / PORT :", ""+dstAddress+"/"+dstPort);
        }

        protected void onPreExecute()
        {
            Toast.makeText(getApplicationContext(), "방범센서 작동", Toast.LENGTH_SHORT).show();
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

                    //5번 서비스 이용, 3번 아두이노의 1번센서(모션센서)로 부터 센싱한다.//
                    trans_data_format += "5" +"/" + "3" + "/" + "" + "1" + "/" +"G" +".";

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

                    Thread.sleep(13000);
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        protected void onProgressUpdate(String... values)
        {
            Log.i("service log :", "service 1 value : "+values[0]);

            //잘못된 타입의 문자가 들어올 경우.//

            try
            {
                int sensor_value = Integer.parseInt(values[0].toString().trim());

                if (sensor_value == 1)
                {
                    SmsManager mSmsManager = SmsManager.getDefault();

                    mSmsManager.sendTextMessage("01042084757", null, "누군가 현관문 앞에 있습니다. CCTV로 확인하세요", null, null);
                }
            }

            catch (NumberFormatException e)
            {
                Log.i("ERROR Message : ", values[0]);
            }
        }

        @Override
        protected void onPostExecute(Void result)
        {

        }

        protected void onCancelled(Void result)
        {
            Toast.makeText(getApplicationContext(), "방범센서 해제", Toast.LENGTH_SHORT).show();
        }
    }
}
