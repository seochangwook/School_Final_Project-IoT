package com.example.seo.project;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by Seo on 2016-03-30.
 */
public class Enroll_1_Activity extends ActionBarActivity
{
    FloatingActionButton info_enroll_button;

    Button next_enroll_page_button, tag_scan_button;
    EditText id_edit, mail_password_edit, tag_password_edit, name_edit, phone_number_edit, address_edit;

    String user_id;
    String mail_password;
    String user_name;
    String user_phone_number;
    String user_address;

    private static final String TAG = "Smart Living";
    private boolean mResumed = false;
    private boolean mWriteMode = false;

    NfcAdapter mNfcAdapter;
    PendingIntent mNfcPendingIntent;
    IntentFilter[] mWriteTagFilters;
    IntentFilter[] mNdefExchangeFilters;

    int check_scan_tag = 0; //0이면 기본적으로 스캔을 하지않았다는 전제.//

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enroll_1_activity_layout);

        info_enroll_button = (FloatingActionButton) findViewById(R.id.info_button);
        next_enroll_page_button = (Button)findViewById(R.id.next_button);
        findViewById(R.id.scan_button).setOnClickListener(mTagWriter);
        id_edit = (EditText)findViewById(R.id.id_edit);
        tag_password_edit = (EditText)findViewById(R.id.tag_password_edit);
        name_edit = (EditText)findViewById(R.id.user_name_edit);
        phone_number_edit = (EditText)findViewById(R.id.user_phone_number_edit);
        address_edit = (EditText)findViewById(R.id.user_address_edit);
        mail_password_edit = (EditText)findViewById(R.id.password_google_edit);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

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

        next_enroll_page_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(check_scan_tag == 1)
                {
                    user_id = id_edit.getText().toString();
                    user_name = name_edit.getText().toString();
                    user_phone_number = phone_number_edit.getText().toString();
                    user_address = address_edit.getText().toString();
                    mail_password = mail_password_edit.getText().toString();

                    Intent intent = new Intent(Enroll_1_Activity.this, Enroll_Final_Activity.class);

                    //다음단계로 값들을 넘긴다.//
                    intent.putExtra("KEY_USER_ID",user_id);
                    intent.putExtra("KEY_USER_NAME",user_name);
                    intent.putExtra("KEY_USER_PHONE_NUMBER",user_phone_number);
                    intent.putExtra("KEY_USER_ADDRESS",user_address);
                    intent.putExtra("KEY_MAIL_PASSWORD",mail_password);

                    startActivity(intent);

                    finish();
                }

                else if(check_scan_tag == 0)
                {
                    Toast.makeText(getApplicationContext(), "태그 등록이 되지 않았습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        info_enroll_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view,
                        "1. 본 서비스는 구글메일과 연동되어 제공됩니다." + "\n" +
                        "2. 비밀번호는 영문, 숫자포함 4~8자리로 입력.",
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
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
    protected void onNewIntent(Intent intent)
    {
        // NDEF exchange mode
        if (!mWriteMode && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()))
        {
            NdefMessage[] msgs = getNdefMessages(intent);
            promptForContent(msgs[0]);
        }

        // Tag writing mode
        if (mWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()))
        {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            writeTag(getNoteAsNdef(), detectedTag);
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

    private View.OnClickListener mTagWriter = new View.OnClickListener() //태그 버튼을 눌렀을 시.//
    {
        @Override
        public void onClick(View arg0)
        {
            // Write to a tag for as long as the dialog is shown.
            disableNdefExchangeMode();
            enableTagWriteMode();

            new AlertDialog.Builder(Enroll_1_Activity.this)
                    .setTitle("태그에 가져다 놓으세요")
                    .setOnCancelListener
                            (
                                    new DialogInterface.OnCancelListener()
                                    {
                                        @Override
                                        public void onCancel(DialogInterface dialog)
                                        {
                                            disableTagWriteMode();
                                            enableNdefExchangeMode();

                                            next_enroll_page_button.setEnabled(true);
                                            check_scan_tag = 1;

                                        }
                                    }).create().show();
        }
    };

    private void promptForContent(final NdefMessage msg)
    {
        new AlertDialog.Builder(this)
                .setTitle("스마트 리빙 서버에 접속하시겠습니까?")
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1)
                            {
                                String body = new String(msg.getRecords()[0]
                                        .getPayload());

                                setNoteBody(body);
                            }
                        })

                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1)
                    {

                    }
                }).show();
    }

    private void setNoteBody(String body) // 값을 받아오는 부분.//
    {
        //사용자 이름, 비밀번호는 후에 사용자가 입력한 겂을 가져온다.//
        String password = ""+tag_password_edit.getText().toString(); //태그랑 이 저장되어 있는 값과 같아야 한다.//

        String tts_str = "";

        // 이 부분에서 네트워크 작업을 한다.//
        if (password.equals(body.toString()))
        {
            Toast.makeText(getApplicationContext(), "Good"+body.toString(), Toast.LENGTH_SHORT).show();
        }

        else
        {
            Toast.makeText(getApplicationContext(), "Bad"+body.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private NdefMessage getNoteAsNdef()
    {
        byte[] textBytes = tag_password_edit.getText().toString().getBytes();
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(), new byte[] {}, textBytes);

        return new NdefMessage(new NdefRecord[] { textRecord });
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

    private void enableNdefExchangeMode()
    {
        mNfcAdapter.enableForegroundNdefPush(Enroll_1_Activity.this, getNoteAsNdef());
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

    boolean writeTag(NdefMessage message, Tag tag)
    {
        int size = message.toByteArray().length;

        try
        {
            Ndef ndef = Ndef.get(tag);

            if (ndef != null)
            {
                ndef.connect();

                if (!ndef.isWritable())
                {
                    toast("Tag is read-only.");
                    return false;
                }

                if (ndef.getMaxSize() < size)
                {
                    toast("Tag capacity is " + ndef.getMaxSize() + " bytes, message is " + size + " bytes.");

                    return false;
                }

                ndef.writeNdefMessage(message);

                toast("비밀번호 적용 완료!! '뒤로'버튼을 눌러주세요");

                return true;
            }

            else
            {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null)
                {
                    try
                    {
                        format.connect();
                        format.format(message);

                        toast("비밀번호 적용 완료!! '뒤로'버튼을 눌러주세요");

                        return true;
                    }

                    catch (IOException e)
                    {
                        toast("태그스캔에 실패했습니다.");

                        return false;
                    }
                }

                else
                {
                    toast("지원되지 않는 태그입니다.");

                    return false;
                }
            }
        }

        catch (Exception e)
        {
            toast("태그스캔에 실패했습니다.");
        }

        return false;
    }

    private void toast(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_2, menu);

        return true;
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
}
