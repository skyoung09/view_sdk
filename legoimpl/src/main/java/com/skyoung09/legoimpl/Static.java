package com.skyoung09.legoimpl;

import com.skyoung09.legoimpl.factory.CardFactory;
import com.skyoung09.legolib.factory.ICardFactory;

/**
 * Created by zhangxiaobo02 on 16/6/5.
 */

public class Static {
    public static void init() {
        CardFactory cardFactory = new CardFactory();
        ICardFactory.setInstance(cardFactory);
    }
}
