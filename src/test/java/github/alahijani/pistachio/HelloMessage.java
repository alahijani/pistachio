package github.alahijani.pistachio;

/**
 * @author Ali Lahijani
 */
public final class HelloMessage extends MutableCaseClass<HelloMessage> {

    private HelloMessage() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> Acceptor<Visitor<R>, R> acceptor() {
        return super.<R>acceptor().cast(classFactory.<R, Visitor<R>>caseVisitorFactory());
    }

    public <R> R accept(Visitor<R> visitor) {
        return this.<R>acceptor().accept(visitor);
    }

    public Visitor<HelloMessage> assign() {
        return (Visitor<HelloMessage>) super.assign();
    }

    /**
     *
     */
    public interface Visitor<R> extends CaseVisitor<R> {
        R helloWorld();

        R hello(String name);

        R hello(String title, String name);
    }

    private static final CaseClassFactory<HelloMessage> classFactory = CaseClassFactory.get(HelloMessage.class);
    private static final SelfVisitorFactory<HelloMessage, Visitor<HelloMessage>> factory = classFactory.selfVisitorFactory();

    public static Visitor<HelloMessage> values() {
        return factory.selfVisitor();
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
        HelloMessage hello = (Math.random() < 0.5) ? (Math.random() < 0.5)
                ? hello1
                : hello2
                : hello3;

        System.out.println(hello.accept(toString));
        hello.accept(analyzer);
        System.out.println();

        System.out.println("Assigning a value...");
        hello.assign().hello("Jack");

        System.out.println(hello.accept(toString));
        hello.accept(analyzer);
        System.out.println();
    }

}
