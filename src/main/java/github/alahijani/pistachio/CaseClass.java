package github.alahijani.pistachio;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * A direct subclass of <code>CaseClass</code> is called a (concrete) case class. A case class should only
 * have private constructors.
 * <p/>
 * To every case class is associated a visitor interface, which should extend {@link CaseVisitor}. It is
 * imperative that a case class overrides the {@link #acceptor() acceptor()} method declared here. The
 * return type of the overridden method determines the visitor interface associated with a concrete case
 * class.
 * <p/>
 * A concrete case class must declare a private no-args constructor.
 *
 * @author Ali Lahijani
 * @see CaseVisitor
 * @see #acceptor()
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
     * todo possibly replace by constructor.getDeclaringClass()
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

    public CaseClassFactory<CC> factory() {
        if (factory == null) {
            setFactory(CaseClassFactory.get(getDeclaringClass()));
        }

        return factory;
    }

    /**
     * @return A method declared in a subclass of {@link CaseVisitor} that has been used to create this instance, or
     *         has been later {@link CaseReference#set() assigned} to it.
     */
    public Method constructor() {
        return constructor;
    }

    /**
     * @return The actual arguments passed when this instance was created or last
     *         {@link CaseReference#set() assigned} to.
     */
    public Object[] arguments() {
        return arguments.clone();
    }

    public String name() {
        return constructor.getName();
    }

    public Class<?>[] parameterTypes() {
        return constructor.getParameterTypes();
    }

    public static class Acceptor<V extends CaseVisitor<R>, R> {
        /**
         * todo visitorClass = constructor.getDeclaringClass()
         */
        public Class<V> visitorClass;
        private CaseClass<?> thisCase;

        /**
         *
         */
        private Acceptor(CaseClass<?> thisCase) {
            this.visitorClass = null;
            this.thisCase = thisCase;
        }

        private Acceptor(CaseClass<?> thisCase, Class<V> visitorClass) {
            this.visitorClass = visitorClass;
            this.thisCase = thisCase;
        }

        public R accept(V visitor) {
            return thisCase.accept0(visitor);
        }

        @SuppressWarnings("unchecked")
        public <W extends CaseVisitor<R>>
        Acceptor<W, R> cast(Class<W> visitorClass) {
            visitorClass.asSubclass(this.visitorClass);
            return cast();
        }

        @SuppressWarnings("unchecked")
        public <W extends CaseVisitor<R>>
        Acceptor<W, R> cast() {
            return (Acceptor<W, R>) this;
        }

        public CaseClass<?> thisCase() {
            return thisCase;
        }

    }

    private Acceptor<?, ?> acceptor = new Acceptor<>(this);

    /**
     * The covariant return type of this method in a subclass of <code>CaseClass</code> defines the type of
     * visitors that the case class wills to accept.
     * <p/>
     * A concrete case class for a hypothetical visitor type <code>Visitor</code> should override this method
     * with a method with return type {@code Acceptor<? super Visitor<R>, R>}. Note that the wildcard <code>?</code>
     * here is also bound from above by {@code CaseVisitor<R>}, by virtue of being the first type parameter to
     * {@code Acceptor}.
     * <p/>
     * The overriding method should return the result of super-call, {@link Acceptor#cast(Class) cast} to the
     * expected type with <code>Visitor.class</code>. That is,
     * <pre>{@code
     * public @Override <R>
     * Acceptor<? super Visitor<R>, R> acceptor() {
     *     return super.<R>acceptor().cast(Visitor.class);
     * }
     * }</pre>
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

    @Override
    public String toString() {
        if (arguments == null) {
            return constructor.getName();
        } else {
            return constructor.getName() + Arrays.toString(arguments);
        }
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
        assert this.factory == null || this.factory == factory; // true, even in a race condition

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
