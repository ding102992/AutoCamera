package org.json.autocamera.entity;

/**
 * Created by JasonDing on 16/8/3.
 * Copyright © 2016年 bestsign.cn. All rights reserved.
 */
public class ClassInfo {

    private String packageName;
    private String qualifiedName;
    private String simpleName;

    public ClassInfo(String packageName, String qualifiedName, String simpleName) {
        this.packageName = packageName;
        this.qualifiedName = qualifiedName;
        this.simpleName = simpleName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }
}
