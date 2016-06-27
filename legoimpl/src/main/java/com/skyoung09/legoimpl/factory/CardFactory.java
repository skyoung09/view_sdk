package com.skyoung09.legoimpl.factory;

import com.skyoung09.legoimpl.R;
import com.skyoung09.legolib.AppInit;
import com.skyoung09.legolib.factory.ICardFactory;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by zhangxiaobo02 on 16/6/5.
 */

public class CardFactory extends ICardFactory {
    @Override
    public View getView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(AppInit.getInstance().getLibContext());
        View view = inflater.inflate(R.layout.hello, null);
        TextView textView = (TextView) view.findViewById(R.id.textView);
        if (textView != null) {
            textView.setText("hello from sdk 123");
        }
        return view;
    }

    @Override
    public void jumpToContainer(Context context) {
        Intent intent = new Intent();
        intent.setClassName(context.getPackageName(), "com.skyoung09.legoimpl.MainActivity");
        context.startActivity(intent);
    }
}
