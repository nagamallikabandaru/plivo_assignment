package com.plivo.api.automation;

import com.github.mkolisnyk.cucumber.runner.ExtendedCucumber;
import com.github.mkolisnyk.cucumber.runner.ExtendedCucumberOptions;
import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(ExtendedCucumber.class)
@ExtendedCucumberOptions(jsonReport = "src/test/results/cucumber.json",
        retryCount = 0,
        detailedReport = true,
        overviewReport = true,
        outputFolder = "target")
@CucumberOptions(plugin = { "html:src/test/results/cucumber-html-report",
        "json:src/test/results/cucumber.json",
        "usage:src/test/results/cucumber-usage.json",
        "junit:src/test/results/cucumber-results.xml"
},
        features = {"./src/test/resources"},
        glue = { "com/plivo/api/automation" },
        tags = {"@demo"})


public class CukesRunnerTest{
}
