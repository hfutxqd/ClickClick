//
// Created by vimfung on 16/8/23.
//

#ifndef SAMPLE_LUACONTEXT_H
#define SAMPLE_LUACONTEXT_H

#include "lua.hpp"
#include "LuaObject.h"
#include "LuaDefined.h"

namespace cn
{
    namespace vimfung
    {
        namespace luascriptcore
        {
            class LuaValue;
            class LuaModule;
            class LuaDataExchanger;
            class LuaSession;
            class LuaExportsTypeManager;
            class LuaExportTypeDescriptor;
            class LuaOperationQueue;
            class LuaError;
            class LuaScriptController;
			class LuaFunction;

            /**
             * Lua上下文环境, 维护原生代码与Lua之间交互的核心类型。
             */
            class LuaContext : public LuaObject
            {
            private:

                /**
                 * 导出原生类型处理器
                 */
                LuaExportsNativeTypeHandler _exportNativeTypeHandler;

                /**
                 * Lua运行异常处理器
                 */
                LuaExceptionHandler _exceptionHandler;

                /**
                 * 方法映射表
                 */
                LuaMethodMap _methodMap;

                /**
                 * 数据交换器
                 */
                LuaDataExchanger *_dataExchanger;

                /**
                 * 主会话对象
                 */
                LuaSession *_mainSession;

                /**
                 * 会话映射表，用于记录各个线程的会话链
                 */
                LuaSessionMap _sessionMap;

                /**
                 * 当前会话对象
                 */
//                LuaSession *_currentSession;
                
                /**
                 导出类型管理器
                 */
                LuaExportsTypeManager *_exportsTypeManager;

                /**
                 * 操作队列
                 */
                LuaOperationQueue *_operationQueue;
                
                /**
                 是否需要进行内存回收
                 */
                bool _needGC;
                
                /**
                 是否激活
                 */
                bool _isActive;

            public:

                /**
                 * 初始化上下文对象
                 *
                 * @param platform 平台类型：ios,android,unity3d
                 */
                LuaContext(std::string const& platform);

                /**
                 * 销毁上下文对象
                 */
                virtual ~LuaContext();

            private:

                /**
                 * 抛出异常
                 * @param error 异常信息
                 */
                void raiseException(LuaError *error);

            public:

                /**
                 * 导出原生类型时触发
                 * @param handler 事件处理器
                 */
                void onExportsNativeType(LuaExportsNativeTypeHandler handler);

                /**
                 * 导出原生类型
                 * @param typeName 类型名称
                 */
                void exportsNativeType(std::string const& typeName);

            public:

                /**
                 * 当lua执行异常时触发该事件
                 *
                 * @param handler 事件处理器
                 */
                void onException (LuaExceptionHandler handler);

                /**
                 * 抛出异常信息
                 *
                 * @param message 异常消息
                 */
                void raiseException (std::string const& message);

                /**
                 * 输出异常信息
                 * @param message 异常消息
                 */
                void outputExceptionMessage(std::string const& message);

                /**
                 * 捕获异常信息
                 *
                 * @return 异常捕获方法所在堆栈位置
                 */
                int catchException();

            public:

                /**
                 * 添加搜索路径
                 *
                 * @param path 路径
                 */
                void addSearchPath(std::string const& path);

                /**
                 * 设置全局变量
                 *
                 * @param name 变量名称
                 * @param value 变量值
                 */
                void setGlobal(std::string const& name, LuaValue *value);

                /**
                 * 获取全局变量
                 *
                 * @param name 变量名称
                 *
                 * @return 变量值
                 */
                LuaValue* getGlobal(std::string const& name);

                /**
                 * 保留Lua层的变量引用，使其不被GC所回收。
                 * 注：判断value能否被保留取决于value所保存的真实对象，所以只要保证保存对象一致，即使value为不同对象并不影响实际效果。
                 * 即：LuaValue *val1 = new LuaValue(obj1)与LuaValue *val2 = new LuaValue(obj1)传入方法中效果相同。
                 *
                 * @param value 对应Lua层变量的原生对象Value，如果value为非Lua回传对象则调用此方法无任何效果。
                 */
                void retainValue(LuaValue *value);

