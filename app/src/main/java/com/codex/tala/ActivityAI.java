package com.codex.tala;

import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityAI extends AppCompatActivity{
    private GestureDetector gestureDetector;
    private RecyclerView recyclerView;
    private TextView txtWelcome;
    private EditText edtMessage;
    private ImageButton btnSend;
    private List<Message> messageList;
    private MessageAdapter messageAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        gestureDetector = new GestureDetector(this, new SwipeGestureListener());

        recyclerView = findViewById(R.id.chat_rv);
        txtWelcome = findViewById(R.id.txtWelcome);
        edtMessage = findViewById(R.id.message_edit_text);
        btnSend = findViewById(R.id.send_btn);

        messageList = new ArrayList<>();
        //Recycler View
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String question = edtMessage.getText().toString().trim();
                addToChat(question, Message.SENT_BY_ME);
                edtMessage.setText("");
                CallAPI(question);
                txtWelcome.setVisibility(View.GONE);
            }
        });
    }

    private void addToChat(String message, String sentBy){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }

    //addresponse function sent by bot
    private void addResponse(String response){
        messageList.remove(messageList.size()-1);
        addToChat(response, Message.SENT_BY_BOT);
    }

    //calling api using http dependency TODO: placeholder function codes
    private void CallAPI(String question){
        messageList.add(new Message("Typing...", Message.SENT_BY_BOT));
        //handler used to showcase the typing text - is temporary
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                addResponse("Hi! I am Cali. Your friendly neighborhood spidergirl!");
            }
        }, 2000);
//        JSONObject jsonObject = new JSONObject();
//        try {
//            jsonObject.put("model", "gpt-3.5-turbo-0125");
//            JSONArray messageArr = new JSONArray();
//            JSONObject obj = new JSONObject();
//            obj.put("role", "user");
//            obj.put("content", "question");
//            messageArr.put(obj);
//
//            jsonObject.put("message", messageArr);
//        }catch (JSONException e){
//            throw new RuntimeException(e);
//        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        finish();
                        overridePendingTransition(0,R.anim.slide_down_anim);
                    }
                    result = true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }
}
