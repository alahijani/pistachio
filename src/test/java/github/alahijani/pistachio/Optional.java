package github.alahijani.pistachio;

/**
 * @author Ali Lahijani
 */
public final class Optional<T> extends CaseClass<Optional<T>> {

    private Optional() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> Acceptor<Visitor<T, R>, R> acceptor() {
        return (Acceptor<Visitor<T, R>, R>) super.<R>acceptor();
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

    private static final SelfVisitorFactory factory =
            CaseClassFactory.get(new Optional<>().getDeclaringClass())
                    .<Visitor<Object, Optional<Object>>>selfVisitorFactory();

    @SuppressWarnings("unchecked")
    public static <T> Visitor<T, Optional<T>> values() {
        return (Visitor<T, Optional<T>>) factory.selfVisitor();
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
