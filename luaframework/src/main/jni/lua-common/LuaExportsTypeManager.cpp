//
//  LuaExportsTypeManager.cpp
//  LuaScriptCore
//
//  Created by 冯鸿杰 on 2017/9/16.
//  Copyright © 2017年 冯鸿杰. All rights reserved.
//

#include "LuaExportsTypeManager.hpp"
#include "LuaExportTypeDescriptor.hpp"
#include "LuaExportPropertyDescriptor.hpp"
#include "lua.hpp"
#include "LuaContext.h"
#include "LuaSession.h"
#include "LuaValue.h"
#include "LuaEngineAdapter.hpp"
#include "LuaExportMethodDescriptor.hpp"
#include "LuaObjectDescriptor.h"
#include "LuaDataExchanger.h"
#include "LuaOperationQueue.h"
#include "StringUtils.h"

#include <algorithm>

using namespace cn::vimfung::luascriptcore;


/**
 * 类型映射处理
 * @param state 状态机
 * @return 参数数量
 */
static int typeMappingHandler(lua_State *state)
{
    LuaExportsTypeManager *manager = (LuaExportsTypeManager *)LuaEngineAdapter::toPointer(state, LuaEngineAdapter::upValueIndex(1));

    LuaSession *session = manager -> context() -> makeSession(state, false);


    if (LuaEngineAdapter::type(state, 1) != LUA_TTABLE)
    {
        session -> reportLuaException("please use the colon syntax to call the method");
    }
    else if (LuaEngineAdapter::getTop(state) < 4)
    {
        session -> reportLuaException("`typeMapping` method need to pass 3 parameters");
    }
    else
    {
        std::string platform = LuaEngineAdapter::toString(state, 2);
        std::transform(platform.begin(), platform.end(), platform.begin(), ::tolower);
        std::string nativeTypeName = LuaEngineAdapter::toString(state, 3);
        std::string alias = LuaEngineAdapter::toString(state, 4);
        manager -> _mappingType(platform, nativeTypeName, alias);
    }

    manager -> context() -> destorySession(session);

    return 0;
}

/**
 *  创建对象时处理
 *
 *  @param state 状态机
 *
 *  @return 参数数量
 */
static int objectCreateHandler (lua_State *state)
{
    LuaExportsTypeManager *manager = (LuaExportsTypeManager *)LuaEngineAdapter::toPointer(state, LuaEngineAdapter::upValueIndex(1));

    LuaSession *session = manager -> context() -> makeSession(state, false);

    LuaExportTypeDescriptor *typeDescriptor = NULL;
    LuaEngineAdapter::getField(state, 1, "_nativeType");
    if (LuaEngineAdapter::type(state, -1) == LUA_TLIGHTUSERDATA)
    {
        typeDescriptor = (LuaExportTypeDescriptor *)LuaEngineAdapter::toPointer(state, -1);
    }
    LuaEngineAdapter::pop(state, 1);

    if (typeDescriptor != NULL)
    {
        try
        {
            LuaObjectDescriptor *objectDescriptor = typeDescriptor -> createInstance(session);

            if (objectDescriptor != NULL)
            {
                manager -> _initLuaObject(objectDescriptor);
                objectDescriptor -> release();
            }
            else
            {
                throw "unknown error!";
            }
            
        }
        catch (std::exception & e)
        {
            std::string errMsg = StringUtils::format("construct `%s` instance fail :  %s", typeDescriptor -> typeName().c_str(), e.what());
            session -> reportLuaException(errMsg);
        }
        catch (...)
        {
            std::string errMsg = StringUtils::format("construct `%s` instance fail : Unknown Error!", typeDescriptor -> typeName().c_str());
            session -> reportLuaException(errMsg);
        }

    }
    else
    {
        
        session -> reportLuaException("can't construct instance, Invalid type!");
    }

    
    manager -> context() -> destorySession(session);
    
    return 1;
}

/**
 *  子类化
 *
 *  @param state 状态机
 *
 *  @return 参数数量
 */
static int subClassHandler (lua_State *state)
{
    LuaExportsTypeManager *manager = (LuaExportsTypeManager *)LuaEngineAdapter::toPointer(state, LuaEngineAdapter::upValueIndex(1));

    LuaContext *context = manager -> context();
    LuaSession *session = context -> makeSession(state, false);

    if (LuaEngineAdapter::type(state, 1) != LUA_TTABLE)
    {
        session -> reportLuaException("please use the colon syntax to call the method");
    }
    else if (LuaEngineAdapter::getTop(state) < 2 || LuaEngineAdapter::type(state, 2) != LUA_TSTRING)
    {
        session -> reportLuaException("missing parameter subclass name or argument type mismatch.");
    }
    else
    {
        //获取传入类型
        LuaExportTypeDescriptor *typeDescriptor = NULL;
        LuaEngineAdapter::getField(state, 1, "_nativeType");
        if (LuaEngineAdapter::type(state, -1) == LUA_TLIGHTUSERDATA)
        {
            typeDescriptor = (LuaExportTypeDescriptor *)LuaEngineAdapter::toPointer(state, -1);
        }
        LuaEngineAdapter::pop(state, 1);

        if (typeDescriptor != NULL)
        {
            std::string subclassName = LuaEngineAdapter::checkString(state, 2);

            //构建子类型描述
            LuaExportTypeDescriptor *subTypeDescriptor = typeDescriptor -> createSubType(session, subclassName);
            manager -> exportsType(subTypeDescriptor);
            manager -> _prepareExportsType(state, typeDescriptor);
            subTypeDescriptor -> release();
        }
        else
        {
            session -> reportLuaException("can't subclass type! Invalid base type.");
        }
    }
    
    context -> destorySession(session);

    return 0;
}

/**
 判断是否是该类型的子类
 
 @param state 状态机
 @return 参数数量
 */
