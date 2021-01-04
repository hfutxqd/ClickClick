//
// Created by vimfung on 16/8/23.
//

#include <stddef.h>
#include <typeinfo>
#include <cstring>
#include "LuaValue.h"
#include "LuaContext.h"
#include "LuaObjectManager.h"
#include "LuaPointer.h"
#include "LuaObjectEncoder.hpp"
#include "LuaObjectDecoder.hpp"
#include "LuaObjectDescriptor.h"
#include "LuaFunction.h"
#include "LuaTuple.h"
#include "LuaNativeClass.hpp"
#include "LuaDataExchanger.h"
#include "LuaExportTypeDescriptor.hpp"
#include "LuaExportsTypeManager.hpp"
#include "LuaTmpValue.hpp"
#include "StringUtils.h"
#include "LuaOperationQueue.h"

using namespace cn::vimfung::luascriptcore;

DECLARE_NATIVE_CLASS(LuaValue);

LuaValue::LuaValue()
        : LuaObject(), _context(NULL)
{
    _type = LuaValueTypeNil;
    _value = NULL;
    _hasManagedObject = false;
}

LuaValue::LuaValue(long value)
        : LuaObject(), _context(NULL)
{
    _type = LuaValueTypeInteger;
    _intValue = (lua_Integer)value;
    _value = NULL;
    _hasManagedObject = false;
}

LuaValue::LuaValue(bool value)
        : LuaObject(), _context(NULL)
{
    _type = LuaValueTypeBoolean;
    _booleanValue = value;
    _value = NULL;
    _hasManagedObject = false;
}

LuaValue::LuaValue(double value)
        : LuaObject(), _context(NULL)
{
    _type = LuaValueTypeNumber;
    _numberValue = value;
    _value = NULL;
    _hasManagedObject = false;
}

LuaValue::LuaValue(std::string const& value)
        : LuaObject(), _context(NULL)
{
    _type = LuaValueTypeString;
    _value = new std::string(value);
    _hasManagedObject = false;
}

LuaValue::LuaValue(const char *bytes, size_t length)
        : LuaObject(), _context(NULL)
{
    _type = LuaValueTypeData;
    _bytesLen = length;
    _value = new char[_bytesLen];
    std::memcpy(_value, bytes, _bytesLen);
    _hasManagedObject = false;
}

LuaValue::LuaValue(LuaValueList value, std::string tableId)
        : LuaObject(), _context(NULL), _tableId(tableId)
{
    _type = LuaValueTypeArray;
    _value = new LuaValueList(value);
    _hasManagedObject = false;
}

LuaValue::LuaValue(LuaValueMap value, std::string tableId)
        : LuaObject(), _context(NULL), _tableId(tableId)
{
    _type = LuaValueTypeMap;
    _value = new LuaValueMap(value);
    _hasManagedObject = false;
}

LuaValue::LuaValue (LuaPointer *value)
        :LuaObject(), _context(NULL)
{
    _type = LuaValueTypePtr;

    value -> retain();
    _value = (void *)value;
    _hasManagedObject = false;
}

LuaValue::LuaValue (LuaObjectDescriptor *value)
        : LuaObject(), _context(NULL)
{
    _type = LuaValueTypeObject;

    value -> retain();
    _value = (void *)value;
    _hasManagedObject = false;
}

LuaValue::LuaValue(LuaFunction *value)
        : LuaObject(), _context(NULL)
{
    _type = LuaValueTypeFunction;

    value -> retain();
    _value = (void *)value;
    _hasManagedObject = false;
}

LuaValue::LuaValue (LuaTuple *value)
        : LuaObject(), _context(NULL)
{
    _type = LuaValueTypeTuple;

    value -> retain();
    _value = (void *)value;
    _hasManagedObject = false;
}

LuaValue::LuaValue (LuaExportTypeDescriptor *value)
        : LuaObject(), _context(NULL)
{
    _type = LuaValueTypeClass;

    value -> retain();
    _value = (void *)value;
    _hasManagedObject = false;
}

