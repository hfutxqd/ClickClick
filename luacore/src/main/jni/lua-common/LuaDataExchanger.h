//
// Created by 冯鸿杰 on 2017/5/11.
//

#ifndef ANDROID_LUADATAEXCHANGER_H
#define ANDROID_LUADATAEXCHANGER_H

#include "LuaObject.h"
#include "LuaValue.h"
#include "LuaManagedObject.h"

namespace cn {
    namespace vimfung {
        namespace luascriptcore {

            class LuaContext;
            class LuaValue;
            class LuaManagedObject;

            /**
             * 对象行为
             */
            enum LuaObjectAction
            {
                LuaObjectActionUnknown = 0,      //未知
                LuaObjectActionRetain = 1,       //保留
                LuaObjectActionRelease = 2,      //释放
            };

            /**
             * 数据交换层
             */
            class LuaDataExchanger : public LuaObject
            {
            private:
                LuaContext *_context;

            public:
                /**
                 * 初始化
                 *
                 * @param context 上下文对象
                 */
                LuaDataExchanger(LuaContext *context);

            public:

                /**
                 * 从栈中获取获取数据
                 *
                 * @param stackIndex 栈索引
                 *
                 * @return 值对象
                 */
                LuaValue* getValue(int stackIndex);

                /**
                 * 将数据压栈
                 *
                 * @param value 值对象
                 */
                void pushStack(LuaValue *value);

                /**
                 * 将数据压栈
                 * @param value 值对象
                 * @param state lua状态
                 * @param queue 队列
                 */
                void pushStack(LuaValue *value, lua_State *state, LuaOperationQueue *queue);

                /**
                 * 获取Lua对象并入栈
                 *
                 * @param object 原生对象
                 */
                void getLuaObject(LuaObject *object);

                /**
                 * 获取Lua对象并入栈
                 * @param object 原生对象
                 * @param state lua状态
                 */
                void getLuaObject(LuaObject *object, lua_State *state, LuaOperationQueue *queue);

                /**
                 * 设置Lua对象
                 *
                 * @param stackIndex 栈索引
                 * @param linkId 连接标识
                 */
                void setLuaObject(int stackIndex, std::string const& linkId);

                /**
                 * 保留对象对应在Lua中的引用
                 *
                 * @param object 原生对象
                 */
                void retainLuaObject(LuaObject *object);
    

                /**
                 * 释放对象对应在Lua中的引用
                 *
                 * @param object 原生对象
                 */
                void releaseLuaObject(LuaObject *object);

                /**
                 * 清除对象在Lua中的引用
                 * @param object 管理对象
                 */
                void clearObject(LuaManagedObject *object);

            private:

                /**
                 * 入栈对象
                 *
                 * @param object 对象
                 * @param state 状态
                 * @param queue 队列
                 */
                void pushStackByObject(LuaManagedObject *object, lua_State *state, LuaOperationQueue *queue);

                /**
                 * 入栈表格
                 *
                 * @param list 数组
                 * @param tableId table标识
                 * @param state 状态
                 * @param queue 队列
                 */
                void pushStackByTable(LuaValueList *list, std::string tableId, lua_State *state, LuaOperationQueue *queue);

                /**
                 * 入栈表格
                 *
                 * @param map 字典
                 * @param tableId table标识
                 * @param state 状态
                 * @param queue 队列
                 */
                void pushStackByTable(LuaValueMap *map, std::string tableId, lua_State *state, LuaOperationQueue *queue);

                /**
                 * 开始获取_vars_表
                 *
                 * @param state 状态
                 * @param queue 队列
                 */
                void beginGetVarsTable(lua_State *state, LuaOperationQueue *queue);

                /**
                 * 结束获取_vars_表
                 *
                 * @param state 状态
                 * @param queue 队列
                 */
                void endGetVarsTable(lua_State *state, LuaOperationQueue *queue);

                /**
                 * 执行对象行为
                 *
                 * @param linkId 连接标识
                 * @param action 行为
                 */
                void doObjectAction(std::string const& linkId, LuaObjectAction action);
            };

        }
    }
}


#endif //ANDROID_LUADATAEXCHANGER_H
