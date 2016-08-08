package org.jason.autocamera.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用户标志生成存储地址的方法,返回一个File对象
 *
 * 这个方法不是必须的,若不设置,则@NeedUseCamera,必须有savePath.
 * 这个方法会覆盖savePath的设置
 *
 *  * 例子:
 * {@literal @}PathGenerator String getFilePath() {
 *   return target.getExternalCacheDir().getAbsolutePath() + "IMAGE_"+System.currentTimeMillis() + ".jpg";
 * }
 *
 * Created by JasonDing on 16/8/8.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface PathGenerator {
}