LuaValue::LuaValue(LuaObjectDecoder *decoder)
    : LuaObject(decoder)
{
    _hasManagedObject = false;
	_value = NULL;
	_intValue = 0;
	_numberValue = 0;
	_booleanValue = false;
	_bytesLen = 0;
    
    int contextId = decoder -> readInt32();
    if (contextId > 0)
    {
        _context = dynamic_cast<LuaContext *>(LuaObjectManager::SharedInstance() -> getObject(contextId));
    }
    else
    {
        _context = NULL;
    }
    ///TODO: 调整C#下tableID的类型，让其直接关联lua中的tableId
    std::string tableId = decoder -> readString();
    
    _type = (LuaValueType)decoder -> readInt16();

    switch (_type)
    {
        case LuaValueTypeInteger:
            _intValue = decoder -> readInt32();
            break;
        case LuaValueTypeNumber:
            _numberValue = decoder -> readDouble();
            break;
        case LuaValueTypeBoolean:
            _booleanValue = decoder -> readByte();
            break;
        case LuaValueTypeString:
            _value = new std::string(decoder -> readString());
            break;
        case LuaValueTypeData:
            decoder -> readBytes(&_value, (int *)&_bytesLen);
            break;
        case LuaValueTypeArray:
        {
            _tableId = tableId;

            int size = decoder -> readInt32();
            LuaValueList list;
            for (int i = 0; i < size; i++)
            {
                LuaValue *item = dynamic_cast<LuaValue *>(decoder->readObject());
                list.push_back(item);
            }
            _value = new LuaValueList(list);
            break;
        }
        case LuaValueTypeMap:
        {
            _tableId = tableId;

            int size = decoder -> readInt32();
            LuaValueMap map;
            for (int i = 0; i < size; i++)
            {
                std::string key = decoder -> readString();
                LuaValue *item = dynamic_cast<LuaValue *>(decoder -> readObject());
                if (item != NULL)
                {
                    map[key] = item;
                }
            }
            _value = new LuaValueMap(map);
            break;
        }
        case LuaValueTypeTuple:
        {
            _value = dynamic_cast<LuaTuple *>(decoder -> readObject());
            break;
        }
        case LuaValueTypeFunction:
        {
            _value = dynamic_cast<LuaFunction *>(decoder -> readObject());
            break;
        }
        case LuaValueTypePtr:
        {
            _value = dynamic_cast<LuaPointer *>(decoder -> readObject());
            break;
        }
        case LuaValueTypeObject:
        {
            _value = decoder -> readObject();
            break;
        }
        case LuaValueTypeClass:
        {
            //类型名称
            std::string typeName = decoder -> readString();
            LuaExportTypeDescriptor *typeDescriptor = decoder -> getContext() -> getExportsTypeManager() -> getExportTypeDescriptor(typeName);
            if (typeDescriptor != NULL)
            {
                typeDescriptor -> retain();
                _value = typeDescriptor;
            }
            break;
        }
        default:
            _value = NULL;
            break;
    }
}

LuaValue::~LuaValue()
{
    if (_hasManagedObject && _context != NULL)
    {
        _hasManagedObject = false;
        _context -> getDataExchanger() -> releaseLuaObject(this);
    }

    if (_value != NULL)
    {
        if (_type == LuaValueTypePtr
                 || _type == LuaValueTypeObject
                 || _type == LuaValueTypeFunction
                 || _type == LuaValueTypeTuple
                 || _type == LuaValueTypeClass)
        {
            ((LuaObject *)_value) -> release();
        }
        else if (_type == LuaValueTypeArray)
        {
            //对于Table类型需要释放其子对象内存
            LuaValueList *arrayValue = static_cast<LuaValueList *> (_value);
            if (arrayValue != NULL)
            {
                //为数组对象
                for (LuaValueList::iterator i = arrayValue -> begin(); i != arrayValue -> end(); ++i)
                {
                    LuaValue *value = *i;
                    value -> release();
                }
            }
        }
        else if (_type == LuaValueTypeMap)
        {
            //为字典对象
            LuaValueMap *mapValue = static_cast<LuaValueMap *> (_value);
            if (mapValue != NULL)
            {
                for (LuaValueMap::iterator i = mapValue -> begin(); i != mapValue -> end(); ++i)
                {
                    i -> second -> release();
                }
            }
        }

        if (_type != LuaValueTypePtr
            && _type != LuaValueTypeObject
            && _type != LuaValueTypeFunction
            && _type != LuaValueTypeTuple
            && _type != LuaValueTypeClass)
        {
            switch (_type)
            {
                case LuaValueTypeString:
                    //fixed：string无法直接通过delete释放，需要使用swap来实现释放操作
                    std::string().swap(*((std::string *)_value));
                    break;
                case LuaValueTypeMap:
                    LuaValueMap().swap(*((LuaValueMap *)_value));
                    break;
                case LuaValueTypeArray:
                    LuaValueList().swap(*((LuaValueList *)_value));
                    break;
                default:
                    break;
            }

            delete[] (char *)_value;
        }

        _value = NULL;
    }
}

LuaValue* LuaValue::NilValue()
{
    return new LuaValue();
}

LuaValue* LuaValue::IntegerValue(long value)
{
    return new LuaValue(value);
}

