package org.jason.autocamera.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 给需要使用截图的Activity或者Fragment添加此注解
 *
 * {@link #savePath} 为必须项;
 *
 * 在{@link #needCrop} 为true的情况下,
 * {@link #aspectX},{@link #aspectY},{@link #outputX},{@link #outputY}才有效;
 * dd
 * Created by JasonDing on 16/8/2.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface NeedUseCamera {

    /**
     * 图片最终的存储地址
     *
     * 若指定了@PathGenerator,则使用其返回的结果.
     *
     * 若未指定,则使用此地址
     * 如果这个地址以 / 起头,则认为是绝对地址
     * 否则,默认存放在 activity.getExternalCacheDir() 之中
     */
    String savePath() default "";

    /**
     * 是否需要截图
     */
    boolean needCrop() default false;

    /**
     * 截图的比例
     *
     * 与{@link #aspectY} 配合使用,在开启截图的情况下,横竖的比例
     */
    int aspectX() default 1;

    /**
     * 截图的比例
     *
     * 与{@link #aspectX} 配合使用,在开启截图的情况下,横竖的比例
     */
    int aspectY() default 1;

    /**
     * 输出宽度,单位为像素
     * @return
     */
    int outputX() default 400;

    /**
     * 输出高度,单位为像素
     * @return
     */
    int outputY() default 400;

}
