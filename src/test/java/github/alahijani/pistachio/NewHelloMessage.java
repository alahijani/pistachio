package github.alahijani.pistachio;

/**
 * @author Ali Lahijani
 */
public class NewHelloMessage extends CaseClass<NewHelloMessage> {

    @SuppressWarnings("unchecked")
    @Override
    public <R> Acceptor<Visitor<R>, R> acceptor() {
        return (Acceptor<Visitor<R>, R>) super.<R>acceptor();
    }

    public <R> R accept(Visitor<R> visitor) {
        return this.<R>acceptor().accept(visitor);
    }

    public interface Visitor<R> extends HelloMessage.Visitor<R> {
        R helloNewWorld();
    }

    private static final SelfVisitorFactory<NewHelloMessage, Visitor<NewHelloMessage>> factory
            = CaseClassFactory.get(NewHelloMessage.class).selfVisitorFactory();

    public static Visitor<NewHelloMessage> values() {
        return factory.selfVisitor();
    }

    /**
     * Interface <code>NewHelloMessage.Visitor</code> being a subtype of <code>HelloMessage.Visitor</code>
     * makes <code>NewHelloMessage</code> a <em>supertype</em> of <code>HelloMessage</code>. This static
     * conversion method witnesses the inclusion of <code>HelloMessage</code>s into <code>NewHelloMessage</code>s.
     */
    public static NewHelloMessage from(HelloMessage instance) {
        return instance.accept(NewHelloMessage.values());
    }
}
