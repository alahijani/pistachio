package github.alahijani.pistachio;

import java.lang.reflect.*;
import java.util.logging.Logger;

/**
 * @author Ali Lahijani
 */
class CaseClassImpl<CC extends CaseClass<CC>> {

    private static Logger logger = Logger.getLogger(CaseClassImpl.class.getName());

    private static ClassValue<CaseClassImpl> implCache = new ClassValue<CaseClassImpl>() {
        @Override
        protected CaseClassImpl computeValue(Class<?> type) {
            if (!CaseClass.class.isAssignableFrom(type))
                return null;

            return new CaseClassImpl<>(type.asSubclass(CaseClass.class));
        }
    };

    @SuppressWarnings("unchecked")
    public static <CC extends CaseClass<CC>>
    CaseClassImpl<CC> get(Class<CC> caseClass) {
        return implCache.get(caseClass);
    }

    private final Class<? extends CaseClass.Visitor<?>> visitorClass;
    private Constructor<? extends CaseClass.Visitor<?>> visitorConstructor;

    @SuppressWarnings("unchecked")
    private <R, V extends CaseClass.Visitor<R>>
    Class<V> visitorClass() {
        return (Class<V>) visitorClass;
    }

    @SuppressWarnings("unchecked")
    private <R, V extends CaseClass.Visitor<R>>
    Constructor<V> visitorConstructor() {
        return (Constructor<V>) visitorConstructor;
    }

    private CaseClassImpl(Class<CC> caseClass) {
        this.visitorClass = getAcceptorType(caseClass);
        this.visitorConstructor = visitorConstructor(visitorClass);
    }

/*
    public <R, V extends CaseClass.Visitor<R>>
    V uniformVisitor() {
        return null;
    }
*/
    @SuppressWarnings("unchecked")
    private static <CC extends CaseClass<CC>>
    Class<? extends CaseClass.Visitor<?>>
    getAcceptorType(Class<CC> caseClass) {
        try {

            Method acceptor = caseClass.getMethod("acceptor");

            Type returnType = acceptor.getGenericReturnType();
            if (returnType instanceof Class<?>) {
                logger.severe("Raw return type found for method " + acceptor);

                assert returnType == CaseClass.Acceptor.class;
                return (Class) CaseClass.Visitor.class;
            } else if (returnType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) returnType;

                assert parameterizedType.getRawType() == CaseClass.Acceptor.class;
                Type visitorType = parameterizedType.getActualTypeArguments()[0];

                return (Class) getRawType(visitorType).asSubclass(CaseClass.Visitor.class);
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

    public <V extends CaseClass.Visitor<CC>>
    V assign(final CC instance) {

        VisitorInvocationHandler<CC, V> handler = new VisitorInvocationHandler<CC, V>(this.<CC, V>visitorClass()) {
            @Override
            protected CC handle(V proxy, Method method, Object[] args) throws Throwable {
                instance.assign0(null, method, args);
                return instance;
            }
        };

        return handler.newVisitor(this.<CC, V>visitorConstructor());
    }

    private static <V extends CaseClass.Visitor<?>>
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
