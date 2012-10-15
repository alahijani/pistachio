package github.alahijani.pistachio;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * @author Ali Lahijani
 */
public abstract class CaseClassFactory<CC extends CaseClass<CC>, V extends CaseClass.Visitor<CC, CC>> {
    private Class<V> visitorClass;
    private Constructor<? extends V> visitorConstructor;
    private V eta;

    private final V postProcessor;

    public CaseClassFactory(Class<CC> caseClass, Class<V> visitorClass) {
        this(caseClass, visitorClass, null);
    }

    public CaseClassFactory(Class<CC> caseClass, Class<V> visitorClass, V postProcessor) {
        this.postProcessor = postProcessor;
        init(caseClass, visitorClass);
    }

    public CaseClassFactory() {
        this(null);
    }
    public CaseClassFactory(V postProcessor) {
        this.postProcessor = postProcessor;
        TypeToken<CC> caseClassToken = new TypeToken<CC>(getClass()) {};
        TypeToken<V> visitorClassToken = new TypeToken<V>(getClass()) {};

        @SuppressWarnings("unchecked") Class<CC> caseClass = (Class<CC>) caseClassToken.getRawType();
        @SuppressWarnings("unchecked") Class<V> visitorClass = (Class<V>) visitorClassToken.getRawType();

        init(caseClass, visitorClass);
    }

    private void init(Class<CC> caseClass, final Class<V> visitorClass) {
        this.visitorClass = visitorClass;
        final Constructor<CC> privateConstructor;

        try {
            privateConstructor = caseClass.getDeclaredConstructor();
            privateConstructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            String message = "Case class " + caseClass.getName() +
                    " should declare a private no-args constructor";
            throw new IllegalStateException(message, e);
        }

        try {
            /**
             * Look up or generate the designated proxy class.
             */
            Class<? extends V> proxyClass =
                    Proxy.getProxyClass(visitorClass.getClassLoader(), visitorClass).asSubclass(visitorClass);

            visitorConstructor = proxyClass.getConstructor(InvocationHandler.class);

            EtaInvocationHandler<CC, V> handler = new EtaInvocationHandler<CC, V>(this.visitorClass) {
                @Override
                protected CC handle(V proxy, Method method, Object[] args) throws Throwable {
                    try {
                        CC instance = privateConstructor.newInstance();
                        instance.assign0(CaseClassFactory.this, method, args);
                        return instance;
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                }
            };

            eta = newVisitor(handler);

        } catch (NoSuchMethodException e) {
            // this cannot happen, unless as an internal error of the VM
            throw new InternalError(e.toString(), e);
        }
    }

    private V newVisitor(EtaInvocationHandler<CC, V> handler) {
        if (postProcessor != null) {
            final EtaInvocationHandler<CC, V> original = handler;
            handler = new EtaInvocationHandler<CC, V>(this.visitorClass) {
                @Override
                protected CC handle(V proxy, Method method, Object[] args) throws Throwable {
                    return CaseClass.apply(postProcessor, original.handle(proxy, method, args));
                }
            };
        }

        try {
            return visitorConstructor.newInstance(handler);
        } catch (IllegalAccessException |
                InstantiationException |
                InvocationTargetException e) {
            // this cannot happen, unless as an internal error of the VM
            throw new InternalError(e.toString(), e);
        }
    }

    public final V eta() {
        return eta;
    }

    /**
     * @param e Is thrown, wrapped in a RuntimeException if necessary
     * @return Never, hence the type
     */
    static AssertionError handle(Throwable e) {
        if (e instanceof Error)
            throw (Error) e;

        if (e instanceof RuntimeException)
            throw (RuntimeException) e;

        throw new RuntimeException(e);
    }

    static Cache<Class<? extends CaseClass>, CaseClassFactory> factoryCache = CacheBuilder.newBuilder()
            .weakKeys()
            .build();

    @SuppressWarnings("unchecked")
    static <CC extends CaseClass<CC>, V extends CaseClass.Visitor<CC, CC>>
    CaseClassFactory<CC, V> get(final Class<CC> caseClass, final Class<V> visitorClass) {
        Callable<CaseClassFactory<CC, V>> loader = new Callable<CaseClassFactory<CC, V>>() {
            @Override
            public CaseClassFactory<CC, V> call() {
                return new CaseClassFactory<CC, V>(caseClass, visitorClass) {
                };
            }
        };

        try {
            return factoryCache.get(caseClass, loader);
        } catch (ExecutionException e) {
            try {
                return loader.call();
            } catch (Exception silly) {
                throw handle(silly);
            }
        }
    }

}