cmake_minimum_required(VERSION 3.4.1)

#引入cryptopp加密库=================
#动态库导入
add_library(lame-lib SHARED IMPORTED)
set_target_properties(lame-lib
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/liblame.so)


#本地库=============================
add_library( # So文件名称
        lame-mp3
        SHARED
        lamemp3.cpp)
include_directories(include)

find_library(
        log-lib
        log)
target_link_libraries(
        lame-mp3
        android
        lame-lib
        # Links the log library to the target library.
        ${log-lib})
