package github.alahijani.pistachio;

import java.util.*;

/**
 * A duplicate of {@link javax.tools.JavaFileObject.Kind} for showcasing enums.
 *
 * @author Ali Lahijani
 */
public class FileType extends CaseClass<FileType> {

    public interface Visitor<R> extends CaseVisitor<R> {
        /**
         * Source files written in the Java programming language.  For
         * example, regular files ending with {@code .java}.
         */
        R SOURCE();

        /**
         * Class files for the Java Virtual Machine.  For example,
         * regular files ending with {@code .class}.
         */
        R CLASS();

        /**
         * HTML files.  For example, regular files ending with {@code
         * .html}.
         */
        R HTML();

        /**
         * Any other kind.
         */
        R OTHER();
    }

    private static final CaseClassFactory<FileType> classFactory = CaseClassFactory.get(FileType.class, new Visitor<FileType>() {
        @Override
        public FileType SOURCE() {
            return new FileType(".java");
        }

        @Override
        public FileType CLASS() {
            return new FileType(".class");
        }

        @Override
        public FileType HTML() {
            return new FileType(".html");
        }

        @Override
        public FileType OTHER() {
            return new FileType("");
        }
    });

    private static Visitor<FileType> values() {
        return (Visitor<FileType>) classFactory.values();
    }

    /**
     * The extension which (by convention) is normally used for
     * this kind of file object.  If no convention exists, the
     * empty string ({@code ""}) is used.
     */
    public final String extension;

    private FileType(String extension) {
        extension.getClass(); // null check
        this.extension = extension;
    }

    @Override
    public <R> Acceptor<? super Visitor<R>, R> acceptor() {
        return super.<R>acceptor().cast(Visitor.class);
    }

    public static void main(String[] args) {
        java.util.List<FileType> list = Arrays.asList(values().SOURCE(), values().CLASS(), values().HTML(), values().OTHER());
        for (FileType fileType : list) {
            System.out.println("fileType = " + fileType + ", fileType.extension = " + fileType.extension);
        }
    }
}
