package xyz.imxqd.luaframework.core.method;

import cn.vimfung.luascriptcore.LuaMethodHandler;

public abstract class LuaMethod implements LuaMethodHandler {
    private String name;

    public LuaMethod(String name) {
        this.name = name;
    }
}
