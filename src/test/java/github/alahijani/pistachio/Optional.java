package github.alahijani.pistachio;

/**
 * @author Ali Lahijani
 */
public final class Optional<T> extends CaseClass<Optional<T>> {

    public interface Visitor<T, R> extends CaseVisitor<R> {
        R none();

        R some(T t);
    }

    private Optional() {
    }

    @Override
    public <R> Acceptor<? super Visitor<T, R>, R> acceptor() {
        return super.<R>acceptor().cast(Visitor.class);
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

    public static <T> Visitor<T, Optional<T>> values() {
        /**
         * We could cache the result of factory() for efficiency,
         * but this is evidently more readable and also does not flag
         * an unchecked cast.
         */
        return (Visitor<T, Optional<T>>) new Optional<T>().factory().values();
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
