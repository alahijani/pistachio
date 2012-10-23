package github.alahijani.pistachio;

/**
 * @author Ali Lahijani
 */
public abstract class MutableCaseClass<CC extends MutableCaseClass<CC>> extends CaseClass<CC> {

    /**
     * TODO factory() needs a ClassValue lookup at this stage. This can be quite slow....
     */
    private final CaseVisitor<CC> assign = factory().assign(this.<CC>acceptor());

    public CC assign(CC that) {
        assign0(that);
        return thisCase();
    }

    /**
     * A concrete mutable case class should override this method and cast the result to the actual sub-interface of
     * <code>CaseVisitor</code>.
     */
    public CaseVisitor<CC> assign() {
        return assign;
    }

}
