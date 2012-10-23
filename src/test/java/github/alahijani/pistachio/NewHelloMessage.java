package github.alahijani.pistachio;

import java.lang.reflect.Method;

/**
 * @author Ali Lahijani
 */
public class NewHelloMessage extends CaseClass<NewHelloMessage> {

    private NewHelloMessage() {
    }

    @Override
    public <R> Acceptor<? super Visitor<R>, R> acceptor() {
        return super.<R>acceptor().cast(Visitor.class);
    }

    public <R> R accept(Visitor<R> visitor) {
        return this.<R>acceptor().accept(visitor);
    }

    public interface Visitor<R> extends HelloMessage.Visitor<R> {
        R helloNewWorld();

        @Override
        R helloWorld();
    }

    private static final CaseClassFactory<NewHelloMessage> classFactory = new NewHelloMessage().factory();

    public static Visitor<NewHelloMessage> values() {
        return (Visitor<NewHelloMessage>) classFactory.values();
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
            NewHelloMessage newHelloWorld = NewHelloMessage.values().helloWorld();
            HelloMessage oldHelloWorld = HelloMessage.values().helloWorld();
            System.out.println("(newHelloWorld == oldHelloWorld) = " + newHelloWorld.equals(oldHelloWorld));
            System.out.println("(newHelloWorld == oldHelloWorld) = " + newHelloWorld.equals(NewHelloMessage.from(oldHelloWorld)));

            CaseClass.Acceptor<? super NewHelloMessage.Visitor<Void>, Void> newAcceptor = newHelloWorld.acceptor();
            CaseClass.Acceptor<? super HelloMessage.Visitor<Void>, Void> oldAcceptor = oldHelloWorld.acceptor();

            newAcceptor = oldAcceptor;
//            oldAcceptor = newAcceptor;

            NewHelloMessage.Visitor<Void> newVisitor = null;
            HelloMessage.Visitor<Void> oldVisitor = null;

            CaseVisitor<Void> caseVisitor = null;
            newAcceptor.accept(newVisitor);
            oldAcceptor.accept(newVisitor);
            oldAcceptor.accept(oldVisitor);

//            newAcceptor.accept(oldVisitor);
//            oldAcceptor.accept(caseVisitor);
//            newAcceptor.accept(caseVisitor);
        }
    }
}
