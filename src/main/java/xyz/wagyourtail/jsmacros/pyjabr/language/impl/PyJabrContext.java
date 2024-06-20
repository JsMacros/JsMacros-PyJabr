package xyz.wagyourtail.jsmacros.pyjabr.language.impl;

import xyz.wagyourtail.jsmacros.core.event.BaseEvent;
import xyz.wagyourtail.jsmacros.core.language.BaseScriptContext;

import java.io.File;

public class PyJabrContext extends BaseScriptContext<Object> {

    public PyJabrContext(BaseEvent event, File file) {
        super(event, file);
    }

    @Override
    public boolean isMultiThreaded() {
        return true;
    }

}
