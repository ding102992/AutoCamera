package org.jason.autocamera;

import java.io.File;

/**
 * Created by JasonDing on 16/8/8.
 */
public class FileUtils {

    public static void delete(String path) {
        File file = new File(path);
        if(file.exists()) {
            file.delete();
        }
    }

}
