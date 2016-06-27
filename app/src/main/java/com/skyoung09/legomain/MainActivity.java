package com.skyoung09.legomain;

import com.skyoung09.legolib.factory.ICardFactory;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ICardFactory.getInstance() != null) {
            View view = ICardFactory.getInstance().getView(MainActivity.this);
            if (view != null) {
                setContentView(view);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ICardFactory.getInstance().jumpToContainer(MainActivity.this);
                    }
                });
            }
        }
    }
}
