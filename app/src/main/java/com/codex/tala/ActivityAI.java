package com.codex.tala;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class ActivityAI extends AppCompatActivity{
    private GestureDetector gestureDetector;
    private RecyclerView recyclerView;
    private TextView txtWelcome;
    private EditText edtMessage;
    private ImageButton btnSend;
    private List<Message> messageList;
    private MessageAdapter messageAdapter;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final AIHelper aiHelper = new AIHelper(this);

    private int userId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        userId = getIntent().getIntExtra("userId", -1);

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
                edtMessage.setClickable(false);
                CallNLP(question);
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

    //add response function sent by bot
    private void addResponse(String response){
        messageList.remove(messageList.size()-1);
        addToChat(response, Message.SENT_BY_BOT);
    }

    private void CallNLP(String question){
        messageList.add(new Message("Typing...", Message.SENT_BY_BOT));
        Disposable disposable = aiHelper.executeSimpleChat(question, userId)
                .subscribe(result -> addResponse(result),
                        throwable -> {
                            addResponse("Error occurred. Please try again later!");
                            Log.e("ERROR", throwable.getMessage());
                        });
        compositeDisposable.add(disposable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        aiHelper.deleteThread().observeOn(AndroidSchedulers.mainThread());
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
