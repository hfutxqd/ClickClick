package xyz.imxqd.luaframework.core.method;

import cn.vimfung.luascriptcore.LuaContext;
import cn.vimfung.luascriptcore.LuaMethodHandler;

public abstract class LuaMethod implements LuaMethodHandler {
    private String name;
    LuaContext luaContext;

    public LuaMethod(String name) {
        this.name = name;
    }

    public void setLuaContext(LuaContext luaContext) {
        this.luaContext = luaContext;
    }
}
