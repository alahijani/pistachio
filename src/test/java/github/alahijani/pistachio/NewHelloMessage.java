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

}
