package xyz.wagyourtail.jsmacros.pyjabr.language.impl;

import io.github.gaming32.pyjabr.object.PythonObject;
import io.github.gaming32.pyjabr.object.PythonObjects;
import io.github.gaming32.pyjabr.run.PythonExec;
import xyz.wagyourtail.jsmacros.core.Core;
import xyz.wagyourtail.jsmacros.core.config.ScriptTrigger;
import xyz.wagyourtail.jsmacros.core.event.BaseEvent;
import xyz.wagyourtail.jsmacros.core.extensions.Extension;
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage;
import xyz.wagyourtail.jsmacros.core.language.EventContainer;
import xyz.wagyourtail.jsmacros.core.library.BaseLibrary;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

public class PyJabrLanguageDefinition extends BaseLanguage<Object, PyJabrContext> {

    public PyJabrLanguageDefinition(Extension extension, Core<?, ?> runner) {
        super(extension, runner);
    }

    public PythonObject buildGlobals(EventContainer<PyJabrContext> ctx, BaseEvent event) {
        PythonObject globals = PythonObjects.stringDict(Map.of("__builtins__", PythonObjects.getBuiltins()));
        for (Map.Entry<String, BaseLibrary> entry : retrieveLibs(ctx.getCtx()).entrySet()) {
            globals.setItem(PythonObjects.str(entry.getKey()), PythonObject.fromJavaObject(entry.getValue()));
        }
        globals.setItem(PythonObjects.str("event"), PythonObject.fromJavaObject(event));
        globals.setItem(PythonObjects.str("ctx"), PythonObject.fromJavaObject(ctx));
        globals.setItem(PythonObjects.str("file"), PythonObject.fromJavaObject(ctx.getCtx().getFile()));
        return globals;
    }

    @Override
    protected void exec(EventContainer<PyJabrContext> eventContainer, ScriptTrigger scriptTrigger, BaseEvent baseEvent) throws Exception {
        PythonExec.execCode(Files.readAllBytes(eventContainer.getCtx().getFile().toPath()), eventContainer.getCtx().getFile().getCanonicalPath(), buildGlobals(eventContainer, baseEvent));
    }

    @Override
    protected void exec(EventContainer<PyJabrContext> eventContainer, String lang, String script, BaseEvent baseEvent) throws Exception {
        File f = eventContainer.getCtx().getFile();
        if (f != null) {
            f = f.getCanonicalFile();
        }
        PythonExec.execCode(script.getBytes(StandardCharsets.UTF_8), String.valueOf(f), buildGlobals(eventContainer, baseEvent));
    }

    @Override
    public PyJabrContext createContext(BaseEvent baseEvent, File file) {
        return new PyJabrContext(baseEvent, file);
    }
}
