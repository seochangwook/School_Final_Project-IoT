package com.example.seo.project;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

/**
 * Created by Seo on 2016-03-30.
 */
public class Login_Activity extends ActionBarActivity implements TextToSpeech.OnInitListener
{
    TextToSpeech myTTS;

    private static final String TAG = "Smart Living";
    private boolean mResumed = false;
    private boolean mWriteMode = false;

    //연결확인을 위한 반환값.//
    static final int GET_STRING = 1;
    static final String STATUS_FINISH_VALUE = "100";

    DB_Helper db_helper; //핼퍼클래스 정의.//
    SQLiteDatabase db; //데이터베이스 기능을 사용하기 위한 SQLite 사용.//
    Cursor cursor; //탐색을 할 커서정의.//
    String sql;

    NfcAdapter mNfcAdapter;
    PendingIntent mNfcPendingIntent;
    IntentFilter[] mWriteTagFilters;
    IntentFilter[] mNdefExchangeFilters;

    EditText id_edit, password_edit, ip_edit;
    Switch NFC_ON_OFF_Switch;
    Button app_info_button, enroll_button;

    private BackPressCloseHandler backPressCloseHandler;

    String anounce_data = ""; //출력할 음성 데이터.//
    String server_ip;
    String server_port = "9000"; //포트 고정//

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_layout);

        id_edit = (EditText)findViewById(R.id.id_edit);
        password_edit = (EditText)findViewById(R.id.password_edit);
        ip_edit = (EditText)findViewById(R.id.ip_edit);
        NFC_ON_OFF_Switch = (Switch)findViewById(R.id.nfc_switch);
        app_info_button = (Button)findViewById(R.id.info_button);
        enroll_button = (Button)findViewById(R.id.enroll_button);

        myTTS = new TextToSpeech(this, this); //TTS객체 선언.TTS엔진을 초기화.//
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this); //NFC초기화.//
        db_helper = new DB_Helper(this); //db초기화.//

        // Handle all of our received NFC intents in this activity.
        //NFC는 PendingIntent로 처리한다.//
        mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Intent filters for reading a note from a tag or exchanging over p2p.
        //NDEF포맷의 데이터를 탐색 시 자동으로 호출.//
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try
        {
            ndefDetected.addDataType("text/plain");
        }

        catch (IntentFilter.MalformedMimeTypeException e)
        {

        }

        mNdefExchangeFilters = new IntentFilter[] { ndefDetected };

        // Intent filters for writing to a tag
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

        mWriteTagFilters = new IntentFilter[] { tagDetected };

        enroll_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Login_Activity.this, Enroll_1_Activity.class);
                startActivity(intent);
            }
        });

        NFC_ON_OFF_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                // TODO Auto-generated method stub
                if (isChecked) //토글버튼이 ON상태일 경우.//
                {
                    //Toast.makeText(getApplicationContext(), "NFC가 켜졌습니다.", Toast.LENGTH_SHORT).show();
                    anounce_data = "NFC가 켜졌습니다. 태그를 인식시켜주세요";
                    myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
                    anounce_data = "";
                }

                else //토글버튼이 OFF상태일 경우.//
                {
                   // Toast.makeText(getApplicationContext(), "NFC가 꺼졌습니다.", Toast.LENGTH_SHORT).show();
                    anounce_data = "NFC가 꺼졌습니다. 스위치를 켜주세요";
                    myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
                    anounce_data = "";
                }
            }
        });

        backPressCloseHandler = new BackPressCloseHandler(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        mResumed = true;

        // Sticky notes received from Android
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()))
        {
            NdefMessage[] messages = getNdefMessages(getIntent());

            byte[] payload = messages[0].getRecords()[0].getPayload();
            setNoteBody(new String(payload));
            setIntent(new Intent()); // Consume this intent.
        }

        enableNdefExchangeMode();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        mResumed = false;
        mNfcAdapter.disableForegroundNdefPush(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_2, menu);

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        // NDEF exchange mode
        if (!mWriteMode && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()))
        {
            NdefMessage[] msgs = getNdefMessages(intent);
            promptForContent(msgs[0]);
        }
    }

    private TextWatcher mTextWatcher = new TextWatcher()
    {
        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
        {

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
        {

        }

        @Override
        public void afterTextChanged(Editable s)
        {
            // TODO Auto-generated method stub

        }
    };

    private void promptForContent(final NdefMessage msg)
    {
        new AlertDialog.Builder(this)
                .setTitle("스마트 리빙 서버에 접속하시겠습니까?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1)
                    {
                        String body = new String(msg.getRecords()[0].getPayload());

                        setNoteBody(body);

                        //finish();
                    }
                })

                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1)
                    {
                        //finish();
                    }
                }).show();
    }

    private void setNoteBody(String body) // 값을 받아오는 부분.//
    {
        //사용자 이름, 비밀번호는 후에 사용자가 입력한 겂을 가져온다.//
        String password = ""+password_edit.getText().toString(); //태그랑 이 저장되어 있는 값과 같아야 한다.//
        String user_id = ""+id_edit.getText().toString(); //데이터베이스에서 값을 불러온다.//

        int check_id = get_enroll_id(user_id);

        // 보안정책으로 NFC의 태그의 비밀번호와 ID가 등록된 것인지 비교//
        if (password.equals(body.toString()) && check_id == 1)
        {
            server_ip = ip_edit.getText().toString(); //IP주소 획득.//

            //네트워크 연결작업 수행 및 메일전송.로딩 액티비티에서 실행 후 정상여부를 반환.//
            Intent intent = new Intent(Login_Activity.this, Loading_Activity.class);

            intent.putExtra("KEY_IP_ADDRESS",server_ip);
            intent.putExtra("KEY_PORT_NUMBER", server_port);
            intent.putExtra("KEY_ID", user_id); //메일 전송을 위한 아이디값 전송.//

            startActivityForResult(intent, GET_STRING); //다음 액티비티 시작.//
        }

        else //동일하지 않을 경우.//
        {
            anounce_data = "등록되지 않은 아이디이거나, 태그 비밀번호가 틀립니다. 다시 확인해주세요";
            myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
            anounce_data = "";
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == GET_STRING)
        {
            if(resultCode == RESULT_OK) //올바른 반환값이 넘어왔는지 확인.//
            {
                if(STATUS_FINISH_VALUE.equals(data.getStringExtra("INPUT_TEXT").toString())) //현재 정확하게 프로그래스가 다 완료되었을 경우.//
                {
                    String user_id = ""+id_edit.getText().toString();

                    anounce_data = "Smart Living 서버에 접속합니다. 오늘도 즐거운 하루되세요!!";
                    myTTS.speak(anounce_data, TextToSpeech.QUEUE_FLUSH, null); //말하기.//
                    anounce_data = "";

                    Intent intent=new Intent(Login_Activity.this, MainActivity.class); //인텐트로 정의.//

                    intent.putExtra("KEY_IP_ADDRESS",server_ip);
                    intent.putExtra("KEY_PORT_NUMBER", server_port);
                    intent.putExtra("KEY_ID", user_id); //메일 전송을 위한 아이디값 전송.//

                    startActivity(intent); //다음 액티비티 시작.//
                }

                else //다 완료가 되지 않았을 경우.//
                {
                    Toast.makeText(getApplicationContext(), "현재 서버에 접속할 수 없습니다. 네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();

                    finish();
                }
            }
        }
    }

    NdefMessage[] getNdefMessages(Intent intent)
    {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action))
        {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (rawMsgs != null)
            {
                msgs = new NdefMessage[rawMsgs.length];

                for (int i = 0; i < rawMsgs.length; i++)
                {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }

            else
            {
                // Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
                msgs = new NdefMessage[] { msg };
            }
        }

        else
        {
            Log.d(TAG, "Unknown intent.");

            finish();
        }

        return msgs;
    }

    private NdefMessage getNoteAsNdef()
    {
        byte[] textBytes = password_edit.getText().toString().getBytes();
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(), new byte[] {}, textBytes);

        return new NdefMessage(new NdefRecord[] { textRecord });
    }

    private void enableNdefExchangeMode()
    {
        mNfcAdapter.enableForegroundNdefPush(Login_Activity.this, getNoteAsNdef());
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefExchangeFilters, null);
    }

    private void disableNdefExchangeMode()
    {
        mNfcAdapter.disableForegroundNdefPush(this);
        mNfcAdapter.disableForegroundDispatch(this);
    }

    private void enableTagWriteMode()
    {
        mWriteMode = true;
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        mWriteTagFilters = new IntentFilter[] { tagDetected };
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
    }

    private void disableTagWriteMode()
    {
        mWriteMode = false;
        mNfcAdapter.disableForegroundDispatch(this);
    }

    private void toast(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed()
    {
        // TODO Auto-generated method stub
        backPressCloseHandler.onBackPressed();
    }

    @Override
    public void onInit(int status)
    {

    }

    public class BackPressCloseHandler
    {
        private long backKeyPressedTime = 0;
        private Toast toast;

        private Activity activity;

        public BackPressCloseHandler(Activity context)
        {
            this.activity = context;
        }

        public void onBackPressed()
        {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000)
            {
                backKeyPressedTime = System.currentTimeMillis();
                showGuide();

                return;
            }

            if (System.currentTimeMillis() <= backKeyPressedTime + 2000)
            {
                activity.finish();
                toast.cancel();
            }
        }

        private void showGuide()
        {
            toast = Toast.makeText(activity, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);

            toast.show();
        }
    }

    public int get_enroll_id(String user_id)
    {
        int check_value = 0; //처음엔 아니라고 가정.//

        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "SELECT user_id FROM home_info"; //전체출력 SQL.//

        cursor = db.rawQuery(sql, null);

        while(cursor.moveToNext())
        {
            String get_id = cursor.getString(0);

            if(user_id.equals(get_id))
            {
                check_value = 1;

                break;
            }
        }

        return check_value;
    }
}
