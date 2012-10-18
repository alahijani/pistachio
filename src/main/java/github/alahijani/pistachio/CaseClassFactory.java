package github.alahijani.pistachio;

import java.lang.reflect.*;
import java.util.logging.Logger;

/**
 * @author Ali Lahijani
 */
public class CaseClassFactory<CC extends CaseClass<CC>> {

    private static Logger logger = Logger.getLogger(CaseClassFactory.class.getName());

    private static ClassValue<CaseClassFactory> implCache = new ClassValue<CaseClassFactory>() {
        @Override
        protected CaseClassFactory computeValue(Class<?> type) {
            if (!CaseClass.class.isAssignableFrom(type))
                return null;

            return new CaseClassFactory<>(type.asSubclass(CaseClass.class));
        }
    };

    @SuppressWarnings("unchecked")
    public static <CC extends CaseClass<CC>>
    CaseClassFactory<CC> get(Class<CC> caseClass) {
        return implCache.get(caseClass);
    }

    private final CaseVisitorFactory<?, ?> caseVisitorFactory;
    private final SelfVisitorFactory<?> selfVisitorFactory;

    private <V extends CaseVisitor<CC>>
    CaseClassFactory(Class<CC> caseClass) {
        Class<V> visitorClass = CaseClassFactory.<CC, CC, V>getAcceptorType(caseClass);

        caseVisitorFactory = new CaseVisitorFactory<>(visitorClass);
        selfVisitorFactory = new SelfVisitorFactory<>(visitorClass, caseClass);
    }

    @SuppressWarnings("unchecked")
    public <R, V extends CaseVisitor<R>>
    CaseVisitorFactory<R, V> caseVisitorFactory() {
        return (CaseVisitorFactory<R, V>) caseVisitorFactory;
    }

    @SuppressWarnings("unchecked")
    public <V extends CaseVisitor<CC>>
    SelfVisitorFactory<V> selfVisitorFactory() {
        return (SelfVisitorFactory<V>) selfVisitorFactory;
    }