static int subclassOfHandler (lua_State *state)
{
    LuaExportsTypeManager *manager = (LuaExportsTypeManager *)LuaEngineAdapter::toPointer(state, LuaEngineAdapter::upValueIndex(1));

    LuaContext *context = manager -> context();
    LuaSession *session = context -> makeSession(state, false);

    bool flag = false;

    if (LuaEngineAdapter::type(state, 1) != LUA_TTABLE)
    {
        session -> reportLuaException("Please use the colon syntax to call the method");
    }
    else if (LuaEngineAdapter::getTop(state) < 2 || LuaEngineAdapter::type(state, 2) != LUA_TTABLE)
    {
        session -> reportLuaException("missing parameter `type` or argument type mismatch.");
    }
    else
    {
        LuaExportTypeDescriptor *typeDescriptor = NULL;
        if (LuaEngineAdapter::type(state, 1) == LUA_TTABLE)
        {
            LuaEngineAdapter::getField(state, 1, "_nativeClass");
            if (LuaEngineAdapter::type(state, -1) == LUA_TLIGHTUSERDATA)
            {
                typeDescriptor = (LuaExportTypeDescriptor *)LuaEngineAdapter::toPointer(state, -1);
            }
            LuaEngineAdapter::pop(state, 1);
        }

        LuaExportTypeDescriptor *checkTypeDescriptor = NULL;
        if (LuaEngineAdapter::type(state, 2) == LUA_TTABLE)
        {
            LuaEngineAdapter::getField(state, 2, "_nativeType");
            if (LuaEngineAdapter::type(state, -1) == LUA_TLIGHTUSERDATA)
            {
                checkTypeDescriptor = (LuaExportTypeDescriptor *)LuaEngineAdapter::toPointer(state, -1);
            }
            LuaEngineAdapter::pop(state, 1);
        }

        if (typeDescriptor != NULL && checkTypeDescriptor != NULL)
        {
            flag = typeDescriptor -> subtypeOfType(checkTypeDescriptor);
        }
        else
        {
            session -> reportLuaException("Unknown error.");
        }
    }

    LuaEngineAdapter::pushBoolean(state, flag);

    context -> destorySession(session);

    return 1;
}

/**
 类型转换为字符串处理
 
 @param state 状态
 @return 参数数量
 */
static int classToStringHandler (lua_State *state)
{

    LuaExportsTypeManager *manager = (LuaExportsTypeManager *)LuaEngineAdapter::toPointer(state, LuaEngineAdapter::upValueIndex(1));

    LuaContext *context = manager -> context();
    LuaSession *session = context -> makeSession(state, false);
    
    LuaExportTypeDescriptor *curType = NULL;
    LuaEngineAdapter::getField(state, 1, "_nativeType");
    if (LuaEngineAdapter::type(state, -1) == LUA_TLIGHTUSERDATA)
    {
        curType = (LuaExportTypeDescriptor *)LuaEngineAdapter::toPointer(state, -1);
    }
    
    if (curType != NULL)
    {
        std::string descStr = StringUtils::format("[%s type]", curType -> typeName().c_str());
        LuaEngineAdapter::pushString(state, descStr.c_str());
    }
    else
    {
        session -> reportLuaException("Can not describe unknown type.");
        LuaEngineAdapter::pushNil(state);
    }
    
    context -> destorySession(session);

    return 1;
}

/**
 *  对象销毁处理
 *
 *  @param state 状态机
 *
 *  @return 参数数量
 */
static int objectDestroyHandler (lua_State *state)
{
    LuaExportsTypeManager *manager = (LuaExportsTypeManager *)LuaEngineAdapter::toUserdata(state, LuaEngineAdapter::upValueIndex(1));

    if (LuaEngineAdapter::getTop(state) > 0 && LuaEngineAdapter::isUserdata(state, 1))
    {
        LuaContext *context = manager -> context();
        
        //判断context是否激活状态，由于调用lua_close时GC会回收对象触发该方法，如果继续执行下面操作会导致崩溃。因此，在这里需要进行判断。
        if (context -> isActive())
        {
            LuaSession *session = context -> makeSession(state, false);
            
            LuaArgumentList args;
            session -> parseArguments(args);
            
            LuaObjectDescriptor *objDesc = args[0] -> toObject();
            objDesc -> getTypeDescriptor() -> destroyInstance(session, objDesc);
            
            int errFuncIndex = manager -> context() -> catchException();
            
            //调用实例对象的destroy方法
            LuaEngineAdapter::pushValue(state, 1);
            
            LuaEngineAdapter::getField(state, -1, "destroy");
            if (LuaEngineAdapter::isFunction(state, -1))
            {
                LuaEngineAdapter::pushValue(state, 1);
                LuaEngineAdapter::pCall(state, 1, 0, errFuncIndex);
                
                LuaEngineAdapter::pop(state, 1);
            }
            else
            {
                LuaEngineAdapter::pop(state, 2);
            }
            
            //移除异常捕获方法
            LuaEngineAdapter::remove(state, errFuncIndex);
            
            //释放实例对象
            objDesc -> release();
            
            for (LuaArgumentList::iterator it = args.begin(); it != args.end() ; ++it)
            {
                LuaValue *value = *it;
                value -> release();
            }
            
            context -> destorySession(session);
        }
    }
    
    return 0;
}

/**
 转换Prototype为字符串处理
 
 @param state 状态
 @return 参数数量
 */
static int prototypeToStringHandler (lua_State *state)
{
    LuaExportsTypeManager *manager = (LuaExportsTypeManager *)LuaEngineAdapter::toPointer(state, LuaEngineAdapter::upValueIndex(1));

    LuaContext *context = manager -> context();
    LuaSession *session = context -> makeSession(state, false);

    LuaExportTypeDescriptor *typeDescriptor = NULL;

    LuaEngineAdapter::getField(state, 1, "_nativeType");
    if (LuaEngineAdapter::type(state, -1) == LUA_TLIGHTUSERDATA)
    {
        typeDescriptor = (LuaExportTypeDescriptor *)LuaEngineAdapter::toPointer(state, -1);
    }

    if (typeDescriptor != NULL)
    {
        std::string descStr = StringUtils::format("[%s prototype]", typeDescriptor -> typeName().c_str());
        LuaEngineAdapter::pushString(state, descStr.c_str());
    }
    else
    {
        session -> reportLuaException("can not describe unknown prototype.");
        LuaEngineAdapter::pushNil(state);
    }

    context -> destorySession(session);

    return 1;
}

/**
 *  对象转换为字符串处理
 *
 *  @param state 状态机
 *
 *  @return 参数数量
 */
