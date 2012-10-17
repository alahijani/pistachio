package github.alahijani.pistachio;

/**
 * @author Ali Lahijani
 */
public abstract class MutableCaseClass<CC extends MutableCaseClass<CC>> extends CaseClass<CC> {

    private final CaseVisitor<CC> assign = CaseClassFactory.get(getDeclaringClass()).selfVisitorFactory().assign(thisCase());

    public CC assign(CC that) {
        assign0(that);
        return thisCase();
    }

    public CaseVisitor<CC> assign() {
        return assign;
    }

}
