package org.bimportal;

import org.openqa.selenium.By;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class Organisation {

    public void setOrganisationAuswahlen(String organisation) {

        By myOrg = By.xpath("//input[@id='searchBox']");
        $(myOrg).shouldBe(visible, Duration.ofSeconds(10)).setValue(organisation);
        By buttonSuhen = By.xpath("//form/app-button/div/button");
        $(buttonSuhen).shouldBe(visible, Duration.ofSeconds(10)).click();
        By buttonOrganisationAuswahlen = By.xpath(
                "//div[starts-with(@class,'table-row')]/div/div[normalize-space(text())='" + organisation + "']/../../div/button/span[normalize-space(text())='Anmelden']/.."
        );
        if ($(buttonOrganisationAuswahlen).shouldBe(visible, Duration.ofSeconds(15)).exists()) {
            $(buttonOrganisationAuswahlen)
                    .scrollIntoView(true)
                    .click();
        } else {
            System.out.println("ERROR! ORGANISATION IS NOT EXIST!");
        }
    }
}
