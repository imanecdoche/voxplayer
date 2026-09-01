cmake_minimum_required(VERSION 3.22.1)

project("vox-dsp")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(
    vox-dsp
    SHARED
    vox_dsp_jni.cpp
    audio_decoder.cpp
    signal_processor.cpp
)

find_library(
    log-lib
    log
)

find_library(
    mediandk-lib
    mediandk
)

find_library(
    android-lib
    android
)

target_link_libraries(
    vox-dsp
    ${log-lib}
    ${mediandk-lib}
    ${android-lib}
)
