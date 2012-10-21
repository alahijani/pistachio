package github.alahijani.pistachio;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

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
    private CaseClassFactory<CC> factory;

    /**
     * A method declared in a subclass of {@link CaseVisitor} that has been used to create this instance
     */
    private Method constructor;

    /**
     * The arguments passed when {@link #constructor} was called.
     */
    private Object[] arguments;

    public CaseClassFactory<CC> getFactory() {
        if (factory == null) {
            setFactory(CaseClassFactory.get(getDeclaringClass()));
        }

        return factory;
    }

    public final class Acceptor<V extends CaseVisitor<R>, R> {
        /**
         * todo visitorClass = constructor.getDeclaringClass()
         */
        public Class<V> visitorClass;

        /**
         *
         */
        private Acceptor() {
            visitorClass = null;
        }

        private Acceptor(Class<V> visitorClass) {
            this.visitorClass = visitorClass;
        }

        public R accept(V visitor) {
            return CaseClass.this.accept0(visitor);
        }

        @SuppressWarnings("unchecked")
        public <W extends CaseVisitor<R>>
        Acceptor<W, R> cast(Class<W> visitorClass) {
            visitorClass.asSubclass(this.visitorClass);
            return (Acceptor<W, R>) this;
        }

    }

    private Acceptor<?, ?> acceptor = new Acceptor<>();

    /**
     * The covariant return type of this method in a subclass of <code>CaseClass</code> defines the type of
     * visitors that the case class wills to accept.
     * <p/>
     * A concrete case class should override this method and cast the result to {@code Acceptor<Visitor<R>, R>} where
     * <code>Visitor</code> is the actual sub-interface of {@link CaseVisitor} that it wills to accept.
     */
    @SuppressWarnings("unchecked")
    public <R>
    Acceptor<?, R> acceptor() {
        return (Acceptor<?, R>) acceptor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CaseClass)) return false;

        CaseClass that = (CaseClass) o;

        return Arrays.equals(this.arguments, that.arguments) &&
                (this.constructor == null ? that.constructor == null : constructor.equals(that.constructor));
    }

    @Override
    public int hashCode() {
        int result = constructor != null ? constructor.hashCode() : 0;
        result = 31 * result + (arguments != null ? Arrays.hashCode(arguments) : 0);
        return result;
    }

    /**
     * This method is package-private.
     */
    final void assign0(CaseClassFactory<CC> factory, Method constructor, Object... arguments) {
        setFactory(factory);
        this.constructor = constructor;
        this.arguments = arguments;
    }

    @SuppressWarnings("unchecked")
    private void setFactory(CaseClassFactory<CC> factory) {
        assert this.factory == null || this.factory == factory;

        this.factory = factory;
        this.acceptor.visitorClass = (Class) factory.visitorClass();
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
        /**
         * Validate visitor
         */
        Objects.requireNonNull(visitor, "null visitor");
        this.constructor.getDeclaringClass().cast(visitor);

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
