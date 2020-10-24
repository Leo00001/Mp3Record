## Mp3录音

### 引用

引用在工程的`build.gradle`中增加

```
allprojects {
    repositories {
        ...
        maven {url "https://dl.bintray.com/baiyuas/maven"}
    }
}
```

在module的`build.gradle`增加依赖

```
dependencies {
    ...
    implementation 'com.baiyuas:Mp3Record:1.0.2'
    ...
}
```

### 使用

```
Mp3Record record = new Mp3Record(this);
// 开始录音
record.startRecord(new File("/sdcard/temp.mp3"), new Mp3RecordListener() {

    @Override
    public void onRecording(int volume) {
        Log.d("baiyuas", "当前音量：" + volume));
    }

    @Override
    public void onFinish(String mp3FilePath) {
        Log.d("baiyuas", mp3FilePath);
    }

    @Override
    public void onError(String msg) {
        Log.d("baiyuas", msg);
    }
});

// 停止录音
record.stop();

// 释放资源
record.release();
```

注：
在开发过程中，当工程中引用了三方的so文件时候，gradle插件版本4.0.0以下的建议将三方so文件放在main/src/jniLibs目录下

如果有需要将库上传到jcenter的，gradle插件建议使用3.5.3，高版本目前还没有兼容适配

 *** 1.0.2 ***
 1. 解决了mp3录音库找不到库的问题

学习自[LameMp3ForAndroid](https://github.com/clam314/LameMp3ForAndroid)