static int objectToStringHandler (lua_State *state)
{
    LuaExportsTypeManager *manager = (LuaExportsTypeManager *)LuaEngineAdapter::toPointer(state, LuaEngineAdapter::upValueIndex(1));

    LuaSession *session = manager -> context() -> makeSession(state, false);
    
    LuaArgumentList args;
    session -> parseArguments(args);
    
    LuaObjectDescriptor *objectDescriptor = args[0] -> toObject();
    LuaExportTypeDescriptor *typeDescriptor = objectDescriptor -> getTypeDescriptor();
    
    if (typeDescriptor)
    {
        std::string descStr = StringUtils::format("[%s object<%p>]", typeDescriptor -> typeName().c_str(), LuaEngineAdapter::toPointer(state, 1));
        LuaEngineAdapter::pushString(state, descStr.c_str());
    }
    else
    {
        session -> reportLuaException("can not describe unknown object.");
        LuaEngineAdapter::pushNil(state);
    }
    
    //释放参数内存
    for (LuaArgumentList::iterator it = args.begin(); it != args.end() ; ++it)
    {
        LuaValue *value = *it;
        value -> release();
    }
    
    manager -> context() -> destorySession(session);

    return 1;
}

/**
 判断是否是该类型的实例对象
 
 @param state 状态机
 @return 参数数量
 */
static int instanceOfHandler (lua_State *state)
{
    LuaExportsTypeManager *manager = (LuaExportsTypeManager *)LuaEngineAdapter::toPointer(state, LuaEngineAdapter::upValueIndex(1));

    LuaContext *context = manager -> context();
    LuaSession *session = context -> makeSession(state, false);
    bool flag = false;

    if (LuaEngineAdapter::getTop(state) < 2)
    {
        session -> reportLuaException("missing parameter `type` or argument type mismatch.");
    }
    else
    {
        //获取实例类型
        LuaExportTypeDescriptor *typeDescriptor = NULL;
        LuaEngineAdapter::getField(state, 1, "_nativeType");
        if (LuaEngineAdapter::type(state, -1) == LUA_TLIGHTUSERDATA)
        {
            typeDescriptor = (LuaExportTypeDescriptor *)LuaEngineAdapter::toPointer(state, -1);
        }
        LuaEngineAdapter::pop(state, 1);

        if (typeDescriptor != NULL)
        {
            if (LuaEngineAdapter::type(state, 2) == LUA_TTABLE)
            {
                LuaEngineAdapter::getField(state, 2, "_nativeType");
                if (LuaEngineAdapter::type(state, -1) == LUA_TLIGHTUSERDATA)
                {
                    LuaExportTypeDescriptor *checkTypeDescriptor = (LuaExportTypeDescriptor *)LuaEngineAdapter::toPointer(state, -1);

                    flag = typeDescriptor -> subtypeOfType(checkTypeDescriptor);

                }
            }
        }
    }

    LuaEngineAdapter::pushBoolean(state, flag);
    context -> destorySession(session);

    return 1;
}

/**
 类方法路由处理器
 
 @param state 状态
 @return 返回参数数量
 */
static int classMethodRouteHandler(lua_State *state)
{
    int retCount = 0;

    LuaExportsTypeManager *manager = (LuaExportsTypeManager *)LuaEngineAdapter::toPointer(state, LuaEngineAdapter::upValueIndex(1));

    const char *methodName = LuaEngineAdapter::toString(state, LuaEngineAdapter::upValueIndex(2));
    LuaContext *context = manager -> context();
    LuaSession *session = context -> makeSession(state, false);

    if (LuaEngineAdapter::type(state, 1) != LUA_TTABLE)
    {
        session -> reportLuaException("please use the colon syntax to call the method");
    }
    else
    {
        LuaExportTypeDescriptor *typeDescriptor = NULL;
        LuaEngineAdapter::getField(state, 1, "_nativeType");
        if (LuaEngineAdapter::type(state, -1) == LUA_TLIGHTUSERDATA)
        {
            typeDescriptor = (LuaExportTypeDescriptor *)LuaEngineAdapter::toPointer(state, -1);
        }
        LuaEngineAdapter::pop(state, 1);

        if (typeDescriptor != NULL)
        {
            LuaArgumentList args;
            session -> parseArguments(args, 2);

            LuaExportMethodDescriptor *methodDescriptor = typeDescriptor -> getClassMethod(methodName, args);
            if (methodDescriptor != NULL)
            {
                LuaValue *retValue = NULL;

                try
                {
                    retValue = methodDescriptor -> invoke(session, args);
                }
                catch (std::exception & e)
                {
                    std::string errMsg = StringUtils::format("call `%s` method fail : %s", methodName, e.what());
                    session -> reportLuaException(errMsg);
                }
                catch (...)
                {
                    std::string errMsg = StringUtils::format("call `%s` method fail : Unknown Error!", methodName);
                    session -> reportLuaException(errMsg);
                }

                if (retValue != NULL)
                {
                    retCount = session -> setReturnValue(retValue);
                    //释放返回值
                    retValue -> release();
                }
            }

            //释放参数内存
            for (LuaArgumentList::iterator it = args.begin(); it != args.end() ; ++it)
            {
                LuaValue *item = *it;
                item -> release();
            }
        }
        else
        {
            std::string errMsg = StringUtils::format("call `%s` method fail : invalid type", methodName);
            session -> reportLuaException(errMsg);
        }
    }

    //销毁会话
    context -> destorySession(session);
    
    return retCount;
}

/**
 实例方法路由处理
 
 @param state 状态
 @return 参数个数
 */
