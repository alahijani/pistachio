package github.alahijani.pistachio;

import java.lang.reflect.*;

/**
 * @author Ali Lahijani
 */
abstract class VisitorInvocationHandler<R, V extends CaseClass.Visitor<R>>
        implements InvocationHandler {

    private final Class<V> visitorClass;

    public VisitorInvocationHandler(Class<V> visitorClass) {
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
