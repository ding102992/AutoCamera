package org.jason.autocamera.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在需要从照相/相册/截图取得结果的方法上,使用此注解
 *
 * 例子:
 * {@literal @}OnImageReturn void onCaptureFinished(File outputFile) {
 *   ...
 * }
 *
 *
 * Created by JasonDing on 16/8/2.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface OnImageReturn {
}
