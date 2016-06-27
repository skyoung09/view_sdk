package com.skyoung09.legolib.factory;

import android.content.Context;
import android.view.View;

/**
 * Created by zhangxiaobo02 on 16/6/5.
 */

public abstract class ICardFactory {
    static ICardFactory cardFactory = null;
    public static void setInstance(ICardFactory cf) {
        cardFactory = cf;
    }
    public static ICardFactory getInstance() {
        return cardFactory;
    }
    public abstract View getView(Context context);
    public abstract void jumpToContainer(Context context);
}