static int instanceMethodRouteHandler(lua_State *state)
{
    int returnCount = 0;
    
    LuaExportsTypeManager *manager = (LuaExportsTypeManager *)LuaEngineAdapter::toPointer(state, LuaEngineAdapter::upValueIndex(1));

    LuaExportTypeDescriptor *typeDescriptor = (LuaExportTypeDescriptor *)LuaEngineAdapter::toPointer(state, LuaEngineAdapter::upValueIndex(2));
    std::string methodName = LuaEngineAdapter::toString(state, LuaEngineAdapter::upValueIndex(3));
    
    LuaContext *context = manager -> context();
    LuaSession *session = context -> makeSession(state, false);
    
    if (LuaEngineAdapter::type(state, 1) != LUA_TUSERDATA)
    {
        std::string errMsg = "call " + methodName + " method error : missing self parameter, please call by instance:methodName(param)";
        session -> reportLuaException(errMsg);
        
        //回收内存
        LuaEngineAdapter::GC(state, LUA_GCCOLLECT, 0);
    }
    else
    {


        LuaArgumentList args;
        session -> parseArguments(args);

        LuaExportMethodDescriptor *methodDescriptor = typeDescriptor -> getInstanceMethod(methodName, args);
        if (methodDescriptor != NULL)
        {
            LuaValue *retValue = NULL;

            try
            {
                retValue = methodDescriptor -> invoke(session, args);
            }
            catch (std::exception & e)
            {
                std::string errMsg = StringUtils::format("call `%s` method fail : %s", methodName.c_str(), e.what());
                session -> reportLuaException(errMsg);
            }
            catch (...)
            {
                std::string errMsg = StringUtils::format("call `%s` method fail : Unknown Error!", methodName.c_str());
                session -> reportLuaException(errMsg);
            }

            if (retValue != NULL)
            {
                returnCount = session -> setReturnValue(retValue);
                //释放返回值
                retValue -> release();
            }
        }

        //释放参数内存
        for (LuaArgumentList::iterator it = args.begin(); it != args.end() ; ++it)
        {
            LuaValue *item = *it;
            item -> release();
        }
    }
    
    context -> destorySession(session);
    
    return returnCount;
}

/**
 实例对象更新索引处理
 
 @param state 状态机
 @return 参数数量
 */
static int instanceNewIndexHandler (lua_State *state)
{
    LuaExportsTypeManager *manager = (LuaExportsTypeManager *)LuaEngineAdapter::toPointer(state, LuaEngineAdapter::upValueIndex(1));

    LuaObjectDescriptor *instance = (LuaObjectDescriptor *)LuaEngineAdapter::toPointer(state, LuaEngineAdapter::upValueIndex(2));
    
    LuaSession *session = manager -> context() -> makeSession(state, true);
    
    std::string key = LuaEngineAdapter::toString(state, 2);
    
    LuaArgumentList arguments;
    
    //检测是否存在类型属性
    LuaExportPropertyDescriptor *propertyDescriptor = manager -> _findInstanceProperty(session,
                                                                                      instance -> getTypeDescriptor(),
                                                                                      key);
    if (propertyDescriptor != NULL)
    {
        //调用对象属性
        LuaValue *value = LuaValue::TmpValue(manager -> context(), 3);

        try
        {
            propertyDescriptor -> invokeSetter(session, instance, value);
        }
        catch (std::exception & e)
        {
            std::string errMsg = StringUtils::format("set `%s` property fail : %s", key.c_str(), e.what());
            session -> reportLuaException(errMsg);
        }
        catch (...)
        {
            std::string errMsg = StringUtils::format("set `%s` property fail : Unknown Error!", key.c_str());
            session -> reportLuaException(errMsg);
        }


        value -> release();
    }
    else
    {
        //先找到实例对象的元表，向元表添加属性
        LuaEngineAdapter::getMetatable(state, 1);
        if (LuaEngineAdapter::isTable(state, -1))
        {
            LuaEngineAdapter::pushValue(state, 2);
            LuaEngineAdapter::pushValue(state, 3);
            LuaEngineAdapter::rawSet(state, -3);
        }
        
        LuaEngineAdapter::pop(state, 1);
    }
    
    manager -> context() -> destorySession(session);

    return 0;
}

/**
 全局对象的index元方法处理
 
 @param state 状态
 @return 返回参数数量
 */
static int globalIndexMetaMethodHandler(lua_State *state)
{
    LuaExportsTypeManager *exporter = (LuaExportsTypeManager *)LuaEngineAdapter::toPointer(state, LuaEngineAdapter::upValueIndex(1));

    LuaSession *session = exporter -> context() -> makeSession(state, true);
    
    //获取key
    std::string key = LuaEngineAdapter::toString(state, 2);
    
    LuaEngineAdapter::rawGet(state, 1);
    if (LuaEngineAdapter::isNil(state, -1))
    {
        //检测是否该key是否为导出类型
        LuaExportTypeDescriptor *typeDescriptor = exporter -> _getMappingType(key);
        if (typeDescriptor != NULL)
        {
            //为导出类型
            LuaEngineAdapter::pop(state, 1);

            LuaEngineAdapter::pushString(state, typeDescriptor->typeName().c_str());
            LuaEngineAdapter::rawGet(state, 1);

            if (LuaEngineAdapter::isNil(state, -1))
            {
                LuaEngineAdapter::pop(state, 1);

                //导出类型
                exporter -> _prepareExportsType(state, typeDescriptor);

                //重新获取
                LuaEngineAdapter::pushString(state, typeDescriptor->typeName().c_str());
                LuaEngineAdapter::rawGet(state, 1);
            }

        }
    }
    
    exporter -> context() -> destorySession(session);

    return 1;
}

/**
 实例对象索引方法处理器
 
 @param state 状态
 @return 返回参数数量
 */
static int instanceIndexHandler(lua_State *state)
{
    int retValueCount = 1;

    LuaExportsTypeManager *exporter = (LuaExportsTypeManager *)LuaEngineAdapter::toPointer(state, LuaEngineAdapter::upValueIndex(1));

    LuaObjectDescriptor *instance = (LuaObjectDescriptor *)LuaEngineAdapter::toPointer(state, LuaEngineAdapter::upValueIndex(2));

    LuaSession *session = exporter -> context() -> makeSession(state, true);
    
    std::string key = LuaEngineAdapter::toString(state, 2);

    //检测元表是否包含指定值
    LuaEngineAdapter::getMetatable(state, 1);
    LuaEngineAdapter::pushValue(state, 2);
    LuaEngineAdapter::rawGet(state, -2);
    
    if (LuaEngineAdapter::isNil(state, -1))
    {
        LuaEngineAdapter::pop(state, 1);
        retValueCount = exporter -> _getInstancePropertyValue(session, instance, instance -> getTypeDescriptor(), key);
    }
    
    LuaEngineAdapter::remove(state, -1-retValueCount);
    
    exporter -> context() -> destorySession(session);
    
    return retValueCount;
}

/**
 设置原型的新属性处理
 
 @param state 状态
 @return 参数数量
 */
