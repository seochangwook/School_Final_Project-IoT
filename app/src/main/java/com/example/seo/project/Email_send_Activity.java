package com.example.seo.project;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by Seo on 2016-04-02.
 */
public class Email_send_Activity extends Activity
{
    DB_Helper db_helper; //핼퍼클래스 정의.//
    SQLiteDatabase db; //데이터베이스 기능을 사용하기 위한 SQLite 사용.//
    Cursor cursor; //탐색을 할 커서정의.//
    String sql;

    EditText receiver_edit;
    EditText mail_content;

    Button send_button;

    String mail_story;
    String receiver_address;

    String user_id;
    String user_password;

    //실행 서비스 목록.//
    Service_Mail_Task service_mail_task;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_send_activity_layout);

        Intent intent = getIntent();

        user_id = intent.getStringExtra("KEY_ID");

        receiver_edit = (EditText)findViewById(R.id.receiver_address);
        mail_content = (EditText)findViewById(R.id.mail_content);
        send_button = (Button)findViewById(R.id.send_button);

        db_helper = new DB_Helper(this); //db초기화.//

        send_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                user_password = get_email_password(user_id); //구글 비밀번호를 가져온다.//

                mail_story = mail_content.getText().toString();
                receiver_address = receiver_edit.getText().toString();

                service_mail_task = new Service_Mail_Task(user_id,mail_story, receiver_address);

                service_mail_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
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
        String user_mail_address;
        String receiver_address;
        String mesage = "";

        Service_Mail_Task(String mail_addr, String mail_story, String receiver_address)
        {
            this.user_mail_address = mail_addr;
            this.mesage = mail_story;
            this.receiver_address = receiver_address;
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
                InternetAddress to = new InternetAddress(receiver_address);
                msg.setRecipient(Message.RecipientType.TO, to);

                // 이메일 제목, 인코딩 타입은 UTF-8로 설정.//
                msg.setSubject("Home Iot Connection Information", "UTF-8");

                //전송 메시지 설정.//
                mesage = "Date : "+time_now+"\n"+mesage;

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
            String pw = ""+user_password;// 구글 비밀번호(2단계 적용)//

            // ID와 비밀번호를 입력한다.
            pa = new PasswordAuthentication(id, pw);
        }

        // 시스템에서 사용하는 인증정보
        public PasswordAuthentication getPasswordAuthentication()
        {
            return pa; //인증정보를 반환.//
        }
    }
}
