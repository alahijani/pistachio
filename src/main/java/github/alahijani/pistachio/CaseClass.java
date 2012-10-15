package github.alahijani.pistachio;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Ali Lahijani
 */
public abstract class CaseClass<CC extends CaseClass<CC>> {

    @SuppressWarnings("unchecked")
    protected final CC thisCase() {
        return (CC) this;
    }

    @SuppressWarnings("unchecked")
    public final Class<CC> getDeclaringClass() {
        return (Class<CC>) getClass();
    }

//    public @interface VisitorType {
//        Class<? extends CaseClass.Visitor> value();
//    }

    /**
     * The factory used for creating this instance
     */
    private CaseClassFactory<CC, ?> factory;

    /**
     * A method declared in a subclass of {@link Visitor} that has been used to create this instance
     */
    private Method constructor;

    /**
     * The arguments passed when {@link #constructor} was called.
     */
    private Object[] arguments;

    protected static <V extends Visitor<CC, R>, CC extends CaseClass<CC>, R>
    R apply(V visitor, CC value) {
        return value.accept0(visitor);
    }

    /**
     *
     * @param <CC> The case class that
     * @param <R>  The return type of <em>every</em> declared by an interface extending this interface
     */
    public interface Visitor<CC extends CaseClass<CC>, R> {

/*
        public R apply(CC value) default {
            return value.accept0(this);
        };
*/

    }

    public final class Acceptor<V extends Visitor<CC, R>, R> {
        public final R accept(V visitor) {
            return accept0(visitor);
        }
    }

    private Acceptor<?, ?> acceptor = new Acceptor<>();

    @SuppressWarnings("unchecked")
    public <R>
    Acceptor<? extends Visitor<CC, R>, R> acceptor() {
        return (Acceptor<? extends Visitor<CC, R>, R>) acceptor;
    }

    public interface Block<O, R> {
        public R apply(O object);

    }

    private final Block<? extends Visitor<CC, ?>, ?> dni = new Block<Visitor<CC, ?>, Object>() {
        @Override
        public Object apply(Visitor<CC, ?> visitor) {
            return accept0(visitor);
        }
    };

    @SuppressWarnings("unchecked")
    public <R>
    Block<? extends Visitor<CC, R>, R> dni() {
        return (Block<? extends Visitor<CC, R>, R>) dni;
    }

    /**
     * This method is package-private.
     */
    final void assign0(CaseClassFactory<CC, ?> factory, Method constructor, Object... arguments) {
        assert this.factory == null || this.factory == factory;

        this.factory = factory;
        this.constructor = constructor;
        this.arguments = arguments;
    }

    /**
     * This method is package-private.
     */
    final void assign0(CaseClass<CC> lvalue) {
        assign0(lvalue.factory, lvalue.constructor, lvalue.arguments);
    }

    /**
     * This method is package-private.
     * <p/>
     * This method throws an exception at runtime if {@code visitor} cannot handle the
     * {@link #constructor} of {@code value}. That is, if the method represented by {@code value.eta}
     * does not have a body in {@code visitor}. That's why this method is not public.
     */
    @SuppressWarnings("unchecked")
    final <R>
    R accept0(Visitor<CC, R> visitor) {
        try {
            return (R) this.constructor.invoke(visitor, this.arguments);
        } catch (IllegalAccessException e) {
            throw CaseClassFactory.handle(e);
        } catch (InvocationTargetException e) {
            throw CaseClassFactory.handle(e.getCause());
        }
    }

}