static int prototypeNewIndexHandler (lua_State *state)
{
    int index = LuaEngineAdapter::upValueIndex(1);
    const void *ptr = LuaEngineAdapter::toPointer(state, index);
    LuaExportsTypeManager *exporter = (LuaExportsTypeManager *)ptr;
    
    LuaSession *session = exporter -> context() -> makeSession(state, true);
    
    //t,k,v
    bool isPropertyReg = false;
    if (LuaEngineAdapter::type(state, 3) == LUA_TTABLE)
    {
        //检测是否为属性设置
        LuaFunction *getter = NULL;
        LuaFunction *setter = NULL;
        
        LuaEngineAdapter::getField(state, 3, "get");
        if (LuaEngineAdapter::type(state, -1) == LUA_TFUNCTION)
        {
            LuaValue *getterValue = LuaValue::ValueByIndex(exporter -> context(), -1);
            getter = getterValue -> toFunction();
        }
        
        LuaEngineAdapter::pop(state, 1);
        
        LuaEngineAdapter::getField(state, 3, "set");
        if (LuaEngineAdapter::type(state, -1) == LUA_TFUNCTION)
        {
            LuaValue *setterValue = LuaValue::ValueByIndex(exporter -> context(), -1);
            setter = setterValue -> toFunction();
        }
        
        LuaEngineAdapter::pop(state, 1);
        
        if (getter != NULL || setter != NULL)
        {
            isPropertyReg = true;
            
            //注册属性
            LuaEngineAdapter::getField(state, 1, "_nativeType");
            if (LuaEngineAdapter::type(state, -1) == LUA_TLIGHTUSERDATA)
            {
                LuaExportTypeDescriptor *typeDescriptor = (LuaExportTypeDescriptor *)LuaEngineAdapter::toPointer(state, -1);
                
                LuaValue *propertyNameValue = LuaValue::ValueByIndex(exporter -> context(), 2);
                
                LuaExportPropertyDescriptor *propertyDescriptor = new LuaExportPropertyDescriptor(propertyNameValue -> toString(), getter, setter);
                typeDescriptor -> addProperty(propertyDescriptor -> name(), propertyDescriptor);
                propertyDescriptor -> release();
            }
        }
    }
    
    if (!isPropertyReg)
    {
        //直接设置
        LuaEngineAdapter::rawSet(state, 1);
    }
    
    exporter -> context() -> destorySession(session);
    
    return 0;
}

LuaExportsTypeManager::LuaExportsTypeManager(LuaContext *context, std::string const& platform)
{
    _context = context;
    _platform = platform;
    
    _setupExportEnv();
    _setupExportType();
}

LuaExportsTypeManager::~LuaExportsTypeManager()
{
    //释放导出类型
    std::map<std::string, LuaExportTypeDescriptor*>::iterator it;
    for (it = _exportTypes.begin(); it != _exportTypes.end(); it++)
    {
        it -> second -> release();
    }
}

LuaContext* LuaExportsTypeManager::context()
{
    return _context;
}

std::string LuaExportsTypeManager::getPlatform()
{
    return _platform;
}

LuaExportTypeDescriptor* LuaExportsTypeManager::getExportTypeDescriptor(std::string const& name)
{
    std::map<std::string, LuaExportTypeDescriptor*>::iterator it = _exportTypes.find(name);
    if (it != _exportTypes.end())
    {
        return it -> second;
    }
    
    return NULL;
}

void LuaExportsTypeManager::exportsType(LuaExportTypeDescriptor *typeDescriptor)
{
    //先查找是否存在
    std::map<std::string, LuaExportTypeDescriptor*>::iterator typeIt = _exportTypes.find(typeDescriptor -> typeName());
    if (typeIt == _exportTypes.end())
    {
        typeDescriptor -> retain();
        _exportTypes[typeDescriptor -> nativeTypeName()] = typeDescriptor;
    }
}

bool LuaExportsTypeManager::_mappingType(std::string const& platform, std::string const& name, std::string const& alias)
{
    if (platform == _platform)
    {
        _exportTypesMapping[alias] = name;
        return true;
    }
    
    return false;
}

std::string LuaExportsTypeManager::_getTypeFullName(std::string const& name)
{
    std::map<std::string, std::string>::iterator it = _exportTypesMapping.find(name);
    if (it != _exportTypesMapping.end())
    {
        return it -> second;
    }

    return name;
}

LuaExportTypeDescriptor* LuaExportsTypeManager::_createTypeDescriptor(std::string const& name)
{
    //通知上下文加载原生类型
    _context->exportsNativeType(name);

    //重新获取类型
    LuaExportTypeDescriptor *typeDescriptor = NULL;
    std::string fullName = _getTypeFullName(name);
    std::map<std::string, LuaExportTypeDescriptor*>::iterator it = _exportTypes.find(fullName);
    if (it != _exportTypes.end())
    {
        typeDescriptor = it -> second;
    }
    return typeDescriptor;
}

LuaExportTypeDescriptor* LuaExportsTypeManager::_getMappingType(std::string const& name)
{
    std::string fullName = _getTypeFullName(name);

    LuaExportTypeDescriptor *typeDescriptor = NULL;
    std::map<std::string, LuaExportTypeDescriptor*>::iterator it = _exportTypes.find(fullName);
    if (it != _exportTypes.end())
    {
        typeDescriptor = it -> second;
    }
    else
    {
        //未生成类型，则进行类型生成
        typeDescriptor = _createTypeDescriptor(fullName);
    }

    return typeDescriptor;
}

void LuaExportsTypeManager::_prepareExportsType(lua_State *state, LuaExportTypeDescriptor *typeDescriptor)
{
    //判断父类是否为导出类型
    LuaExportTypeDescriptor *parentTypeDescriptor = typeDescriptor -> parentTypeDescriptor();
    if (parentTypeDescriptor == NULL && typeDescriptor -> typeName() != "Object")
    {
        parentTypeDescriptor = getExportTypeDescriptor("Object");
    }
    
    if (parentTypeDescriptor != NULL)
    {
        _context -> getOperationQueue() -> performAction([this, state, parentTypeDescriptor](){

            LuaEngineAdapter::getGlobal(state, parentTypeDescriptor -> typeName().c_str());
            LuaEngineAdapter::pop(state, 1);

        });
    }
    
    _exportsType(state, typeDescriptor);
}

