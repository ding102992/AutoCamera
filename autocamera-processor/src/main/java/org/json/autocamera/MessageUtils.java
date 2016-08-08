package org.json.autocamera;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

/**
 * Created by JasonDing on 16/8/3.
 * Copyright © 2016年 bestsign.cn. All rights reserved.
 */
public class MessageUtils {

    public static void e(Messager messager,String msg){
        messager.printMessage(Diagnostic.Kind.ERROR,"[AutoCamera] " + msg);
    }

    public static void i(Messager messager,String msg){
        messager.printMessage(Diagnostic.Kind.NOTE,"[AutoCamera] " + msg);
    }

    public static void w(Messager messager,String msg){
        messager.printMessage(Diagnostic.Kind.WARNING,"[AutoCamera] " + msg);
    }
}
