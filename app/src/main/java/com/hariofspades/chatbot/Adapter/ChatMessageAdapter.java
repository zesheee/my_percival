package com.hariofspades.chatbot.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.hariofspades.chatbot.Pojo.ChatMessage;
import com.hariofspades.chatbot.R;

import java.util.List;
import java.util.Objects;

public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {
    private static final int MY_MESSAGE = 0, OTHER_MESSAGE = 1, MY_IMAGE = 2, OTHER_IMAGE = 3;

    public ChatMessageAdapter(Context context, List<ChatMessage> data) {
        super(context, R.layout.item_mine_message, data);
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage item = getItem(position);

        assert item != null;
        if (item.isMine() && !item.isImage()) return MY_MESSAGE;
        else if (!item.isMine() && !item.isImage()) return OTHER_MESSAGE;
        else if (item.isMine() && item.isImage()) return MY_IMAGE;
        else return OTHER_IMAGE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);
        if (viewType == MY_MESSAGE) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mine_message, parent, false);

            TextView textView = (TextView) convertView.findViewById(R.id.text);
            textView.setText(Objects.requireNonNull(getItem(position)).getContent());

        } else if (viewType == OTHER_MESSAGE) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_other_message, parent, false);

            TextView textView = (TextView) convertView.findViewById(R.id.text);
            textView.setText(Objects.requireNonNull(getItem(position)).getContent());
        }  //convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mine_image, parent, false);
        // convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_other_image, parent, false);


        convertView.findViewById(R.id.chatMessageView).setOnClickListener(v -> Toast.makeText(getContext(), "onClick", Toast.LENGTH_LONG).show());


        return convertView;
    }
}
