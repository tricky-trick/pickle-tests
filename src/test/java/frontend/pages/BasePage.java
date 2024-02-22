package frontend.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import pickleib.web.utilities.WebUtilities;

public class BasePage extends WebUtilities {

    WebDriver driver;

    public void waitForPageLoaded() {
        driver.findElement(By.id("app")).isDisplayed();
    }

    public void openPage(String title) {
        driver.findElement(By.xpath(String.format("//h5[text()='%s']", title))).click();
    }

    public void findBook(String title) {
        driver.findElement(By.xpath("//div[@id='searchBox-wrapper']//input")).sendKeys(title);
    }

    public String getBookTitle() {
        return driver.findElement(By.xpath("//div[@class='rt-table']//div[@class='rt-td']//a")).getText();
    }
}
