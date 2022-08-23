import de.variantsync.studies.evolution.simulation.diff.DiffParser;
import de.variantsync.studies.evolution.simulation.diff.components.FineDiff;
import de.variantsync.studies.evolution.simulation.diff.filter.ILineFilter;
import de.variantsync.studies.evolution.simulation.diff.splitting.DefaultContextProvider;
import de.variantsync.studies.evolution.simulation.diff.splitting.DiffSplitter;
import de.variantsync.studies.evolution.simulation.diff.splitting.IContextProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CachedPCBasedFilterTest {
    Path resourceDir = Paths.get("src", "test", "resources", "edit-filter");

    private void runComparison(Path pathToExpectedResult, ILineFilter lineFilter) throws IOException {
        List<String> diffLines = Files.readAllLines(resourceDir.resolve("diff-A-B.txt"));
        IContextProvider contextProvider = new DefaultContextProvider(resourceDir);
        FineDiff fineDiff;
        if (lineFilter == null) {
            fineDiff = DiffSplitter.split(DiffParser.toOriginalDiff(diffLines), contextProvider);
        } else {
            fineDiff = DiffSplitter.split(DiffParser.toOriginalDiff(diffLines), null, lineFilter, contextProvider);
        }

        List<String> expectedLines = Files.readAllLines(pathToExpectedResult);
        List<String> actualLines = fineDiff.toLines();
        for (int i = 0; i < expectedLines.size(); i++) {
            String expectedLine = expectedLines.get(i);
            String actualLine = actualLines.get(i);
            Assertions.assertEquals(expectedLine, actualLine);
        }
    }
    
    @Test
    public void filteredInsertion() throws IOException {
        Path pathToExpectedResult = resourceDir.resolve("fine-diff-A-B.txt");
        ILineFilter lineFilter = (f, i) -> !(f.toString().contains("version-B/first-file.txt") && i == 12);
        runComparison(pathToExpectedResult, lineFilter);
    }
}