void LuaExportsTypeManager::_exportsType(lua_State *state, LuaExportTypeDescriptor *typeDescriptor)
{
    _context -> getOperationQueue() -> performAction([this, state, typeDescriptor](){

        //创建类模块
        LuaEngineAdapter::newTable(state);

        //设置类名, since ver 1.3
        LuaEngineAdapter::pushString(state, typeDescriptor -> typeName().c_str());
        LuaEngineAdapter::setField(state, -2, "name");

        //关联本地类型
        LuaEngineAdapter::pushLightUserdata(state, (void *)(typeDescriptor));
        LuaEngineAdapter::setField(state, -2, "_nativeType");

        //导出声明的类方法
        _exportsClassMethods(state, typeDescriptor);

        //构造函数
        LuaEngineAdapter::pushLightUserdata(state, (void *)this);
        LuaEngineAdapter::pushCClosure(state, objectCreateHandler, 1);
        LuaEngineAdapter::setField(state, -2, "__call");

        //关联索引
        LuaEngineAdapter::pushValue(state, -1);
        LuaEngineAdapter::setField(state, -2, "__index");

        //类型描述
        LuaEngineAdapter::pushLightUserdata(state, (void *)this);
        LuaEngineAdapter::pushLightUserdata(state, (void *)typeDescriptor);
        LuaEngineAdapter::pushCClosure(state, classToStringHandler, 2);
        LuaEngineAdapter::setField(state, -2, "__tostring");

        //获取父类型
        LuaExportTypeDescriptor *parentTypeDescriptor = typeDescriptor -> parentTypeDescriptor();

        //关联父类模块
        if (parentTypeDescriptor != NULL)
        {
            //存在父类，则直接设置父类为元表
            LuaEngineAdapter::getGlobal(state, parentTypeDescriptor -> typeName().c_str());
            if (LuaEngineAdapter::isTable(state, -1))
            {
                //设置父类指向
                LuaEngineAdapter::pushValue(state, -1);
                LuaEngineAdapter::setField(state, -3, "super");

                //关联元表
                LuaEngineAdapter::setMetatable(state, -2);
            }
            else
            {
                LuaEngineAdapter::pop(state, 1);
            }
        }
        else
        {
            //添加子类化对象方法
            LuaEngineAdapter::pushLightUserdata(state, (void *)this);
            LuaEngineAdapter::pushCClosure(state, subClassHandler, 1);
            LuaEngineAdapter::setField(state, -2, "subclass");

            //增加子类判断方法, since ver 1.3
            LuaEngineAdapter::pushLightUserdata(state, (void *)this);
            LuaEngineAdapter::pushCClosure(state, subclassOfHandler, 1);
            LuaEngineAdapter::setField(state, -2, "subclassOf");

            //类型映射
            LuaEngineAdapter::pushLightUserdata(state, (void *)this);
            LuaEngineAdapter::pushCClosure(state, typeMappingHandler, 1);
            LuaEngineAdapter::setField(state, -2, "typeMapping");

            //Object需要创建一个新table来作为元表，否则无法使用元方法，如：print(Object);
            LuaEngineAdapter::newTable(state);

            //构造函数, Object需要在其元表中添加该构造方法
            LuaEngineAdapter::pushLightUserdata(state, (void *)this);
            LuaEngineAdapter::pushCClosure(state, objectCreateHandler, 1);
            LuaEngineAdapter::setField(state, -2, "__call");

            //类型描述
            LuaEngineAdapter::pushLightUserdata(state, (void *)this);
            LuaEngineAdapter::pushCClosure(state, classToStringHandler, 1);
            LuaEngineAdapter::setField(state, -2, "__tostring");

            LuaEngineAdapter::setMetatable(state, -2);
        }

        LuaEngineAdapter::setGlobal(state, typeDescriptor -> typeName().c_str());

        //---------创建实例对象原型表---------------
        LuaEngineAdapter::newMetatable(state, typeDescriptor -> prototypeTypeName().c_str());

        LuaEngineAdapter::getGlobal(state, typeDescriptor -> typeName().c_str());
        LuaEngineAdapter::setField(state, -2, "class");

        LuaEngineAdapter::pushLightUserdata(state, (void *)typeDescriptor);
        LuaEngineAdapter::setField(state, -2, "_nativeType");

        LuaEngineAdapter::pushValue(state, -1);
        LuaEngineAdapter::setField(state, -2, "__index");

        //增加__newindex元方法监听，主要用于原型中注册属性
        LuaEngineAdapter::pushLightUserdata(state, (void *)this);
        LuaEngineAdapter::pushCClosure(state, prototypeNewIndexHandler, 1);
        LuaEngineAdapter::setField(state, -2, "__newindex");

        LuaEngineAdapter::pushLightUserdata(state, (void *)this);
        LuaEngineAdapter::pushLightUserdata(state, (void *)typeDescriptor);
        LuaEngineAdapter::pushCClosure(state, prototypeToStringHandler, 2);
        LuaEngineAdapter::setField(state, -2, "__tostring");

        //给类元表绑定该实例元表
        LuaEngineAdapter::getGlobal(state, typeDescriptor -> typeName().c_str());
        LuaEngineAdapter::pushValue(state, -2);
        LuaEngineAdapter::setField(state, -2, "prototype");
        LuaEngineAdapter::pop(state, 1);

        //导出实例方法
        _exportsInstanceMethods(state, typeDescriptor);

        if (parentTypeDescriptor != NULL)
        {
            //关联父类
            LuaEngineAdapter::getMetatable(state, parentTypeDescriptor -> prototypeTypeName().c_str());
            if (LuaEngineAdapter::isTable(state, -1))
            {
                //设置父类访问属性 since ver 1.3
                LuaEngineAdapter::pushValue(state, -1);
                LuaEngineAdapter::setField(state, -3, "super");

                //设置父类元表
                LuaEngineAdapter::setMetatable(state, -2);
            }
            else
            {
                LuaEngineAdapter::pop(state, 1);
            }
        }
        else
        {
            //Object需要创建一个新table来作为元表，否则无法使用元方法，如：print(Object);
            LuaEngineAdapter::newTable(state);

            //增加__newindex元方法监听，主要用于Object原型中注册属性
            LuaEngineAdapter::pushLightUserdata(state, (void *)this);
            LuaEngineAdapter::pushCClosure(state, prototypeNewIndexHandler, 1);
            LuaEngineAdapter::setField(state, -2, "__newindex");

            LuaEngineAdapter::pushLightUserdata(state, (void *)this);
            LuaEngineAdapter::pushCClosure(state, prototypeToStringHandler, 1);
            LuaEngineAdapter::setField(state, -2, "__tostring");

            LuaEngineAdapter::setMetatable(state, -2);

            //Object类需要增加一些特殊方法
            //创建instanceOf方法 since ver 1.3
            LuaEngineAdapter::pushLightUserdata(state, (void *)this);
            LuaEngineAdapter::pushLightUserdata(state, (void *)typeDescriptor);
            LuaEngineAdapter::pushCClosure(state, instanceOfHandler, 2);
            LuaEngineAdapter::setField(state, -2, "instanceOf");
        }

        LuaEngineAdapter::pop(state, 1);

    });
}

