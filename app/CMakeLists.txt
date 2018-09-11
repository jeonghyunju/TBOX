cmake_minimum_required(VERSION 3.4.1)

set(pathOPENCV /Users/hyunjujung/Desktop/openCV/OpenCV-android-sdk)
set(pathPROJECT /Users/hyunjujung/AndroidStudioProjects/TBOX)
set(pathLIBOPENCV_JAVA ${pathPROJECT}/app/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)
set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

include_directories(${pathOPENCV}/sdk/native/jni/include)

add_library( lib_opencv SHARED IMPORTED )

set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${pathLIBOPENCV_JAVA})

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp )

target_link_libraries( native-lib
                        lib_opencv
                        ${log-lib} )

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )