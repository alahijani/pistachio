package github.alahijani.pistachio;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
* @author Ali Lahijani
*/
public class SelfVisitorFactory<CC extends CaseClass<CC>, V extends CaseClass.Visitor<CC>>
        extends CaseVisitorFactory<CC, V> {

    private final V selfVisitor;

    SelfVisitorFactory(Class<V> visitorClass, Class<CC> caseClass) {
        super(visitorClass);

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
                    instance.assign0(null, method, args);
                    return instance;
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        };

        selfVisitor = handler.newVisitor(visitorConstructor);
    }

    public V assign(final CC instance) {

        VisitorInvocationHandler<CC, V> handler = new VisitorInvocationHandler<CC, V>(visitorClass) {
            @Override
            protected CC handle(V proxy, Method method, Object[] args) throws Throwable {
                instance.assign0(null, method, args);
                return instance;
            }
        };

        return handler.newVisitor(visitorConstructor);
    }

    public V selfVisitor() {
        return selfVisitor;
    }
}
