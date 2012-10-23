package github.alahijani.pistachio;

import java.lang.*;

/**
 * @author Ali Lahijani
 */
public class CaseReference<CC extends CaseClass<CC>, V extends CaseVisitor<java.lang.Void>> {

    private CC value;
    private final V assign;

    /**
     * Constructs a new {@code CaseReference} with the given initial value. The provided value should not be
     * {@code null}, since {@code CaseReference} needs a reference to a {@link CaseClassFactory}. Use
     * {@linkplain #CaseReference(CaseClassFactory) the other constructor} that explicitly takes
     * a {@code CaseClassFactory} to initialize with a value of {@code null}.
     *
     * @param value initial value of this reference
     * @throws NullPointerException if value is null
     */
    public CaseReference(CC value) {
        this(value.factory());
        this.value = value;
    }

    /**
     * Constructs a new {@code CaseReference} with the given factory and an initial value of {@code null}.
     *
     * @param factory CaseClassFactory to use
     * @throws NullPointerException if factory is null
     */
    public CaseReference(CaseClassFactory<CC> factory) {
        assign = factory.assign(this);
    }

    public CC get() {
        return value;
    }

    public void set(CC value) {
        this.value = value;
    }

    public V set() {
        return assign;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
