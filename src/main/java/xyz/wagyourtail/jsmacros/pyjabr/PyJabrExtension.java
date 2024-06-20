package xyz.wagyourtail.jsmacros.pyjabr;

import io.github.gaming32.pyjabr.PythonSystem;
import io.github.gaming32.pyjabr.object.PythonException;
import io.github.gaming32.pyjabr.object.PythonObject;
import io.github.gaming32.pyjabr.object.PythonObjects;
import io.github.gaming32.pyjabr.run.PythonExec;
import xyz.wagyourtail.jsmacros.core.Core;
import xyz.wagyourtail.jsmacros.core.extensions.Extension;
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage;
import xyz.wagyourtail.jsmacros.core.language.BaseWrappedException;
import xyz.wagyourtail.jsmacros.core.library.BaseLibrary;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class PyJabrExtension implements Extension {

    @Override
    public void init() {
        PythonSystem.initialize();
        PythonExec.execString("print('Hello PyJabr!', flush = True)", "null");
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public String getLanguageImplName() {
        return "pyjabr";
    }

    @Override
    public ExtMatch extensionMatch(File file) {
        if (file.getName().endsWith(".py")) {
            if (file.getName().contains(getLanguageImplName())) {
                return ExtMatch.MATCH_WITH_NAME;
            } else {
                return ExtMatch.MATCH;
            }
        }
        return ExtMatch.NOT_MATCH;
    }

    @Override
    public String defaultFileExtension() {
        return "py";
    }

    @Override
    public BaseLanguage<?, ?> getLanguage(Core<?, ?> core) {
        return null;
    }

    @Override
    public Set<Class<? extends BaseLibrary>> getLibraries() {
        return Set.of();
    }

    @Override
    public BaseWrappedException<?> wrapException(Throwable throwable) {
        if (throwable instanceof PythonException) {
            Throwable cause = throwable.getCause();
            String message;
            if (cause != null) {
                message = cause.getClass().getName();
                String intMessage = cause.getMessage();
                if (intMessage != null) {
                    message += ": " + intMessage;
                }
            }
            else {
                message = throwable.getMessage();
                message = message.split("'")[1] + ": " + message.split(":", 2)[1];
            }
            Iterator<StackTraceElement> elements = Arrays.stream(throwable.getStackTrace()).iterator();
            return new BaseWrappedException<>(throwable, message, null, elements.hasNext() ? wrapStackTrace(elements.next(), elements) : null);
        }
        return null;
    }

    private BaseWrappedException<?> wrapStackTrace(StackTraceElement current, Iterator<StackTraceElement> elements) {
        if (current.isNativeMethod()) return null;
        String fileName = current.getFileName();
        if (fileName == null || fileName.endsWith(".java")) {
            return BaseWrappedException.wrapHostElement(current, elements.hasNext() ? wrapStackTrace(elements.next(), elements) : null);
        }
        File folder = new File(current.getClassName()).getParentFile();
        BaseWrappedException.SourceLocation loc = new BaseWrappedException.GuestLocation(new File(folder, fileName), -1, -1, current.getLineNumber(), -1);
        String message = current.getMethodName();
        return new BaseWrappedException<>(current, " at " + message, loc, elements.hasNext() ? wrapStackTrace(elements.next(), elements) : null);
    }

    @Override
    public boolean isGuestObject(Object o) {
        return o instanceof PythonObject;
    }
}
