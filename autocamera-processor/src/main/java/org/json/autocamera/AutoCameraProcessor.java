package org.json.autocamera;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import org.jason.autocamera.annotations.NeedUseCamera;
import org.jason.autocamera.annotations.OnImageReturn;
import org.jason.autocamera.annotations.PathGenerator;
import org.json.autocamera.entity.CameraInfo;
import org.json.autocamera.entity.ClassInfo;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@AutoService(Processor.class)
public class AutoCameraProcessor extends AbstractProcessor {

    private static final String ACTION_CAMERA = "android.media.action.IMAGE_CAPTURE";
    private static final String ACTION_ALBUM = "android.intent.action.PICK";
    private static final String ACTION_CROP = "com.android.camera.action.CROP";

    private static final ClassName CLASS_URI = ClassName.get("android.net","Uri");
    private static final ClassName CLASS_INTENT = ClassName.get("android.content","Intent");
    private static final ClassName CLASS_MEDIA_STORE = ClassName.get("android.provider","MediaStore");
    private static final ClassName CLASS_COMPRESS_FORMAT = ClassName.get("android.graphics","Bitmap","CompressFormat");
    private static final ClassName CLASS_ACTIVITY = ClassName.get("android.app","Activity");

    private Messager messager;
    private Filer filer;
    private Elements elementUtils;
    private Types typeUtils;
    private CameraInfo cameraInfo;
    private ClassInfo classInfo;
    private String generateMethodName;
    private String returnMethodName;


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> names = new HashSet<>(2);
        names.add(OnImageReturn.class.getCanonicalName());
        names.add(NeedUseCamera.class.getCanonicalName());
        names.add(PathGenerator.class.getCanonicalName());
        return names;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        // 解析获取地址的方法
        Set<? extends Element> generateMethodSet = roundEnv.getElementsAnnotatedWith(PathGenerator.class);
        final int size = generateMethodSet.size();
        if(size > 1) {
            MessageUtils.e(messager,"single method can use @PathGenerator");
            return false;
        } else if(size == 1){
            for (Element element : generateMethodSet) {
                if (!validateGenerateMethodElement(element)) {
                    return false;
                }
                generateMethodName = element.getSimpleName().toString();
            }
        }

        // 解析回调的方法
        Set<? extends Element> returnMethodSet = roundEnv.getElementsAnnotatedWith(OnImageReturn.class);
        if(returnMethodSet.size() > 1) {
            MessageUtils.e(messager,"single method can use @OnImageReturn");
            return false;
        }
        for (Element element : returnMethodSet) {
            if (!validateReturnMethodElement(element)) {
                return false;
            }
            returnMethodName = element.getSimpleName().toString();
        }

