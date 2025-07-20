cmake_minimum_required(VERSION 3.22.1)

project("virtualspace")

# 设置C++标准
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 设置编译选项
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra -O2")

# 添加编译定义
add_definitions(-DANDROID)
add_definitions(-D__ANDROID_API__=${ANDROID_NATIVE_API_LEVEL})

# 包含目录
include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}/Foundation
    ${CMAKE_CURRENT_SOURCE_DIR}/Substrate
    ${CMAKE_CURRENT_SOURCE_DIR}/utils
)

# 源文件
set(SOURCES
    Foundation/IORelocator.cpp
    Foundation/ProcessManager.cpp
    Foundation/SystemCallHook.cpp
    Substrate/SubstrateHook.cpp
    Substrate/ARMHook.cpp
    Substrate/ARM64Hook.cpp
    utils/LogUtils.cpp
    utils/FileUtils.cpp
    utils/StringUtils.cpp
)

# 创建共享库
add_library(virtualspace SHARED ${SOURCES})

# 链接库
target_link_libraries(virtualspace
    android
    log
    dl
)

# 设置输出目录
set_target_properties(virtualspace PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}
) 