                /**
                 * 释放Lua层的变量引用，使其内存管理权交回Lua。
                 * 注：判断value能否被释放取决于value所保存的真实对象，所以只要保证保存对象一致，即使value为不同对象并不影响实际效果。
                 * 即：LuaValue *val1 = new LuaValue(obj1)与LuaValue *val2 = new LuaValue(obj1)传入方法中效果相同。
                 *
                 * @param value 对应Lua层变量的原生对象Value，如果value为非Lua回传对象则调用此方法无任何效果。
                 */
                void releaseValue(LuaValue *value);

                /**
                 * 解析脚本
                 *
                 * @param script 脚本内容
                 */
                LuaValue* evalScript(std::string const& script);

                /**
                 * 解析脚本
                 * @param script 脚本内容
                 * @param scriptController 脚本控制器
                 * @return 返回值
                 */
                LuaValue* evalScript(std::string const& script, LuaScriptController *scriptController);

                /**
                 * 从lua文件中解析脚本
                 *
                 * @param path lua文件路径
                 */
                LuaValue* evalScriptFromFile(std::string const& path);

                /**
                 * 从lua文件中解析脚本
                 * @param path lua文件路径
                 * @param scriptController 脚本控制器
                 * @return 返回值
                 */
                LuaValue* evalScriptFromFile(std::string const& path, LuaScriptController *scriptController);

                /**
                 * 调用方法
                 *
                 * @param methodName 方法名称
                 * @param arguments 参数列表
                 */
                LuaValue* callMethod(std::string const& methodName, LuaArgumentList *arguments);

                /**
                 * 调用方法
                 * @param methodName 方法名称
                 * @param arguments 参数列表
                 * @param scriptController 脚本控制器
                 * @return 返回值
                 */
                LuaValue* callMethod(std::string const& methodName, LuaArgumentList *arguments, LuaScriptController *scriptController);

                /**
                 * 注册方法
                 *
                 * @param methodName 方法名称
                 * @param handler 方法处理
                 */
                void registerMethod(std::string const& methodName, LuaMethodHandler handler);

                /**
                 * 执行线程
                 * @param handler  线程处理器
                 * @param arguments 参数列表
                 */
                void runThread(LuaFunction *handler, LuaArgumentList arguments);

                /**
                 * 执行线程
                 * @param handler 线程处理器
                 * @param arguments 参数列表
                 * @param scriptController 脚本控制器
                 */
                void runThread(LuaFunction *handler, LuaArgumentList arguments, LuaScriptController *scriptController);

            public:
                
                /**
                 获取上下文状态

                 @return true 表示可用， false 表示不可用，已经销毁
                 */
                bool isActive();

                /**
                 * 根据方法名称获取对应的方法处理器
                 *
                 * @param methodName 方法名称
                 *
                 * @return 方法处理器
                 */
                LuaMethodHandler getMethodHandler(std::string const& methodName);

                /**
                 * 获取数据数据交换层
                 */
                LuaDataExchanger *getDataExchanger();
                
                /**
                 获取导出类型管理器
                 
                 @return 导出类型管理器
                 */
                LuaExportsTypeManager* getExportsTypeManager();

                /**
                 * 获取操作队列
                 * @return 操作队列
                 */
                LuaOperationQueue* getOperationQueue();

                /**
                 * 创建会话
                 *
                 * @param state 状态
                 * @param lightweight 轻量级
                 * @return 会话对象
                 */
                LuaSession* makeSession(lua_State *state, bool lightweight);

                /**
                 * 销毁会话
                 *
                 * @param session 会话对象
                 */
                void destorySession(LuaSession *session);

                /**
                 * 获取主会话对象
                 *
                 * @return 主会话对象
                 */
                LuaSession* getMainSession();

                /**
                 * 获取当前会话对象
                 *
                 * @return 当前会话对象
                 */
                LuaSession* getCurrentSession();
                
                /**
                 内存回收
                 */
                void gc();
                
                /**
                 进行内存回收，由定时器进行调用
                 */
                void gcHandler();
            };

        }
    }
}


#endif //SAMPLE_LUACONTEXT_H