LuaValue* LuaValue::BooleanValue(bool value)
{
    return new LuaValue(value);
}

LuaValue* LuaValue::NumberValue(double value)
{
    return new LuaValue(value);
}

LuaValue* LuaValue::StringValue(std::string const& value)
{
    return new LuaValue(value);
}

LuaValue* LuaValue::DataValue(const char *bytes, size_t length)
{
    return new LuaValue(bytes, length);
}

LuaValue* LuaValue::ArrayValue(LuaValueList value, std::string tableId)
{
    return new LuaValue(value, tableId);
}

LuaValue* LuaValue::DictonaryValue(LuaValueMap value, std::string tableId)
{
    return new LuaValue(value, tableId);
}

LuaValue* LuaValue::PointerValue(LuaPointer *value)
{
    return new LuaValue(value);
}

LuaValue* LuaValue::FunctionValue(LuaFunction *value)
{
    return new LuaValue(value);
}

LuaValue* LuaValue::TupleValue(LuaTuple *value)
{
    return new LuaValue(value);
}

LuaValue* LuaValue::ObjectValue(LuaObjectDescriptor *value)
{
    return new LuaValue(value);
}

LuaValue* LuaValue::ValueByIndex(LuaContext *context, int index)
{
    LuaValue *value = context -> getDataExchanger() -> getValue(index);
    value -> managedObject(context);

    return value;
}

LuaValue* LuaValue::TmpValue(LuaContext *context, int index)
{
    return new LuaTmpValue(context, index);
}

LuaValueType LuaValue::getType()
{
    return _type;
}

std::string LuaValue::typeName()
{
    static std::string name = typeid(LuaValue).name();
    return name;
}

void LuaValue::push(LuaContext *context)
{
    context -> getDataExchanger() -> pushStack(this);
}

lua_Integer LuaValue::toInteger()
{
    if (_type == LuaValueTypeInteger)
    {
        return _intValue;
    }
    return 0;
}


const std::string LuaValue::toString()
{
    if (_type == LuaValueTypeString)
    {
        return *((const std::string *)_value);
    }

    return NULL;
}

double LuaValue::toNumber()
{
    if (_type == LuaValueTypeNumber)
    {
        return _numberValue;
    }

    return 0;
}

bool LuaValue::toBoolean()
{
    if (_type == LuaValueTypeBoolean)
    {
        return  _booleanValue;
    }

    return false;
}

const char* LuaValue::toData()
{
    if (_type == LuaValueTypeData)
    {
        return (const char *)_value;
    }

    return NULL;
}

size_t LuaValue::getDataLength()
{
    if (_type == LuaValueTypeData)
    {
        return _bytesLen;
    }

    return 0;
}

LuaValueList* LuaValue::toArray()
{
    if (_type == LuaValueTypeArray)
    {
        return static_cast<LuaValueList *>(_value);
    }

    return NULL;
}

LuaValueMap* LuaValue::toMap()
{
    if (_type == LuaValueTypeMap)
    {
        return static_cast<LuaValueMap *>(_value);
    }

    return NULL;
}

LuaPointer* LuaValue::toPointer()
{
    if (_type == LuaValueTypePtr)
    {
        return (LuaPointer *)_value;
    }

    return NULL;
}

LuaFunction* LuaValue::toFunction()
{
    if (_type == LuaValueTypeFunction)
    {
        return (LuaFunction *)_value;
    }

    return NULL;
}

LuaTuple* LuaValue::toTuple()
{
    if (_type == LuaValueTypeTuple)
    {
        return (LuaTuple *)_value;
    }

    return NULL;
}

LuaExportTypeDescriptor* LuaValue::toType()
{
    if (_type == LuaValueTypeClass)
    {
        return (LuaExportTypeDescriptor *)_value;
    }

    return NULL;
}

LuaObjectDescriptor* LuaValue::toObject()
{
    if (_type == LuaValueTypeObject)
    {
        return (LuaObjectDescriptor *)_value;
    }
    else if (_type == LuaValueTypePtr)
    {
        return (LuaObjectDescriptor *)((LuaPointer *)_value) -> getValue() -> value;
    }

    return NULL;
}

