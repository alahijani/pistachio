package github.alahijani.pistachio;

/**
 * @author Ali Lahijani
 */
public class NewHelloMessage extends CaseClass<NewHelloMessage> {

    public interface Visitor<R> extends HelloMessage.Visitor<Visitor<R>> {
        R helloNewWorld();
    }

}