        //解析类
        Set<? extends Element> classSet = roundEnv.getElementsAnnotatedWith(NeedUseCamera.class);
        for (Element element : classSet) {
            if (!validateClassElement(element)) {
                return false;
            }

            initMetaInfo(element);

            if(!validateMetaInfo()) {
                return false;
            }

            TypeSpec autoCameraClass = generateClassCode();

            JavaFile javaFile = JavaFile.builder(classInfo.getPackageName(), autoCameraClass).build();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private TypeSpec generateClassCode() {
        // 声明常量
        final FieldSpec fieldRequestCodeCamera = FieldSpec.builder(int.class, "REQUEST_CODE_CAMERA", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer("0xea0").build();
        final FieldSpec fieldRequestCodeAlbum = FieldSpec.builder(int.class, "REQUEST_CODE_ALBUM", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer("0xea1").build();
        final FieldSpec fieldRequestCodeCrop = FieldSpec.builder(int.class, "REQUEST_CODE_CROP", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer("0xea2").build();
        final FieldSpec fieldSavePath = FieldSpec.builder(String.class, "SAVE_PATH", Modifier.PRIVATE, Modifier.STATIC).initializer("$S",cameraInfo.getSavePath()).build();
        final FieldSpec fieldSavePathUri = FieldSpec.builder(CLASS_URI, "SAVE_URI", Modifier.PRIVATE, Modifier.STATIC).build();
        final FieldSpec fieldAspectX = FieldSpec.builder(int.class, "ASPECT_X", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer(String.valueOf(cameraInfo.getAspectX())).build();
        final FieldSpec fieldAspectY = FieldSpec.builder(int.class, "ASPECT_Y", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer(String.valueOf(cameraInfo.getAspectY())).build();
        final FieldSpec fieldOutputX = FieldSpec.builder(int.class, "OUTPUT_X", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer(String.valueOf(cameraInfo.getOutputX())).build();
        final FieldSpec fieldOutputY = FieldSpec.builder(int.class, "OUTPUT_Y", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer(String.valueOf(cameraInfo.getOutputY())).build();
        final FieldSpec fieldNeedCrop = FieldSpec.builder(boolean.class, "NEED_CROP", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer(String.valueOf(cameraInfo.isNeedCrop())).build();

        // 方法
        final ClassName classNameActivity = ClassName.get(classInfo.getPackageName(), classInfo.getSimpleName());

        // 打开相机的方法
        MethodSpec methodOpenCamera = MethodSpec
                .methodBuilder("openCamera")
                .addModifiers(Modifier.STATIC)
                .returns(void.class)
                .addParameter(classNameActivity, "target")
                .addStatement("$T cameraIntent = new $T($S)", CLASS_INTENT,CLASS_INTENT,ACTION_CAMERA)
                .addStatement("final $T uri = saveUri(target)",CLASS_URI)
                .addCode(CodeBlock.builder()
                        .beginControlFlow("if(!NEED_CROP)")
                        .addStatement("cameraIntent.putExtra($T.EXTRA_OUTPUT, uri)",CLASS_MEDIA_STORE)
                        .endControlFlow()
                        .beginControlFlow("else")
                        .addStatement("cameraIntent.putExtra($T.EXTRA_OUTPUT, tempPath())",CLASS_MEDIA_STORE)
                        .endControlFlow()
                        .build())
                .addStatement("cameraIntent.putExtra($T.EXTRA_VIDEO_QUALITY, 1)",CLASS_MEDIA_STORE)
                .addStatement("target.startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA)")
                .build();

        // 打开相册的方法
        MethodSpec methodOpenAlbum = MethodSpec
                .methodBuilder("openAlbum")
                .addModifiers(Modifier.STATIC)
                .returns(void.class)
                .addParameter(classNameActivity, "target")
                .addStatement("$T intent = new $T($S)", CLASS_INTENT,CLASS_INTENT,ACTION_ALBUM)
                .addStatement("intent.setType(\"image/*\")")
                .addStatement("target.startActivityForResult(intent, REQUEST_CODE_ALBUM)")
                .build();

        // 在需要截图的情况下,临时存储照相的地址
        MethodSpec methodTempPath = MethodSpec
                .methodBuilder("tempPath")
                .addModifiers(Modifier.PRIVATE,Modifier.STATIC)
                .returns(CLASS_URI)
                .addStatement("return $T.fromFile(new $T(SAVE_PATH+\"_TEMP\"))",CLASS_URI,File.class)
                .build();

        MethodSpec methodSaveUri = MethodSpec
                .methodBuilder("saveUri")
                .addModifiers(Modifier.PRIVATE,Modifier.STATIC)
                .addParameter(classNameActivity,"target")
                .returns(CLASS_URI)
                .addCode(CodeBlock.builder()
                        .beginControlFlow("if(SAVE_URI == null)")
                        .add(generateMethodName != null ? "SAVE_PATH = target."+generateMethodName+"();" : "")
                        .addStatement("$T path",String.class)
                        .beginControlFlow("if(SAVE_PATH.startsWith(\"/\"))")
                        .addStatement("path = SAVE_PATH")
                        .endControlFlow()
                        .beginControlFlow("else")
                        .addStatement("path = target.getExternalCacheDir().getAbsolutePath() + \"/\" + SAVE_PATH")
                        .addStatement("SAVE_PATH = path")
                        .endControlFlow()
                        .addStatement("SAVE_URI = $T.fromFile(new File(path))",CLASS_URI)
                        .endControlFlow()
                        .build())
                .addStatement("return SAVE_URI",CLASS_URI)
                .build();

        // 打开相册的方法
        MethodSpec methodOpenCrop = MethodSpec
                .methodBuilder("resizeImage")
                .addModifiers(Modifier.PRIVATE,Modifier.STATIC)
                .returns(void.class)
                .addParameter(classNameActivity, "target")
                .addParameter(CLASS_URI,"uri")
                .addStatement("$T intent = new $T($S)", CLASS_INTENT,CLASS_INTENT,ACTION_CROP)
                .addStatement("intent.setDataAndType(uri, \"image/*\")")
                .addStatement("intent.putExtra(\"crop\", \"true\")")
                .addStatement("intent.putExtra(\"aspectX\", ASPECT_X)")
                .addStatement("intent.putExtra(\"aspectY\", ASPECT_Y)")
                .addStatement("intent.putExtra(\"outputX\", OUTPUT_X)")
                .addStatement("intent.putExtra(\"outputY\", OUTPUT_Y)")
                .addStatement("intent.putExtra(\"output\", saveUri(target))")
                .addStatement("intent.putExtra(\"outputFormat\", $T.JPEG.toString())",CLASS_COMPRESS_FORMAT)
                .addStatement("intent.putExtra(\"noFaceDetection\", true)")
                .addStatement("target.startActivityForResult(intent, REQUEST_CODE_CROP)")
                .build();

        // onActivityResult 方法
        MethodSpec methodOnActivityResult = MethodSpec
                .methodBuilder("onActivityResult")
                .addModifiers(Modifier.STATIC)
                .returns(void.class)
                .addParameter(classNameActivity, "target")
                .addParameter(int.class,"requestCode")
                .addParameter(int.class,"resultCode")
                .addParameter(CLASS_INTENT,"data")
                .addCode(
                        CodeBlock.builder()
                                .beginControlFlow("switch (requestCode)")
                                .add("case REQUEST_CODE_CAMERA:")
                                .beginControlFlow("if (resultCode == $T.RESULT_OK)",CLASS_ACTIVITY)
                                .beginControlFlow("if(!NEED_CROP)")
                                .addStatement("invokeReturnMethod(target,saveUri(target))")
                                .endControlFlow()
                                .beginControlFlow("else")
                                .addStatement("resizeImage(target,tempPath())")
                                .endControlFlow()
                                .endControlFlow()
                                .addStatement("break")
                                .add("case REQUEST_CODE_ALBUM:")
                                .beginControlFlow("if (resultCode == $T.RESULT_OK)",CLASS_ACTIVITY)
                                .addStatement("final Uri uri = data.getData()")
                                .beginControlFlow("if(!NEED_CROP)")
                                .addStatement("invokeReturnMethod(target,uri)")
                                .endControlFlow()
                                .beginControlFlow("else")
                                .addStatement("resizeImage(target,uri)")
                                .endControlFlow()
                                .endControlFlow()
                                .addStatement("break")
                                .add("case REQUEST_CODE_CROP:")
                                .beginControlFlow("if (resultCode == $T.RESULT_OK)",CLASS_ACTIVITY)
                                .addStatement("invokeReturnMethod(target,saveUri(target))")
                                .addStatement("$T.delete(SAVE_PATH+\"_TEMP\")", org.jason.autocamera.FileUtils.class)
                                .endControlFlow()
                                .addStatement("break")
                                .endControlFlow()
                                .build()
                )
                .build();

        MethodSpec methodInvokeReturnMethod = MethodSpec
                .methodBuilder("invokeReturnMethod")
                .addModifiers(Modifier.PRIVATE,Modifier.STATIC)
                .returns(void.class)
                .addParameter(classNameActivity, "target")
                .addParameter(CLASS_URI,"uri")
                .addStatement("target."+ returnMethodName +"(uri)")
                .build();

        return TypeSpec
                .classBuilder(classInfo.getSimpleName()+"AutoCamera")
                .addJavadoc("This class is generated by AutoCamera,DO NOT MODIFY.")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(fieldRequestCodeCamera)
                .addField(fieldRequestCodeAlbum)
                .addField(fieldRequestCodeCrop)
                .addField(fieldSavePath)
                .addField(fieldSavePathUri)
                .addField(fieldAspectX)
                .addField(fieldAspectY)
                .addField(fieldOutputX)
                .addField(fieldOutputY)
                .addField(fieldNeedCrop)
                .addMethod(methodOpenCamera)
                .addMethod(methodOpenAlbum)
                .addMethod(methodOpenCrop)
                .addMethod(methodOnActivityResult)
                .addMethod(methodInvokeReturnMethod)
                .addMethod(methodTempPath)
                .addMethod(methodSaveUri)
                .build();
    }


    private void initMetaInfo(Element element) {
        final NeedUseCamera annotation = element.getAnnotation(NeedUseCamera.class);
        TypeElement typeElement = (TypeElement) element;
        final PackageElement packageName = elementUtils.getPackageOf(element);
        final Name qualifiedName = typeElement.getQualifiedName();
        final Name simpleName = typeElement.getSimpleName();

        cameraInfo = new CameraInfo(annotation);
        classInfo = new ClassInfo(
                packageName.getQualifiedName().toString(),
                qualifiedName.toString(),
                simpleName.toString());
    }

    private boolean validateClassElement(Element element) {
        if(element.getKind() != ElementKind.CLASS) {
            MessageUtils.e(messager,"Annotation @NeedUseCamera only support class.");
            return false;
        }
        return true;
    }

    private boolean validateReturnMethodElement(Element element) {
        if(element.getKind() != ElementKind.METHOD) {
            MessageUtils.e(messager,"Annotation @OnImageReturn only support class.");
            return false;
        }
        ExecutableElement executableElement = (ExecutableElement) element;
        final List<? extends VariableElement> parameters = executableElement.getParameters();
        if(parameters.size() != 1 || !"android.net.Uri".equals(parameters.get(0).asType().toString())){
            MessageUtils.e(messager,"Annotation @OnImageReturn only have one parameter,type is android.net.Uri");
            return false;
        }
        if (element.getModifiers().contains(Modifier.PRIVATE)) {
            MessageUtils.e(messager,"Annotation @OnImageReturn should not be private.");
            return false;
        }
        return true;
    }

    private boolean validateGenerateMethodElement(Element element) {
        if(element.getKind() != ElementKind.METHOD) {
            MessageUtils.e(messager,"Annotation @PathGenerator only support class.");
            return false;
        }
        ExecutableElement executableElement = (ExecutableElement) element;
        final List<? extends VariableElement> parameters = executableElement.getParameters();
        if(parameters.size() > 0){
            MessageUtils.e(messager,"Annotation @PathGenerator method have no parameter");
            return false;
        }
        if (element.getModifiers().contains(Modifier.PRIVATE)) {
            MessageUtils.e(messager,"Annotation @PathGenerator method should not be private.");
            return false;
        }
        if (!"java.lang.String".equals(executableElement.getReturnType().toString())) {
            MessageUtils.e(messager,"Annotation @PathGenerator method return type should be java.lang.String");
            return false;
        }
        return true;
    }

    private boolean validateMetaInfo() {
        final String savePath = cameraInfo.getSavePath();
        if(generateMethodName == null && (savePath == null || savePath.length() == 0)) {
            MessageUtils.e(messager,"savePath should be set when not set @PathGenerator.");
            return false;
        }
        if(cameraInfo.isNeedCrop()) {
            if(cameraInfo.getAspectX() <= 0
                    || cameraInfo.getAspectY() <= 0
                    || cameraInfo.getOutputX() <= 0
                    || cameraInfo.getOutputY() <= 0) {
                MessageUtils.e(messager,"aspectX,aspectY,outputX,outputY should set over zero");
                return false;
            }
        }
        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
