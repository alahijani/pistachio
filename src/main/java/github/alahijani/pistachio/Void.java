package github.alahijani.pistachio;

/**
 * A case class that can accept <em>any</em> instance of {@link CaseVisitor}.
 * Subsequently it has no instances, because there are visitor interfaces that
 * cannot handle any constructor (like {@link CaseVisitor} itself).
 * <p/>
 * Because an instance of <code>Void</code> does not hold any state (simply
 * and vacuously, because there are no instances of <code>Void</code> at all!)
 * <code>Void</code> is both mutable and pooled/immutable.
 *
 * @author Ali Lahijani
 */
public final class Void extends CaseClass<Void> {
    private Void() {
    }

    @Override
    public <R> Acceptor<? super CaseVisitor<R>, R> acceptor() {
        return super.<R>acceptor().cast(CaseVisitor.class);
    }

    public <R> R accept(CaseVisitor<R> visitor) {
        return this.<R>acceptor().accept(visitor);
    }

    private static final CaseClassFactory<Void> classFactory = CaseClassFactory.get(Void.class);

    public static CaseVisitor<Void> values() {
        return classFactory.values();
    }

}
