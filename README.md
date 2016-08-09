## AutoCamera
> 使用注解自动生成代码，更便捷的使用Camera API.

#### 前提
这个库不负责申请权限，需要有的权限有：

```xml
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

#### 使用方法

* 在Activity上使用此注解，指定存储位置。以下两种方式可以取其一

```java
// @NeedUseCamera是必须的，savePath可以不传，但是一定要有@PathGenerator才行，默认needCrop为fales，即关闭截图
@NeedUseCamera(savePath = "test.jpg")
public class MainActivity extends AppCompatActivity

// 如果指定了savePath，这个方法可以不创建，如果创建了这个方法，会覆盖savePath
@PathGenerator
String generatePath(){
    return "test.jpg";
}

```

* 声明返回方法
    
```java
// 参数的类型必须是Uri
@OnImageReturn
void onImageReturn(Uri uri){
    Toast.makeText(this,uri.toString(),Toast.LENGTH_LONG).show();
}
```

* Build 了之后，在onActivityResult中添加

```java
MainActivityAutoCamera.onActivityResult(this,requestCode,resultCode,data);
```

* 使用

```java
  // 开启相册
  MainActivityAutoCamera.openAlbum(MainActivity.this);
  
  // 开启相机
  MainActivityAutoCamera.openCamera(MainActivity.this);
```

#### 配置
* 根项目下的build.gradle添加插件
```groovy
classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
```
* app项目下的build.gradle添加依赖
```groovy
    compile 'org.jason:autocamera-library:0.0.1'
    apt 'org.jason:autocamera-processor:0.0.1'
```

* app项目下的build.gradle应用插件
```groovy
apply plugin: 'com.neenbedankt.android-apt'
```

License
-------

    Copyright 2016 Jason Ding

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

