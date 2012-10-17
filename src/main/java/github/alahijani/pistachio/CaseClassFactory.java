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
    private final SelfVisitorFactory<?, ?> selfVisitorFactory;

    private <V extends CaseClass.Visitor<CC>>
    CaseClassFactory(Class<CC> caseClass) {
        Class<V> visitorClass = CaseClassFactory.<CC, CC, V>getAcceptorType(caseClass);

        caseVisitorFactory = new CaseVisitorFactory<>(visitorClass);
        selfVisitorFactory = new SelfVisitorFactory<>(visitorClass, caseClass);
    }

    @SuppressWarnings("unchecked")
    public <R, V extends CaseClass.Visitor<R>>
    CaseVisitorFactory<R, V> caseVisitorFactory() {
        return (CaseVisitorFactory<R, V>) caseVisitorFactory;
    }

    @SuppressWarnings("unchecked")
    public <V extends CaseClass.Visitor<CC>>
    SelfVisitorFactory<CC, V> selfVisitorFactory() {
        return (SelfVisitorFactory<CC, V>) selfVisitorFactory;
    }

    @SuppressWarnings("unchecked")
    private static <CC extends CaseClass<CC>, R, V extends CaseClass.Visitor<R>>
    Class<V> getAcceptorType(Class<CC> caseClass) {
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

}