void LuaExportsTypeManager::_exportsClassMethods(lua_State *state, LuaExportTypeDescriptor *typeDescriptor)
{
    _context -> getOperationQueue() -> performAction([this, state, typeDescriptor](){

        std::list<std::string> methodNameList = typeDescriptor -> classMethodNameList();
        for (std::list<std::string>::iterator it = methodNameList.begin(); it != methodNameList.end(); it++)
        {
            LuaEngineAdapter::getField(state, -1, (*it).c_str());
            if (!LuaEngineAdapter::isFunction(state, -1))
            {
                LuaEngineAdapter::pop(state, 1);

                LuaEngineAdapter::pushLightUserdata(state, (void *)this);
                LuaEngineAdapter::pushString(state, (*it).c_str());
                LuaEngineAdapter::pushCClosure(state, classMethodRouteHandler, 2);

                LuaEngineAdapter::setField(state, -2, (*it).c_str());
            }
            else
            {
                LuaEngineAdapter::pop(state, 1);
            }
        }

    });

}

void LuaExportsTypeManager::_exportsInstanceMethods(lua_State *state, LuaExportTypeDescriptor *typeDescriptor)
{
    _context -> getOperationQueue() -> performAction([this, state, typeDescriptor](){

        std::list<std::string> methodNameList = typeDescriptor -> instanceMethodNameList();
        for (std::list<std::string>::iterator it = methodNameList.begin(); it != methodNameList.end(); it++)
        {
            LuaEngineAdapter::getField(state, -1, (*it).c_str());
            if (!LuaEngineAdapter::isFunction(state, -1))
            {
                LuaEngineAdapter::pop(state, 1);

                LuaEngineAdapter::pushLightUserdata(state, (void *)this);
                LuaEngineAdapter::pushLightUserdata(state, (void *)typeDescriptor);
                LuaEngineAdapter::pushString(state, (*it).c_str());
                LuaEngineAdapter::pushCClosure(state, instanceMethodRouteHandler, 3);

                LuaEngineAdapter::setField(state, -2, (*it).c_str());
            }
            else
            {
                LuaEngineAdapter::pop(state, 1);
            }
        }

    });
}

void LuaExportsTypeManager::_setupExportType()
{
    //建立一个基类Object的描述
    exportsType(LuaExportTypeDescriptor::objectTypeDescriptor());
}

void LuaExportsTypeManager::_setupExportEnv()
{
    _context -> getOperationQueue() -> performAction([this](){

        //为_G设置元表，用于监听其对象的获取，从而找出哪些是导出类型
        lua_State *state = _context -> getCurrentSession() -> getState();
        LuaEngineAdapter::getGlobal(state, "_G");

        if (!LuaEngineAdapter::isTable(state, -1))
        {
            _context -> getCurrentSession() -> reportLuaException("invalid '_G' object，setup the exporter fail.");
            LuaEngineAdapter::pop(state, 1);
            return;
        }

        //创建_G元表
        LuaEngineAdapter::newTable(state);

        //监听__index元方法
        LuaEngineAdapter::pushLightUserdata(state, (void *)this);
        LuaEngineAdapter::pushCClosure(state, globalIndexMetaMethodHandler, 1);
        LuaEngineAdapter::setField(state, -2, "__index");

        //绑定为_G元表
        LuaEngineAdapter::setMetatable(state, -2);

        LuaEngineAdapter::pop(state, 1);

    });
}

void LuaExportsTypeManager::createLuaObject(LuaObjectDescriptor *objectDescriptor)
{
    LuaExportTypeDescriptor *typeDescriptor = objectDescriptor -> getTypeDescriptor();
    if (typeDescriptor)
    {
        _context -> getOperationQueue() -> performAction([this, typeDescriptor](){

            lua_State *state = _context -> getCurrentSession() -> getState();

            //getGlobal可以使类型自动导入
            LuaEngineAdapter::getGlobal(state, typeDescriptor -> typeName().c_str());
            LuaEngineAdapter::pop(state, 1);

        });

        _initLuaObject(objectDescriptor);
    }
}

void LuaExportsTypeManager::_initLuaObject(LuaObjectDescriptor *objectDescriptor)
{
    _context -> getOperationQueue() -> performAction([this, objectDescriptor](){

        lua_State *state = _context -> getCurrentSession() -> getState();
        int errFuncIndex = _context -> catchException();

        _bindLuaInstance(objectDescriptor);

        //通过_createLuaInstanceWithState方法后会创建实例并放入栈顶
        //调用实例对象的init方法
        LuaEngineAdapter::getField(state, -1, "init");
        if (LuaEngineAdapter::isFunction(state, -1))
        {
            LuaEngineAdapter::pushValue(state, -2);

            //将create传入的参数传递给init方法
            //-4 代表有4个非参数值在栈中，由栈顶开始计算，分别是：实例对象，init方法，实例对象, 错误捕获方法
            int paramCount = LuaEngineAdapter::getTop(state) - 4;
            //从索引2开始传入参数，ver2.2后使用冒号调用create方法，忽略第一个参数self
            for (int i = 2; i <= paramCount; i++)
            {
                LuaEngineAdapter::pushValue(state, i);
            }

            LuaEngineAdapter::pCall(state, paramCount, 0, errFuncIndex);
        }
        else
        {
            LuaEngineAdapter::pop(state, 1);    //出栈init方法
        }

        LuaEngineAdapter::remove(state, errFuncIndex); //出栈异常捕获方法

    });

}

