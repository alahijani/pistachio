package github.alahijani.pistachio;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Ali Lahijani
 */
public abstract class CaseClass<CC extends CaseClass<CC>> {

    /**
     * This method is package-private.
     */
    @SuppressWarnings("unchecked")
    final CC thisCase() {
        return (CC) this;
    }

    @SuppressWarnings("unchecked")
    public final Class<CC> getDeclaringClass() {
        return (Class<CC>) getClass();
    }

    /**
     * The factory used for creating this instance
     */
    private SelfVisitorFactory<CC, ?> factory;

    /**
     * A method declared in a subclass of {@link CaseVisitor} that has been used to create this instance
     */
    private Method constructor;

    /**
     * The arguments passed when {@link #constructor} was called.
     */
    private Object[] arguments;

    public final class Acceptor<V extends CaseVisitor<R>, R> {
        public final R accept(V visitor) {
            return CaseClass.this.accept0(visitor);
        }
    }

    private Acceptor<?, ?> acceptor = new Acceptor<>();

    @SuppressWarnings("unchecked")
    public <R>
    Acceptor<? extends CaseVisitor<R>, R> acceptor() {
        return (Acceptor<CaseVisitor<R>, R>) acceptor;
    }

    /**
     * This method is package-private.
     */
    final void assign0(SelfVisitorFactory<CC, ?> factory, Method constructor, Object... arguments) {
        assert this.factory == null || this.factory == factory;

        this.factory = factory;
        this.constructor = constructor;
        this.arguments = arguments;
    }

    /**
     * This method is package-private.
     */
    final void assign0(CC lvalue) {
        CaseClass<CC> that = lvalue;
        assign0(that.factory, that.constructor, that.arguments);
    }

    /**
     * This method is package-private.
     * <p/>
     * This method throws an exception at runtime if {@code visitor} cannot handle the
     * {@link #constructor} of {@code value}. That is, if the method represented by {@code value.eta}
     * does not have a body in {@code visitor}. That's why this method is not public.
     */
    @SuppressWarnings("unchecked")
    private <R>
    R accept0(CaseVisitor<R> visitor) {
        try {
            return (R) this.constructor.invoke(visitor, this.arguments);
        } catch (IllegalAccessException e) {
            throw handle(e);
        } catch (InvocationTargetException e) {
            throw handle(e.getCause());
        }
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

}
