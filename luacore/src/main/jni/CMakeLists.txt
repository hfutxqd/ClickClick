# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.0)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.


include_directories(lua-5.4.2 lua-common)

add_library( # Sets the name of the library.
            LuaScriptCore

            # Sets the library as a shared library.
            SHARED

            # Provides a relative path to your source file(s).
            # Associated headers in the same location as their source
            # file are automatically included.对应的C文件的目录位置
            cn_vimfung_luascriptcore_LuaNativeUtil.cpp
            LuaJavaConverter.cpp
            LuaJavaEnv.cpp
            LuaJavaExceptionHandler.cpp
            LuaJavaObjectDescriptor.cpp
            LuaJavaType.cpp
            LuaJavaExportTypeDescriptor.cpp
            LuaJavaExportMethodDescriptor.cpp
            LuaJavaExportPropertyDescriptor.cpp
            lua-5.4.2/lapi.c
            lua-5.4.2/lauxlib.c
            lua-5.4.2/lbaselib.c
            lua-5.4.2/lcode.c
            lua-5.4.2/lcorolib.c
            lua-5.4.2/lctype.c
            lua-5.4.2/ldblib.c
            lua-5.4.2/ldebug.c
            lua-5.4.2/ldo.c
            lua-5.4.2/ldump.c
            lua-5.4.2/lfunc.c
            lua-5.4.2/lgc.c
            lua-5.4.2/linit.c
            lua-5.4.2/liolib.c
            lua-5.4.2/llex.c
            lua-5.4.2/lmathlib.c
            lua-5.4.2/lmem.c
            lua-5.4.2/loadlib.c
            lua-5.4.2/lobject.c
            lua-5.4.2/lopcodes.c
            lua-5.4.2/loslib.c
            lua-5.4.2/lparser.c
            lua-5.4.2/lstate.c
            lua-5.4.2/lstring.c
            lua-5.4.2/lstrlib.c
            lua-5.4.2/ltable.c
            lua-5.4.2/ltablib.c
            lua-5.4.2/ltm.c
            lua-5.4.2/lua.c
            lua-5.4.2/lundump.c
            lua-5.4.2/lutf8lib.c
            lua-5.4.2/lvm.c
            lua-5.4.2/lzio.c
            lua-common/LuaCoroutine.cpp
            lua-common/LuaOperationQueue.cpp
            lua-common/LuaContext.cpp
            lua-common/LuaFunction.cpp
            lua-common/LuaNativeClass.cpp
            lua-common/LuaNativeClassFactory.cpp
            lua-common/LuaObject.cpp
            lua-common/LuaObjectDecoder.cpp
            lua-common/LuaObjectDescriptor.cpp
            lua-common/LuaObjectEncoder.cpp
            lua-common/LuaObjectManager.cpp
            lua-common/LuaPointer.cpp
            lua-common/LuaValue.cpp
            lua-common/LuaTmpValue.cpp
            lua-common/LuaTuple.cpp
            lua-common/StringUtils.cpp
            lua-common/LuaDataExchanger.cpp
            lua-common/LuaManagedObject.cpp
            lua-common/LuaSession.cpp
            lua-common/LuaEngineAdapter.cpp
            lua-common/LuaExportMethodDescriptor.cpp
            lua-common/LuaExportsTypeManager.cpp
            lua-common/LuaExportTypeDescriptor.cpp
            lua-common/LuaExportPropertyDescriptor.cpp
            lua-common/LuaError.cpp
            lua-common/LuaScriptController.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.指定依赖库
                       LuaScriptCore

                       # Links the target library to the log library
                       # included in the NDK.关联日志记录库文件，在ndk目录中
                       ${log-lib} )

