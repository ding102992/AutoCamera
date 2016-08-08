package org.json.autocamera.entity;

import org.jason.autocamera.annotations.NeedUseCamera;

/**
 * Created by JasonDing on 16/8/3.
 * Copyright © 2016年 bestsign.cn. All rights reserved.
 */
public class CameraInfo {

    private String savePath;
    private boolean needCrop;
    private int aspectX;
    private int aspectY;
    private int outputX;
    private int outputY;

    public CameraInfo(NeedUseCamera needUseCamera){
        savePath = needUseCamera.savePath();
        needCrop = needUseCamera.needCrop();
        aspectX = needUseCamera.aspectX();
        aspectY = needUseCamera.aspectY();
        outputX = needUseCamera.outputX();
        outputY = needUseCamera.outputY();
    }

    public String getSavePath() {
        return savePath;
    }

    public boolean isNeedCrop() {
        return needCrop;
    }

    public int getAspectX() {
        return aspectX;
    }

    public int getAspectY() {
        return aspectY;
    }

    public int getOutputX() {
        return outputX;
    }

    public int getOutputY() {
        return outputY;
    }
}