void LuaValue::serialization (LuaObjectEncoder *encoder)
{
    LuaObject::serialization(encoder);
    
    //增加contextId和tableId
    if (_context != NULL)
    {
        encoder -> writeInt32(_context -> objectId());
    }
    else
    {
        encoder -> writeInt32(0);
    }

    encoder -> writeString(_tableId);
    
    encoder -> writeInt16(getType());
    
    switch (getType())
    {
        case LuaValueTypeNumber:
        {
            encoder -> writeDouble(toNumber());
            break;
        }
        case LuaValueTypeInteger:
        {
            encoder -> writeInt32((int)toInteger());
            break;
        }
        case LuaValueTypeString:
        {
            encoder -> writeString(toString());
            break;
        }
        case LuaValueTypeData:
        {
            const char *bytes = toData();
            size_t dataLen = getDataLength();
            encoder -> writeInt32((int)dataLen);
            encoder -> writeBuffer(bytes, (int)dataLen);
            break;
        }
        case LuaValueTypeArray:
        {
            LuaValueList *list = toArray();
            encoder -> writeInt32((int)list -> size());
            for (LuaValueList::iterator it = list -> begin(); it != list -> end(); ++it)
            {
                LuaValue *value = *it;
                encoder -> writeObject(value);
            }
            break;
        }
        case LuaValueTypeMap:
        {
            LuaValueMap *map = toMap();
            encoder -> writeInt32((int)map -> size());
            for (LuaValueMap::iterator it = map -> begin(); it != map -> end(); ++it)
            {
                std::string key = it -> first;
                LuaValue *value = it -> second;
                
                encoder -> writeString(key);
                encoder -> writeObject(value);
            }
            break;
        }
        case LuaValueTypeTuple:
        {
            encoder -> writeObject(toTuple());
            break;
        }
        case LuaValueTypeObject:
        {
            encoder -> writeObject(toObject());
            break;
        }
        case LuaValueTypeBoolean:
        {
            encoder -> writeByte(toBoolean());
            break;
        }
        case LuaValueTypeFunction:
        {
            encoder -> writeObject(toFunction());
            break;
        }
        case LuaValueTypePtr:
        {
            encoder -> writeObject(toPointer());
            break;
        }
        case LuaValueTypeClass:
        {
            LuaExportTypeDescriptor *typeDescriptor = toType();
            if (typeDescriptor != NULL)
            {
                encoder -> writeString(typeDescriptor -> typeName());
            }
            else
            {
                encoder -> writeString("");
            }
            break;
        }
        default:
            break;
    }
}

void LuaValue::managedObject(LuaContext *context)
{
    _context = context;
    if (!_hasManagedObject)
    {
        _hasManagedObject = true;
        _context -> getDataExchanger() -> retainLuaObject(this);
    }
}

std::string LuaValue::tableId()
{
    return _tableId;
}

void LuaValue::setObject(std::string keyPath, LuaValue *object, LuaContext *context)
{
    if (getType() == LuaValueTypeMap)
    {
        std::deque<std::string> keys = StringUtils::split(keyPath, ".", false);
        setObject(toMap(), keys, 0, object);

        if (context != NULL)
        {
            context -> getOperationQueue() -> performAction([=](){

                lua_State *state = context -> getCurrentSession() -> getState();
                context -> getDataExchanger() -> getLuaObject(this);

                if (LuaEngineAdapter::type(state, -1) == LUA_TTABLE)
                {
                    //先寻找对应的table对象
                    bool hasExists = true;
                    if (keys.size() > 1)
                    {
                        for (int i = 0; i < keys.size() - 1; i++)
                        {
                            std::string key = keys[i];
                            LuaEngineAdapter::pushString(state, key.c_str());
                            LuaEngineAdapter::rawGet(state, -2);

                            if (LuaEngineAdapter::type(state, -1) == LUA_TTABLE)
                            {
                                //移除前一个table对象
                                LuaEngineAdapter::remove(state, -2);
                            }
                            else
                            {
                                hasExists = false;
                                LuaEngineAdapter::pop(state, 1);
                                break;
                            }
                        }
                    }

                    if (hasExists)
                    {
                        std::string key = keys[keys.size() - 1];
                        LuaEngineAdapter::pushString(state, key.c_str());
                        context -> getDataExchanger() -> pushStack(object);
                        LuaEngineAdapter::rawSet(state, -3);
                    }
                }

                LuaEngineAdapter::pop(state, 1);

            });
        }
    }
}

void LuaValue::setObject(LuaValueMap *map,
                         std::deque<std::string> keys,
                         int keyIndex,
                         LuaValue *object)
{
    if (keyIndex < keys.size())
    {
        std::string key = keys[keyIndex];
        if (keys.size() == keyIndex + 1)
        {
            //最后一个元素
            if (object != NULL)
            {
                object -> retain();
                (*map)[key] = object;
            }
            else
            {
                map -> erase(key);
            }
        }
        else
        {
            LuaValue *value = (*map)[key];
            if (value != NULL && value -> getType() == LuaValueTypeMap)
            {
                LuaValueMap *subMap = value -> toMap();
                setObject(subMap, keys, keyIndex + 1, object);
            }
        }
    }
}
