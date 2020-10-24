package com.baiyuas.record;

/**
 * @author 拜雨
 * @date 2020-10
 */
class Mp3Util {

    //计算音量大小
    protected static int calculateRealVolume(short[] buffer, int readSize) {
        double sum = 0;
        for (int i = 0; i < readSize; i++) {
            sum += buffer[i] * buffer[i];
        }
        if (readSize > 0) {
            double amplitude = sum / readSize;
            return  (int) Math.sqrt(amplitude);
        }
        return 0;
    }
}
