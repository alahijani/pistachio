package github.alahijani.pistachio;

import java.lang.reflect.*;
import java.util.logging.Logger;

/**
 * @author Ali Lahijani
 */
class CaseClassImpl {

    private static Logger logger = Logger.getLogger(CaseClassImpl.class.getName());

    private CaseClassImpl() {
    }

    public static <CC extends MutableCaseClass<CC>>
    CaseClass.Visitor<CC> assign(final CC instance) {
        return assign(instance, getAssignType(instance.getDeclaringClass()));
    }

    @SuppressWarnings("unchecked")
    private static <CC extends MutableCaseClass<CC>>
    Class<? extends CaseClass.Visitor<CC>>
    getAssignType(Class<CC> caseClass) {
        try {

            // todo this is reflection per instance, should be once per class

            Class<?> returnType = caseClass.getMethod("assign").getReturnType();
            return (Class) returnType.asSubclass(CaseClass.Visitor.class);

        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <CC extends CaseClass<CC>>
    Class<? extends CaseClass.Visitor<?>>
    getAcceptorType(Class<CC> caseClass) {
        try {

            // todo this is reflection per instance, should be once per class

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

    private static <CC extends MutableCaseClass<CC>, V extends CaseClass.Visitor<CC>>
    V assign(final CC instance, Class<V> visitorClass) {

        EtaInvocationHandler<CC, V> handler = new EtaInvocationHandler<CC, V>(visitorClass) {
            @Override
            protected CC handle(V proxy, Method method, Object[] args) throws Throwable {
                instance.assign0(null, method, args);
                return instance;
            }
        };

        return newVisitor(visitorClass, handler);
    }

    private static <CC extends MutableCaseClass<CC>, V extends CaseClass.Visitor<CC>>
    V newVisitor(Class<V> visitorClass, EtaInvocationHandler<CC, V> handler) {
        try {
            return visitorConstructor(visitorClass).newInstance(handler);
        } catch (IllegalAccessException |
                InstantiationException |
                InvocationTargetException e) {
            // this cannot happen, unless as an internal error of the VM
            throw new InternalError(e.toString(), e);
        }
    }

    private static <V extends CaseClass.Visitor>
    Constructor<V> visitorConstructor(Class<V> visitorClass) {
        return null;                                          // todo
    }

}
