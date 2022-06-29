package com.hariofspades.chatbot;

import android.content.res.AssetManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ListView;
import org.alicebot.ab.AIMLProcessor;
import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.alicebot.ab.MagicStrings;
import org.alicebot.ab.PCAIMLProcessorExtension;

import com.hariofspades.chatbot.Adapter.ChatMessageAdapter;
import com.hariofspades.chatbot.Pojo.ChatMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class ChatBotActivity extends AppCompatActivity {

    private ListView mListView;
    private EditText mEditTextMessage;
    public Bot bot;
    public static Chat chat;
    private ChatMessageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);
        mListView = (ListView) findViewById(R.id.listView);
        FloatingActionButton mButtonSend = (FloatingActionButton) findViewById(R.id.btn_send);
        mEditTextMessage = (EditText) findViewById(R.id.et_message);
        //ImageView mImageView = (ImageView) findViewById(R.id.iv_image);
        mAdapter = new ChatMessageAdapter(this, new ArrayList<>());
        mListView.setAdapter(mAdapter);

        mButtonSend.setOnClickListener(v -> {
            String message = mEditTextMessage.getText().toString();
            //bot
            String response = chat.multisentenceRespond(mEditTextMessage.getText().toString());
            if (TextUtils.isEmpty(message)) {
                return;
            }
            sendMessage(message);
            mimicOtherMessage(response);
            mEditTextMessage.setText("");
            mListView.setSelection(mAdapter.getCount() - 1);
        });
        //checking SD card availablility
        //boolean a = isSDCARDAvailable();
        //receiving the assets from the app directory
        AssetManager assets = getResources().getAssets();
        File jayDir = new File(Objects.requireNonNull(getApplicationContext().getExternalFilesDir(null)).getAbsolutePath() + "/hari/bots/Hari");
        //boolean b = jayDir.mkdirs();
        if (jayDir.exists()) {
            //Reading the file
            try {
                for (String dir : Objects.requireNonNull(assets.list("Hari"))) {
                    File subdir = new File(jayDir.getPath() + "/" + dir);
                    //boolean subdir_check = subdir.mkdirs();
                    for (String file : Objects.requireNonNull(assets.list("Hari/" + dir))) {
                        File f = new File(jayDir.getPath() + "/" + dir + "/" + file);
                        if (f.exists()) {
                            continue;
                        }
                        InputStream in;
                        OutputStream out;
                        in = assets.open("Hari/" + dir + "/" + file);
                        out = new FileOutputStream(jayDir.getPath() + "/" + dir + "/" + file);
                        //copy file from assets to the mobile's SD card or any secondary memory
                        copyFile(in, out);
                        in.close();
                        out.flush();
                        out.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //get the working directory
        MagicStrings.root_path = Objects.requireNonNull(getApplicationContext().getExternalFilesDir(null)).getAbsolutePath() + "/hari";
        System.out.println("Working Directory = " + MagicStrings.root_path);
        AIMLProcessor.extension =  new PCAIMLProcessorExtension();
        //Assign the AIML files to bot for processing
        bot = new Bot("Hari", MagicStrings.root_path, "chat");
        chat = new Chat(bot);

    }

    private void sendMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, true, false);
        mAdapter.add(chatMessage);

        //mimicOtherMessage(message);
    }

    private void mimicOtherMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, false, false);
        mAdapter.add(chatMessage);
    }

    //copying the file
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

}
