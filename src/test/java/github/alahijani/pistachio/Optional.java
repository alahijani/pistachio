package github.alahijani.pistachio;

/**
 * @author Ali Lahijani
 */
// @CaseClass.VisitorType(Optional.Visitor.class)
public final class Optional<T> extends CaseClass<Optional<T>>
        // CaseClass<Optional<T>, Optional.Visitor<T, Optional<T>>>
{

    private Optional() {
    }

    public <R> R accept(Visitor<T, R> visitor) {
        return apply(visitor, this);
    }

    /**
     *
     */
    public interface Visitor<T, R> extends CaseClass.Visitor<Optional<T>, R> {
        R nothing();

        R something(T t);
    }

    private static final CaseClassFactory<Optional<Object>, Visitor<Object, Optional<Object>>> factory =
            new CaseClassFactory<Optional<Object>, Visitor<Object, Optional<Object>>>() {};

    @SuppressWarnings("unchecked")
    public static <T> Visitor<T, Optional<T>> values() {
        return (Visitor) factory.eta();
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
