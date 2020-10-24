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
    implementation 'com.baiyuas:Mp3Record:1.0.0'
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

学习自[LameMp3ForAndroid](https://github.com/clam314/LameMp3ForAndroid)
