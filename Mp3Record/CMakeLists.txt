cmake_minimum_required(VERSION 3.4.1)

#引入cryptopp加密库=================
#动态库导入
set(LIB_RELEASE_SRC ${CMAKE_CURRENT_SOURCE_DIR}/../distribution/release/)
add_library(lame-lib SHARED IMPORTED)
set_target_properties(lame-lib
        PROPERTIES IMPORTED_LOCATION
        ${LIB_RELEASE_SRC}/lame/${ANDROID_ABI}/liblame.so)
#本地库=============================
add_library( # So文件名称
        lame-mp3
        SHARED
        src/main/cpp/lamemp3.cpp)
include_directories(src/main/cpp/include/)
target_include_directories(lame-mp3 PUBLIC ${LIB_RELEASE_SRC}/include/)
# 指定头文件路径
# 添加NDK库的支持，具体哪些库可以百度查看
find_library(
        log-lib
        log)

# 将NDK库关联到自己的库
target_link_libraries(
        lame-mp3
        android
        lame-lib
        # Links the log library to the target library.
        ${log-lib})
