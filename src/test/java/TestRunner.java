import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"src/test/java/features"},
        plugin = {"json:target/reports/Cucumber.json"},
        glue = {"src/test/java/frontend/steps"})

public class TestRunner {
}
