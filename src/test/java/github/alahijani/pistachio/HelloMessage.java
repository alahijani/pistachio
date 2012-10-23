package github.alahijani.pistachio;

/**
 * @author Ali Lahijani
 */
public final class HelloMessage extends CaseClass<HelloMessage> {

    public interface Visitor<R> extends CaseVisitor<R> {
        R helloWorld();

        R hello(String name);

        R hello(String title, String name);
    }

    private HelloMessage() {
    }

    @Override
    public <R> Acceptor<? super Visitor<R>, R> acceptor() {
        return super.<R>acceptor().cast(Visitor.class);
    }

    public <R> R accept(Visitor<R> visitor) {
        return this.<R>acceptor().accept(visitor);
    }

    private static final CaseClassFactory<HelloMessage> classFactory = new HelloMessage().factory();

    public static Visitor<HelloMessage> values() {
        return (Visitor<HelloMessage>) classFactory.values();
    }

    /**
     * This can be loaded from a properties file, a la GWT
     */
    public static Visitor<String> toString =
            new Visitor<String>() {
                @Override
                public String helloWorld() {
                    return "Hello World!";
                }

                @Override
                public String hello(String name) {
                    return "Hello " + name + "!";
                }

                @Override
                public String hello(String title, String name) {
                    return "Hello " + title + " " + name + "!";
                }
            };


    public static void main(String[] args) {
        HelloMessage hello1 = HelloMessage.values().helloWorld();
        HelloMessage hello2 = HelloMessage.values().hello("John");
        HelloMessage hello3 = HelloMessage.values().hello("Mr", "Smith");

        System.out.println(hello1);
        System.out.println(hello2);
        System.out.println(hello3);
        System.out.println();

        System.out.println(hello1.accept(toString));
        System.out.println(hello2.accept(toString));
        System.out.println(hello3.accept(toString));
        System.out.println();

        Visitor<Void> analyzer = new Visitor<Void>() {
            @Override
            public Void helloWorld() {
                System.out.println("That's boring.");
                return null;
            }

            @Override
            public Void hello(String name) {
                System.out.println("Is it you " + name + "?");
                return null;
            }

            @Override
            public Void hello(String title, String name) {
                System.out.println("So it was " + name + ", which is a \"" + title + "\".");
                return null;
            }
        };

        System.out.println("Randomizing...");
        HelloMessage helloX = (Math.random() < 0.5) ? (Math.random() < 0.5)
                ? hello1
                : hello2
                : hello3;

        CaseReference<HelloMessage, Visitor<java.lang.Void>> hello = new CaseReference<>(helloX);

        System.out.println(hello.get().accept(toString));
        hello.get().accept(analyzer);
        System.out.println();

        System.out.println("Assigning a value...");
        hello.set().hello("Jack");

        System.out.println(hello.get().accept(toString));
        hello.get().accept(analyzer);
        System.out.println();
    }

}
