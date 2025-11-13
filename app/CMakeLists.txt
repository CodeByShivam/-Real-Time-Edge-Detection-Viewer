cmake_minimum_required(VERSION 3.10)
project(edgeviewer-native)

set(CMAKE_CXX_STANDARD 17)

# If you have OpenCV installed or supply the OpenCV SDK, set OpenCV_DIR accordingly or use find_package
# Example: set(OpenCV_DIR "${CMAKE_SOURCE_DIR}/opencv/sdk/native/jni")
find_package(OpenCV REQUIRED COMPONENTS core imgproc highgui)

include_directories(${OpenCV_INCLUDE_DIRS})
add_library(native-lib SHARED native-lib.cpp)

find_library(log-lib log)
target_link_libraries(native-lib ${OpenCV_LIBS} ${log-lib})