void LuaExportsTypeManager::_bindLuaInstance(LuaObjectDescriptor *objectDescriptor)
{
    _context -> getOperationQueue() -> performAction([this, objectDescriptor](){

        lua_State *state = _context -> getCurrentSession() -> getState();

        //先为实例对象在lua中创建内存
        LuaUserdataRef ref = (LuaUserdataRef)LuaEngineAdapter::newUserdata(state, sizeof(LuaUserdataRef));

        if (objectDescriptor != NULL)
        {
            //创建本地实例对象，赋予lua的内存块并进行保留引用
            ref -> value = objectDescriptor;

            //引用对象
            objectDescriptor -> retain();
        }

        //创建一个临时table作为元表，用于在lua上动态添加属性或方法
        LuaEngineAdapter::newTable(state);

        //设置__index元方法为路由方法，用于检测对象的属性
        LuaEngineAdapter::pushLightUserdata(state, this);
        LuaEngineAdapter::pushLightUserdata(state, objectDescriptor);
        LuaEngineAdapter::pushCClosure(state, instanceIndexHandler, 2);
        LuaEngineAdapter::setField(state, -2, "__index");

        LuaEngineAdapter::pushLightUserdata(state, this);
        LuaEngineAdapter::pushLightUserdata(state, objectDescriptor);
        LuaEngineAdapter::pushCClosure(state, instanceNewIndexHandler, 2);
        LuaEngineAdapter::setField(state, -2, "__newindex");

        LuaEngineAdapter::pushLightUserdata(state, this);
        LuaEngineAdapter::pushCClosure(state, objectDestroyHandler, 1);
        LuaEngineAdapter::setField(state, -2, "__gc");

        LuaEngineAdapter::pushLightUserdata(state, this);
        LuaEngineAdapter::pushCClosure(state, objectToStringHandler, 1);
        LuaEngineAdapter::setField(state, -2, "__tostring");

        LuaEngineAdapter::pushValue(state, -1);
        LuaEngineAdapter::setMetatable(state, -3);

        LuaEngineAdapter::getMetatable(state, objectDescriptor -> getTypeDescriptor() -> prototypeTypeName().c_str());
        if (LuaEngineAdapter::isTable(state, -1))
        {
            LuaEngineAdapter::setMetatable(state, -2);
        }
        else
        {
            LuaEngineAdapter::pop(state, 1);
        }

        LuaEngineAdapter::pop(state, 1);

        //将创建对象放入到_vars_表中，主要修复对象创建后，在init中调用方法或者访问属性，由于对象尚未记录在_vars_中，而循环创建lua对象，并导致栈溢出。
        this -> context() -> getDataExchanger() -> setLuaObject(-1, objectDescriptor -> getExchangeId());

    });
}

int LuaExportsTypeManager::_getInstancePropertyValue(LuaSession *session,
                                                     LuaObjectDescriptor *instance,
                                                     LuaExportTypeDescriptor *typeDescriptor,
                                                     std::string const& propertyName)
{
    int retValueCount = 1;
    if (typeDescriptor != NULL)
    {
        _context -> getOperationQueue() -> performAction([=, &retValueCount](){

            lua_State *state = session -> getState();

            LuaEngineAdapter::getMetatable(state, typeDescriptor -> prototypeTypeName().c_str());
            LuaEngineAdapter::pushString(state, propertyName.c_str());
            LuaEngineAdapter::rawGet(state, -2);

            if (LuaEngineAdapter::isNil(state, -1))
            {
                LuaEngineAdapter::pop(state, 1);

                //不存在
                LuaExportPropertyDescriptor *propertyDescriptor = typeDescriptor -> getProperty(propertyName);
                if (propertyDescriptor == NULL)
                {
                    if (typeDescriptor -> parentTypeDescriptor() != NULL)
                    {
                        //递归父类
                        retValueCount = _getInstancePropertyValue(
                                session,
                                instance,
                                typeDescriptor -> parentTypeDescriptor(),
                                propertyName);
                    }
                    else
                    {
                        LuaEngineAdapter::pushNil(state);
                    }
                }
                else
                {
                    if (propertyDescriptor -> canRead())
                    {
                        try
                        {
                            LuaValue *retValue = propertyDescriptor -> invokeGetter(session, instance);
                            retValueCount = session -> setReturnValue(retValue);
                        }
                        catch (std::exception & e)
                        {
                            std::string errMsg = StringUtils::format("get `%s` property fail : %s", propertyName.c_str(), e.what());
                            session -> reportLuaException(errMsg);
                        }
                        catch (...)
                        {
                            std::string errMsg = StringUtils::format("get `%s` property fail : Unknown Error!", propertyName.c_str());
                            session -> reportLuaException(errMsg);
                        }

                    }
                    else
                    {
                        LuaEngineAdapter::pushNil(state);
                    }
                }
            }

            LuaEngineAdapter::remove(state, -1-retValueCount);

        });


    }

    return retValueCount;

}

LuaExportPropertyDescriptor* LuaExportsTypeManager::_findInstanceProperty(LuaSession *session,
                                                                          LuaExportTypeDescriptor *typeDescriptor,
                                                                          std::string const& propertyName)
{
    LuaExportPropertyDescriptor *propertyDescriptor = NULL;
    if (typeDescriptor != NULL)
    {
        _context -> getOperationQueue() -> performAction([=, &propertyDescriptor](){

            lua_State *state = session -> getState();

            LuaEngineAdapter::getMetatable(state, typeDescriptor -> prototypeTypeName().c_str());
            LuaEngineAdapter::pushString(state, propertyName.c_str());
            LuaEngineAdapter::rawGet(state, -2);

            if (LuaEngineAdapter::isNil(state, -1))
            {
                //不存在
                propertyDescriptor = typeDescriptor -> getProperty(propertyName);
                if (propertyDescriptor == NULL)
                {
                    if (typeDescriptor -> parentTypeDescriptor() != NULL)
                    {
                        //递归父类
                        propertyDescriptor = _findInstanceProperty(session, typeDescriptor -> parentTypeDescriptor(), propertyName);
                    }
                }
            }

            LuaEngineAdapter::pop(state, 2);

        });


    }

    return propertyDescriptor;
}
