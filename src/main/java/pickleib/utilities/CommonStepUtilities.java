package pickleib.utilities;

import pickleib.driver.DriverFactory;
import pickleib.mobile.driver.PickleibAppiumDriver;
import pickleib.mobile.interactions.MobileInteractions;
import pickleib.utilities.element.ElementAcquisition;
import pickleib.utilities.page.repository.PageRepository;
import pickleib.web.driver.PickleibWebDriver;
import pickleib.web.interactions.WebInteractions;
import utils.Printer;
import utils.StringUtilities;

/**
 * A utility class that provides common methods and interactions for web and mobile steps in the context of Pickleib.
 *
 * <p>
 * This class houses the utilities and common functionality for both web and mobile platforms,
 * such as acquiring page objects, getting element interactions, and reflections based on the specific platform type.
 * </p>
 *
 * @param <ObjectRepository> A type extending {@link PageRepository} which provides the structure and access
 *                           to the underlying page elements and components.
 *
 * @author Umut Ay Bora
 * @since 1.8.7
 */
public class CommonStepUtilities<ObjectRepository extends PageRepository> {

    public DriverFactory.DriverType defaultPlatform = DriverFactory.DriverType.Web;
    public StringUtilities strUtils = new StringUtilities();
    public Printer log = new Printer(this.getClass());
    public WebInteractions webInteractions = new WebInteractions();
    public MobileInteractions mobileInteractions = new MobileInteractions();
    ElementAcquisition.PageObjectModel<ObjectRepository> webObjectModel;
    ElementAcquisition.PageObjectModel<ObjectRepository> mobileObjectModel;
    ElementAcquisition.Reflections<ObjectRepository> webReflections;
    ElementAcquisition.Reflections<ObjectRepository> mobileReflections;

    /**
     * Constructs an instance of the CommonStepUtilities class with the specific object repository.
     *
     * @param objectRepositoryClass The class of the object repository which will be used to initialize
     *                              the page object model, element interactions, and reflections.
     */
    public CommonStepUtilities(Class<ObjectRepository> objectRepositoryClass) {

        webObjectModel = new ElementAcquisition.PageObjectModel<>(
                PickleibWebDriver.driver,
                objectRepositoryClass
        );
        mobileObjectModel = new ElementAcquisition.PageObjectModel<>(
                PickleibAppiumDriver.driver,
                objectRepositoryClass
        );
        webReflections = new ElementAcquisition.Reflections<>(
                PickleibWebDriver.driver,
                objectRepositoryClass
        );
        mobileReflections = new ElementAcquisition.Reflections<>(
                PickleibAppiumDriver.driver,
                objectRepositoryClass
        );
    }

    /**
     * Retrieves the appropriate element interactions based on the given driver type.
     *
     * @param driverType The type of the driver (Web or Mobile).
     * @return The element interactions for the specified driver type.
     */
    public Interactions getInteractions(DriverFactory.DriverType driverType) {
        if (!StringUtilities.isBlank(driverType))
            switch (driverType) {
                case Web -> {
                    return webInteractions;
                }
                case Mobile -> {
                    return mobileInteractions;
                }
            }
        else return getInteractions(defaultPlatform);
        return null;
    }

    /**
     * Retrieves the page object acquisition for the specified driver type.
     *
     * @param driverType The type of the driver (Web or Mobile).
     * @return The page object acquisition for the specified driver type.
     */
    public ElementAcquisition.PageObjectModel<ObjectRepository> getAcquisition(DriverFactory.DriverType driverType) {
        if (!StringUtilities.isBlank(driverType))
            switch (driverType) {
                case Web -> {
                    return webObjectModel;
                }
                case Mobile -> {
                    return mobileObjectModel;
                }
            }
        else return getAcquisition(defaultPlatform);
        return null;
    }

    /**
     * Retrieves the reflections for the specified driver type, which provides a view into the object repository's
     * structure and elements.
     *
     * @param driverType The type of the driver (Web or Mobile).
     * @return The reflections for the specified driver type.
     */
    public ElementAcquisition.Reflections<ObjectRepository> getReflections(DriverFactory.DriverType driverType) {
        if (!StringUtilities.isBlank(driverType))
            switch (driverType) {
                case Web -> {
                    return webReflections;
                }
                case Mobile -> {
                    return mobileReflections;
                }
            }
        else return getReflections(defaultPlatform);
        return null;
    }

}
