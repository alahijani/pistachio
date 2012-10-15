package github.alahijani.pistachio;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Ali Lahijani
 */
abstract class EtaInvocationHandler<CC extends CaseClass<CC>, V extends CaseClass.Visitor<CC>>
        implements InvocationHandler {

    private final Class<V> visitorClass;

    public EtaInvocationHandler(Class<V> visitorClass) {
        this.visitorClass = visitorClass;
    }

    @Override
    public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class)
            handleObjectMethod(this, proxy, method, args);

        if (method.getDeclaringClass() == CaseClass.Visitor.class)
            return handleCommonMethod(proxy, method, args);

        /**
         * throw early if visitorClass cannot handle method
         */
        visitorClass.asSubclass(method.getDeclaringClass());

        return handle(visitorClass.cast(proxy), method, args);
    }

    protected abstract CC handle(V proxy, Method method, Object[] args) throws Throwable;

    private Object handleObjectMethod(InvocationHandler handler, Object proxy, Method method, Object[] args) {
        switch (method.getName()) {
            case "equals":
                assert args.length == 1;
                Object that = args[0];
                return getClass().isInstance(that) && Proxy.getInvocationHandler(that) == handler;
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

}
