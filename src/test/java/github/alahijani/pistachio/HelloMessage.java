package github.alahijani.pistachio;

/**
 * @author Ali Lahijani
 */
public final class HelloMessage extends MutableCaseClass<HelloMessage> {

    private HelloMessage() {
    }

    /**
     *
     */
    public interface Visitor<R> extends CaseClass.Visitor<HelloMessage, R> {
        R helloWorld();

        R hello(String name);

        R hello(String title, String name);
    }

    public <R> R accept(Visitor<R> visitor) {
        // return CaseClass.apply(visitor, this);
        return CaseClass.apply(visitor, this);
    }

    private static final CaseClassFactory<HelloMessage, Visitor<HelloMessage>> factory
            = new CaseClassFactory<HelloMessage, Visitor<HelloMessage>>() {};

    public static Visitor<HelloMessage> values() {
        return factory.eta();
    }

    public Visitor<HelloMessage> assign() {
        return (Visitor<HelloMessage>) super.assign();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Block<Visitor<R>, R> dni() {
        return (Block) super.dni();
    }

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
