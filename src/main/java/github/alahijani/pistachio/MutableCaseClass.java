package github.alahijani.pistachio;

/**
 * @author Ali Lahijani
 */
public abstract class MutableCaseClass<CC extends MutableCaseClass<CC>> extends CaseClass<CC> {

    private final CaseVisitor<CC> assign = getFactory().selfVisitorFactory().assign(thisCase());

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
