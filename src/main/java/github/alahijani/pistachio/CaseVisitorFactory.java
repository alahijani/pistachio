package github.alahijani.pistachio;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author Ali Lahijani
 */
public class CaseVisitorFactory<R, V extends CaseClass.Visitor<R>> {

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
