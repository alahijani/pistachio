package github.alahijani.pistachio;

import java.lang.reflect.Method;

/**
 * @author Ali Lahijani
 */
public class NewHelloMessage extends CaseClass<NewHelloMessage> {

    @Override
    public <R> Acceptor<Visitor<R>, R> acceptor() {
        CaseClassFactory.CaseVisitorFactory<R, Visitor<R>>
                factory = classFactory.caseVisitorFactory();

        return super.<R>acceptor().cast(factory);
    }

    public <R> R accept(Visitor<R> visitor) {
        return this.<R>acceptor().accept(visitor);
    }

    public interface Visitor<R> extends HelloMessage.Visitor<R> {
        R helloNewWorld();

        @Override
        R helloWorld();
    }

    private static final CaseClassFactory<NewHelloMessage> classFactory = CaseClassFactory.get(NewHelloMessage.class);

    public static Visitor<NewHelloMessage> values() {
        CaseClassFactory<NewHelloMessage>.SelfVisitorFactory<Visitor<NewHelloMessage>>
                factory = classFactory.selfVisitorFactory();

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

    public static void main(String[] args) throws NoSuchMethodException {
        {
            Method helloWorld1 = Visitor.class.getMethod("helloWorld");
            Method helloWorld2 = HelloMessage.Visitor.class.getMethod("helloWorld");

            System.out.println("(helloWorld1 == helloWorld2) = " + (helloWorld1 == helloWorld2));
        }

        {
            NewHelloMessage helloWorld1 = NewHelloMessage.values().helloWorld();
            HelloMessage helloWorld2 = HelloMessage.values().helloWorld();
            System.out.println("(helloWorld1 == helloWorld2) = " + helloWorld1.equals(helloWorld2));
            System.out.println("(helloWorld1 == helloWorld2) = " + helloWorld1.equals(NewHelloMessage.from(helloWorld2)));
        }
    }
}
