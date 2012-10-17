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

    /**
     *
     */
    public interface Visitor<T, R> extends CaseClass.Visitor<R> {
        R nothing();

        R something(T t);
    }

    private static final SelfVisitorFactory factory
            = CaseClassFactory.get(Optional.class).selfVisitorFactory();

    @SuppressWarnings("unchecked")
    public static <T> Visitor<T, Optional<T>> values() {
        return (Visitor) factory.selfVisitor();
    }

    public static void main(String[] args) {

        Visitor<Integer, Void> visitor = new Visitor<Integer, Void>() {
            @Override
            public Void nothing() {
                System.out.println("Nothing!");
                return null;
            }

            @Override
            public Void something(Integer integer) {
                System.out.println("integer = " + integer);
                return null;
            }
        };


        Optional<Integer> optional = Optional.<Integer>values().something(1);
        optional.accept(visitor);

        optional = Optional.<Integer>values().nothing();
        optional.accept(visitor);

    }

}
