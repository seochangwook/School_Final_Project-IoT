package com.example.seo.project;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ProgressBar;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Created by Seo on 2016-04-02.
*/
public class Loading_Activity extends Activity
{
    DB_Helper db_helper; //핼퍼클래스 정의.//
    SQLiteDatabase db; //데이터베이스 기능을 사용하기 위한 SQLite 사용.//
    Cursor cursor; //탐색을 할 커서정의.//
    String sql;

    private static final int PROGRESS = 0x1;
    private ProgressBar mProgress; //프로그래스바 정의.//
    private int mProgressStatus = 0; //프로그래스바의 상태값.//
    int i=0; //증가값 변수.//

    String user_id;
    String server_ip;
    String server_port;
    String email_password;
    String get_server_msg;

    TextView progress_status_text;

    //실행 서비스 목록.//
    Service_Mail_Task service_mail_task;
    Connect_NetworkTask myClientTask; //Connect Service//

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_activity_layout);

        mProgress = (ProgressBar)findViewById(R.id.progress_bar);
        progress_status_text = (TextView)findViewById(R.id.progress_bar_status_texts);

        Intent intent = getIntent();

        user_id = intent.getStringExtra("KEY_ID");
        server_ip = intent.getStringExtra("KEY_IP_ADDRESS");
        server_port = intent.getStringExtra("KEY_PORT_NUMBER");

        db_helper = new DB_Helper(this); //db초기화.//

        //프로그래스바는 상태에 대해서 변화해 가면서 동적으로 보여준는 것이기에 스레드가 필요.//
        //스레드 또한 이벤트 처리 처럼 무명 클래스로 처리하는것이 효율적이다.//
        new Thread(new Runnable() //이렇게 안할시 Runnable인터페이스를 implements해야한다.//
        {
            @Override
            public void run()
            {
                // TODO Auto-generated method stub
                //스레드로 수행할 작업을 명시.후에 네트워크도 이와같이 데이터 송수신 시 스레드를 이용해서 UI스레드와 연동해서 작업하게 된다.//
                while(mProgressStatus < 100)
                {
                    //스레드는 예외사항이 발생할 수 잇기에(인터럽트 관련)예외처리가 필요.//
                    try
                    {
                        Thread.sleep(30);
                    }

                    catch(InterruptedException e)
                    {

                    }

                    mProgressStatus = i++; //값을 증가시켜 상태를 변경.//

                    //앞서서 run()이 있었다. 이 스레드는 작업 스레드로서 네트워크나 데이터베이스 등 시스템적인 스레드였다.자바에서 스레드 규칙 상
                    //다른 외부 스레드 작업이 현재 UI스레드에 영향을 미치면 안된다는 것이다. 이유는 UI스레드와 같이 동작하게 되면 속도가 느려지고,
                    //어플리케이션이 다운될 수 있다. 따라서 스레드 작업에 대한 Runnable을 UI스레드에 전송해야 하는데 이것이 post()이다.
                    //개념은 핸들러가 된다. 또한 이 방식 말고도 AsyncTask<>라는 탬플릿이 존재하여 이 작업을 더 간단하게 해준다. 그러나 네트워크 통신 시
                    //이렇게 post()로 해주게 된다.//
                    mProgress.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mProgress.setProgress(mProgressStatus); //프로그래스의 상태변경.//

                            //상태에 따른 메시지 출력.//
                            if(mProgressStatus == 40)
                            {
                                progress_status_text.setText("Checking the server status ...");
                            }

                            else if(mProgressStatus == 60)
                            {
                                progress_status_text.setText("Access e-mail sent ...");

                                //메일을 전송하는 서비스 수행.//
                                //기존 핸들러 내부에서도 스레드를 생성해서 작업가능.//
                                email_password = get_email_password(user_id); //구글 비밀번호를 가져온다.//

                                service_mail_task = new Service_Mail_Task(user_id, server_ip);

                                service_mail_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }

                            else if(mProgressStatus == 90)
                            {
                                progress_status_text.setText("During server access ...");

                                int port_number = Integer.parseInt(server_port);

                                myClientTask = new Connect_NetworkTask(server_ip, port_number);

                                myClientTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }

                            else if(mProgressStatus == 100) //프로그래스바가 모두 상태가 변화햇을 떄.//
                            {
                                Intent intent=new Intent(); //다시 값을 보내주기 위해서 인텐트 정의.//
                                intent.putExtra("INPUT_TEXT", ""+mProgressStatus); //문자열로 데이터 넘김.//

                                setResult(RESULT_OK, intent); //현재의 인텐트 값을 콜벡 메소드인 onActivityResult로 반환한다.//

                                finish();
                            }
                        }
                    });
                }
            }

        }).start(); //스레드 작업을 전체적으로 시작.//
    }

    public class Connect_NetworkTask extends AsyncTask<Void, Void, Void>
    {
        String dstAddress;
        int dstPort;
        String response;

        Connect_NetworkTask(String addr, int port)
        {
            dstAddress = addr;
            dstPort = port;

            Log.i("IP / PORT :", ""+dstAddress+"/"+dstPort);
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            try
            {
                Socket socket = new Socket(dstAddress, dstPort);

                InputStream inputStream = socket.getInputStream();
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];

                out.println("connect");

                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1)
                {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }

                socket.close();

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

            get_server_msg = response.substring(0, str_size-1);

            if(get_server_msg.trim().equals("success"))
            {
                Toast.makeText(getApplicationContext(), server_ip+"/"+server_port+" Server Connect", Toast.LENGTH_SHORT).show();
            }

            super.onPostExecute(result);
        }
    }

    public String get_email_password(String user_email_address)
    {
        String email_password = null;

        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        //조건절을 주어서 해당 아이디에 맞는 비밀번호를 찾는다.//
        sql = "SELECT mail_password FROM home_info WHERE user_id = '"+user_email_address+"';";

        cursor = db.rawQuery(sql, null);

        while(cursor.moveToNext())
        {
            email_password = cursor.getString(0);

            //Toast.makeText(getApplicationContext(), ""+email_password, Toast.LENGTH_SHORT).show();
        }

        return email_password;
    }

    public class Service_Mail_Task extends AsyncTask<Void, Void, Void>
    {
        String server_ip_address;
        String user_mail_address;
        String mesage = "";

        Service_Mail_Task(String mail_addr, String server_ip)
        {
            server_ip_address = server_ip;
            user_mail_address = mail_addr;
        }

        /*protected void onPreExecute()
        {
            Toast.makeText(getApplicationContext(), "Sending e-mail contact information...", Toast.LENGTH_SHORT).show();
        }*/

        @Override
        protected Void doInBackground(Void... arg0) //백그라운드로 데이터 송수신 작업 진행.//
        {
            //메일 전송을 위한 기본 포맷을 정의.//
            Properties p = System.getProperties();

            p.put("mail.smtp.starttls.enable", "true");//메일전송규약은 SMTP프로토콜을 따른다.//
            p.put("mail.smtp.host", "smtp.gmail.com");// smtp 서버 주소 -> 구글메일.//
            p.put("mail.smtp.auth", "true");// 인증허용.//
            p.put("mail.smtp.port", "587");// 포트설정.//

            Authenticator auth = new MyAuthentication(); //인증을 위한 절차.//
            //인증은 구글의 일반 비밀번호가 아닌 2단계의 비밈번호가 필요.//
            //후에 사용자 등록과정에서 구글과 연동됨을 보이고, 사용자로 하여금 구글에 가입하도록 유도.//

            // session 생성 및  MimeMessage생성
            //메일을 구성하기 위한 여러가지 클래스 선언.//
            //MIME은 전자우편 포맷이다.//
            Session session = Session.getDefaultInstance(p, auth);
            MimeMessage msg = new MimeMessage(session);

            try
            {
                long now = System.currentTimeMillis(); //현재 시간을 구하기 위해서 사용.유닉스 기반의 Time사용//
                Date date = new Date(now);

                SimpleDateFormat time_format = new SimpleDateFormat("yyyy/MM/d HH:mm:ss");
                String time_now = time_format.format(date);

                //SMTP방식으로 해당 사용자 메일기반으로 보낸다.//
                msg.setSentDate(new Date());

                InternetAddress from = new InternetAddress();

                from = new InternetAddress(user_mail_address); //도메인 주소로 입력.//
                // 이메일 발신자
                msg.setFrom(from);

                // 이메일 수신자
                InternetAddress to = new InternetAddress(user_mail_address);
                msg.setRecipient(Message.RecipientType.TO, to);

                // 이메일 제목, 인코딩 타입은 UTF-8로 설정.//
                msg.setSubject("Home Iot Connection Information", "UTF-8");

                //전송 메시지 설정.//
                mesage = mesage+"Date : "+time_now+"\n"+"/ IP Address : "+server_ip_address+
                        "\n"+"/ Good Connecion";

                // 이메일 내용
                msg.setText(mesage, "UTF-8");

                // 이메일 헤더 - html형식의 헤더를 구성.//
                //내부적으로 html코드로 만들어서 전송.//
                msg.setHeader("content-Type", "text/html");

                // 메일보내기 -> 해당 send()과정은 네트워크 작업이므로 UI스레드와 분리되야 하기에 AsyncTask<>로 수행.//
                javax.mail.Transport.send(msg);
            }

            catch (AddressException addr_e)
            {
                addr_e.printStackTrace();
            }

            catch (MessagingException msg_e)
            {
                msg_e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            Toast.makeText(getApplicationContext(), "Complete mail delivery", Toast.LENGTH_SHORT).show();

            super.onPostExecute(result);
        }
    }

    class MyAuthentication extends Authenticator
    {
        PasswordAuthentication pa; //인증 클래스 선언.//

        public MyAuthentication()
        {
            String id = user_id;// 구글 ID
            String pw = ""+email_password;// 구글 비밀번호(2단계 적용)//

            // ID와 비밀번호를 입력한다.
            pa = new PasswordAuthentication(id, pw);
        }

        // 시스템에서 사용하는 인증정보
        public PasswordAuthentication getPasswordAuthentication()
        {
            return pa; //인증정보를 반환.//
        }
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
}
