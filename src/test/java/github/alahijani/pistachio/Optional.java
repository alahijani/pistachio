package github.alahijani.pistachio;

/**
 * @author Ali Lahijani
 */
public final class Optional<T> extends CaseClass<Optional<T>> {

    private Optional() {
    }

    @Override
    public <R> Acceptor<Visitor<T, R>, R> acceptor() {
        CaseClassFactory.CaseVisitorFactory<R, Visitor<T, R>>
                factory = classFactory.caseVisitorFactory();

        Acceptor<?, R> acceptor = super.acceptor();
        return factory.cast(acceptor);
    }

    public <R> R accept(Visitor<T, R> visitor) {
        return this.<R>acceptor().accept(visitor);
    }

    public T orElse(final T ifNone) {
        return accept(new Visitor<T, T>() {
            @Override public T none() {
                return ifNone;
            }
            @Override public T some(T t) {
                return t;
            }
        });
    }

    /**
     *
     */
    public interface Visitor<T, R> extends CaseVisitor<R> {
        R none();

        R some(T t);
    }

    private static final CaseClassFactory<Optional<Object>> classFactory = new Optional<>().getFactory();

    @SuppressWarnings("unchecked")
    private static <T>
    CaseClassFactory<Optional<T>> classFactory() {
        return (CaseClassFactory) classFactory;
    }

    public static <T> Visitor<T, Optional<T>> values() {
        CaseClassFactory<Optional<T>>.SelfVisitorFactory<Visitor<T, Optional<T>>>
                factory = Optional.<T>classFactory().selfVisitorFactory();

        return factory.selfVisitor();
    }

    public static void main(String[] args) {

        Visitor<Integer, Void> visitor = new Visitor<Integer, Void>() {
            @Override
            public Void none() {
                System.out.println("Nothing!");
                return null;
            }

            @Override
            public Void some(Integer integer) {
                System.out.println("integer = " + integer);
                return null;
            }
        };


        Optional<Integer> optional = Optional.<Integer>values().some(1);
        optional.accept(visitor);

        optional = Optional.<Integer>values().none();
        optional.accept(visitor);

    }

}
