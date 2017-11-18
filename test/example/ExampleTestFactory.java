package example;

import example.ignored.IgnoredEntity;
import example.nestedlist.NestedListEntity;
import example.ommited.OmittedEntity;
import example.renamed.RenamedEntity;
import example.restructured.RestructuredEntity;
import example.traversal.TraversalEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class ExampleTestFactory implements Iterator<DynamicTest> {
    private static final Path README_OUTPUT = Paths.get("readme.md");

    private static final Testdoc[] TESTDOCS = new Testdoc[] {
            new Testdoc(Paths.get("test/example/renamed/"), RenamedEntity.class, "Renaming Fields", "`@JsonAnnotation(\"...\")`"),
            new Testdoc(Paths.get("test/example/ommited/"), OmittedEntity.class, "Omitting Field Name", "ending with a `@JsonAnnotation(\".../\")`"),
            new Testdoc(Paths.get("test/example/ignored/"), IgnoredEntity.class, "Ignoring Parts of Structure", "blank annotations `@JsonAnnotation(\"\")`"),
            new Testdoc(Paths.get("test/example/restructured/"), RestructuredEntity.class, "Restructuring Object", "description"),
            new Testdoc(Paths.get("test/example/traversal/"), TraversalEntity.class, "Traversing Paths", "description"),
            new Testdoc(Paths.get("test/example/nestedlist/"), NestedListEntity.class, "Nested List", "description"),
    };
    // todo ignored fields (members and objects)
    private int testdocIndex;

    @TestFactory
    Iterator<DynamicTest> mapperPrinterTests() {
        return new ExampleTestFactory();
    }

    public boolean hasNext() {
        return testdocIndex < TESTDOCS.length;
    }

    public DynamicTest next() {
        Testdoc testdoc = TESTDOCS[testdocIndex++];
        return DynamicTest.dynamicTest(testdoc.name, testdoc);
    }

    @AfterAll
    static void publishReadme() throws IOException {
        StringBuilder readmeBuilder = new StringBuilder();
        for (Testdoc testdoc : TESTDOCS)
            readmeBuilder.append(testdoc.markdown);
        String readme = readmeBuilder.toString();
        Files.write(README_OUTPUT, readme.getBytes());
    }
}
