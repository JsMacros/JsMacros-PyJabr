package xyz.wagyourtail.jsmacros.pyjabr.library.impl;

import io.github.gaming32.pyjabr.object.PythonObject;
import xyz.wagyourtail.jsmacros.core.Core;
import xyz.wagyourtail.jsmacros.core.MethodWrapper;
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage;
import xyz.wagyourtail.jsmacros.core.library.IFWrapper;
import xyz.wagyourtail.jsmacros.core.library.Library;
import xyz.wagyourtail.jsmacros.core.library.PerExecLanguageLibrary;
import xyz.wagyourtail.jsmacros.pyjabr.language.impl.PyJabrContext;
import xyz.wagyourtail.jsmacros.pyjabr.language.impl.PyJabrLanguageDefinition;

import java.util.Arrays;

@Library(value = "JavaWrapper", languages = PyJabrLanguageDefinition.class)
public class FWrapper extends PerExecLanguageLibrary<Object, PyJabrContext> implements IFWrapper<PythonObject> {

    public FWrapper(PyJabrContext context, Class<? extends BaseLanguage<Object, PyJabrContext>> language) {
        super(context, language);
    }

    @Override
    public <A, B, R> MethodWrapper<A, B, R, ?> methodToJava(PythonObject pythonObject) {
        return new PyJabrMethodWrapper<>(ctx, pythonObject, true);
    }

    @Override
    public <A, B, R> MethodWrapper<A, B, R, ?> methodToJavaAsync(PythonObject pythonObject) {
        return new PyJabrMethodWrapper<>(ctx, pythonObject, false);
    }

    @Override
    public void stop() {
        ctx.closeContext();
    }

    private static class PyJabrMethodWrapper<A, B, R> extends MethodWrapper<A, B, R, PyJabrContext> {
        private final PythonObject pythonObject;
        private final boolean await;

        public PyJabrMethodWrapper(PyJabrContext ctx, PythonObject pythonObject1, boolean await) {
            super(ctx);
            this.pythonObject = pythonObject1;
            this.await = await;
        }

        private void internalAccept(boolean await, PythonObject... params) {
            if (await) {
                internalApply(params);
                return;
            }

            Thread t = new Thread(() -> {
                ctx.bindThread(Thread.currentThread());
                try {
                    pythonObject.call(params);
                } catch (Throwable e) {
                    Core.getInstance().profile.logError(e);
                } finally {
                    ctx.releaseBoundEventIfPresent(Thread.currentThread());
                    ctx.unbindThread(Thread.currentThread());

                    Core.getInstance().profile.joinedThreadStack.remove(Thread.currentThread());
                }
            });
            t.start();
        }

        private PythonObject internalApply(PythonObject... params) {
            if (ctx.getBoundThreads().contains(Thread.currentThread())) {
                return pythonObject.call(params);
            }

            try {
                ctx.bindThread(Thread.currentThread());
                return pythonObject.call(params);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                ctx.releaseBoundEventIfPresent(Thread.currentThread());
                ctx.unbindThread(Thread.currentThread());

                Core.getInstance().profile.joinedThreadStack.remove(Thread.currentThread());
            }
        }

        @Override
        public void accept(A a) {
            internalAccept(await, PythonObject.fromJavaObject(a));
        }

        @Override
        public void accept(A a, B b) {
            internalAccept(await, PythonObject.fromJavaObject(a), PythonObject.fromJavaObject(b));
        }

        @Override
        public R apply(A a) {
            return (R) internalApply(PythonObject.fromJavaObject(a)).asJavaObject();
        }

        @Override
        public R apply(A a, B b) {
            return (R) internalApply(PythonObject.fromJavaObject(a), PythonObject.fromJavaObject(b)).asJavaObject();
        }

        @Override
        public boolean test(A a) {
            return (boolean) internalApply(PythonObject.fromJavaObject(a)).asJavaObject();
        }

        @Override
        public boolean test(A a, B b) {
            return (boolean) internalApply(PythonObject.fromJavaObject(a), PythonObject.fromJavaObject(b)).asJavaObject();
        }

        @Override
        public void run() {
            internalAccept(await);
        }

        @Override
        public int compare(A a, A t1) {
            return (int) internalApply(PythonObject.fromJavaObject(a), PythonObject.fromJavaObject(t1)).asJavaObject();
        }

        @Override
        public R get() {
            return (R) internalApply().asJavaObject();
        }
    }

}