    @SuppressWarnings("unchecked")
    private static <CC extends CaseClass<CC>, R, V extends CaseVisitor<R>>
    Class<V> getAcceptorType(Class<CC> caseClass) {
        try {

            Method acceptor = caseClass.getMethod("acceptor");

            Type returnType = acceptor.getGenericReturnType();
            if (returnType instanceof Class<?>) {
                logger.severe("Raw return type found for method " + acceptor);

                assert returnType == CaseClass.Acceptor.class;
                return (Class) CaseVisitor.class;
            } else if (returnType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) returnType;

                assert parameterizedType.getRawType() == CaseClass.Acceptor.class;
                Type visitorType = parameterizedType.getActualTypeArguments()[0];

                return (Class) getRawType(visitorType).asSubclass(CaseVisitor.class);
            } else {
                throw new AssertionError("Strange method signature: " + acceptor);
            }

        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    private static Class<?> getRawType(Type type) {
        while (true) {
            if (type instanceof Class<?>) {
                return (Class) type;
            } else if (type instanceof GenericArrayType) {
                Class<?> componentClass = getRawType(((GenericArrayType) type).getGenericComponentType());
                return Array.newInstance(componentClass, 0).getClass();
            } else if (type instanceof ParameterizedType) {
                type = ((ParameterizedType) type).getRawType();
            } else if (type instanceof WildcardType) {
                type = ((WildcardType) type).getUpperBounds()[0];
            } else {
                throw new AssertionError("Strange kind of java.lang.reflect.Type: " + type);
            }
        }
    }

    /**
     * @author Ali Lahijani
     */
    public static class CaseVisitorFactory<R, V extends CaseVisitor<R>> {

        protected final Class<V> visitorClass;
        protected final Constructor<? extends V> visitorConstructor;

        public CaseVisitorFactory(Class<V> visitorClass) {
            this.visitorClass = visitorClass;
            this.visitorConstructor = visitorConstructor(this.visitorClass);
        }

    /*
        public V uniformVisitor() {
            return null;
        }
    */
        private static <V extends CaseVisitor<?>>
        Constructor<? extends V> visitorConstructor(Class<V> visitorClass) {
            try {
                Class<? extends V> proxyClass =
                        Proxy.getProxyClass(visitorClass.getClassLoader(), visitorClass).asSubclass(visitorClass);
                return proxyClass.getConstructor(InvocationHandler.class);
            } catch (NoSuchMethodException e) {
                // this cannot happen, unless as an internal error of the VM
                throw new InternalError(e.toString(), e);
            }
        }

    }

    /**
    * @author Ali Lahijani
    */
    public class SelfVisitorFactory<V extends CaseVisitor<CC>>
            extends CaseVisitorFactory<CC, V> {

        private final V postProcessor;
        private final V selfVisitor;

        public SelfVisitorFactory(Class<V> visitorClass, Class<CC> caseClass) {
            this(visitorClass, caseClass, null);
        }

        public SelfVisitorFactory(Class<V> visitorClass, Class<CC> caseClass, V postProcessor) {
            super(visitorClass);
            this.postProcessor = postProcessor;

            final Constructor<CC> privateConstructor;

            try {
                privateConstructor = caseClass.getDeclaredConstructor();
                privateConstructor.setAccessible(true);
            } catch (NoSuchMethodException e) {
                String message = "Case class " + caseClass.getName() +
                        " should declare a private no-args constructor";
                throw new IllegalStateException(message, e);
            }

            VisitorInvocationHandler<CC, V> handler = new VisitorInvocationHandler<CC, V>(this.visitorClass) {
                @Override
                protected CC handle(V proxy, Method method, Object[] args) throws Throwable {
                    try {
                        CC instance = privateConstructor.newInstance();
                        instance.assign0(SelfVisitorFactory.this, method, args);
                        return instance;
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                }
            };

            handler = applyPostProcessor(handler);

            selfVisitor = handler.newVisitor(visitorConstructor);
        }

        private VisitorInvocationHandler<CC, V> applyPostProcessor(final VisitorInvocationHandler<CC, V> handler) {
            if (postProcessor == null)
                return handler;

            return new VisitorInvocationHandler<CC, V>(this.visitorClass) {
                @Override
                protected CC handle(V proxy, Method method, Object[] args) throws Throwable {
                    CC instance = handler.handle(proxy, method, args);
                    CaseVisitorFactory<CC, CaseVisitor<CC>> factory = caseVisitorFactory();

                    return instance.<CC>acceptor().cast(factory).accept(postProcessor);
                }
            };
        }

        public V assign(final CC instance) {

            VisitorInvocationHandler<CC, V> handler = new VisitorInvocationHandler<CC, V>(visitorClass) {
                @Override
                protected CC handle(V proxy, Method method, Object[] args) throws Throwable {
                    instance.assign0(SelfVisitorFactory.this, method, args);
                    return instance;
                }
            };

            handler = applyPostProcessor(handler);

            return handler.newVisitor(visitorConstructor);
        }

        public V selfVisitor() {
            return selfVisitor;
        }

    }

    /**
     * @author Ali Lahijani
     */
    abstract static class VisitorInvocationHandler<R, V extends CaseVisitor<R>>
            implements InvocationHandler {

        private final Class<V> visitorClass;

        public VisitorInvocationHandler(Class<V> visitorClass) {
            this.visitorClass = visitorClass;
        }

        @Override
        public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class)
                handleObjectMethod(this, proxy, method, args);

            if (method.getDeclaringClass() == CaseVisitor.class)
                return handleCommonMethod(proxy, method, args);

            /**
             * throw early if visitorClass cannot handle method
             */
            visitorClass.asSubclass(method.getDeclaringClass());

            return handle(visitorClass.cast(proxy), method, args);
        }

        protected abstract R handle(V proxy, Method method, Object[] args) throws Throwable;

        /**
         * Actually there is nothing Visitor-specific about this method
         */
        private static Object handleObjectMethod(InvocationHandler handler, Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "equals":
                    assert args.length == 1;
                    Object that = args[0];
                    return Proxy.isProxyClass(proxy.getClass()) && Proxy.getInvocationHandler(that) == handler;
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy));
                default:
                    throw new AssertionError();
            }
        }

        private Object handleCommonMethod(Object proxy, Method method, Object[] args) {
            // assert CaseClass.Visitor.class.getMethod("apply", CaseClass.class).equals(method);
            return null;
        }

        public V newVisitor(Constructor<? extends V> visitorConstructor) {
            try {
                return visitorConstructor.newInstance(this);
            } catch (IllegalAccessException |
                    InstantiationException |
                    InvocationTargetException e) {
                // this cannot happen, unless as an internal error of the VM
                throw new InternalError(e.toString(), e);
            }
        }

    }
}
