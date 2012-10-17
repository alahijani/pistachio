package github.alahijani.pistachio;

/**
 * @author Ali Lahijani
 */
public abstract class MutableCaseClass<CC extends MutableCaseClass<CC>> extends CaseClass<CC> {

    private final Visitor<CC> assign = CaseClassFactory.get(getDeclaringClass()).selfVisitorFactory().assign(thisCase());

    public CC assign(CC that) {
        assign0(that);
        return thisCase();
    }

    public Visitor<CC> assign() {
        return assign;
    }

}
