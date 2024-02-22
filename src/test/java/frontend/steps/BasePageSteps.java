package frontend.steps;

import frontend.pages.BasePage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

public class BasePageSteps {

    BasePage basePage = new BasePage();

    @Given("User navigates to the {string} site")
    public void navigateToSite(String url) {
        basePage.navigate(url);
        basePage.waitForPageLoaded();
    }

    @When("User opens the {string} page")
    public void openPage(String page) {
        basePage.openPage(page);
    }

    @And("User search the book {string}")
    public void searchBook(String book) {
        basePage.findBook(book);
    }

    @Then("The {string} book is displayed")
    public void isBookDisplayed(String book) {
        Assert.assertEquals(basePage.getBookTitle(), book);
    }
}
