package org.bimportal;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.By;

import java.time.Duration;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class AIABearbeiten {
    By buttonBearbeiten = By.xpath("//button/div/span[contains(text(),'AIA')]/../../../../../following-sibling::div/div/div/button[contains(text(),'Bearbeiten')]");
    By buttonInitialBeurteilen = By.xpath("//button[contains(text(),'Initial beurteilen')]");
    By buttonBeurteilungStarten = By.xpath("//button[contains(text(),'Beurteilung starten')]");
    By buttonUbernehmen = By.xpath("//button[contains(text(),'Übernehmen')]");
    By buttonAnnehmen = By.xpath("//button[contains(text(),'Annehmen')]");
    By buttonAnnehmenPruferAuswahlen = By.xpath("//div[@class='modal-footer justify-content-center']//button[@type='submit' and normalize-space(text())='Annehmen']");
    By buttonFortfahren = By.xpath("//button[contains(text(),'Fortfahren')]");
    By buttonBestatigen = By.xpath("//button[contains(text(),'Bestätigen')]");
    By buttonPrufungBeenden = By.xpath("//button[contains(text(),'Prüfung beenden')]");

    public void setAIABearbeiten() {
        By selector = By.xpath("//accordion-group[2]/div/div/div/div/button/div/span");
        SelenideElement element = $(selector);
        actions().moveToElement(element)
                .pause(Duration.ofMillis(1000))
                .click(element).perform();
        if ($(buttonBearbeiten).exists()) {
            $(buttonBearbeiten).scrollIntoView(true)
                    .shouldBe(Condition.exist, Duration.ofMillis(1000)).click();

        } else {
            System.out.println("ERROR! BUTTON BEARBEITEN IS NOT EXIST!");
        }
    }

    public void clickSortIcon() {
        By sortIcon = By.xpath("//div[contains(text(),'Letzte Änderung')]//img[@alt='Pfeil nach unten deaktiviert']");
        $(sortIcon).click();
    }

    public boolean clickButtonInitialBeurteilen() {
        if ($(buttonInitialBeurteilen).exists()) {
            $(buttonInitialBeurteilen)
                    .shouldHave(exist, Duration.ofMillis(15000))
                    .scrollIntoView(true)
                    .click();
        } else {
            System.out.println("BUTTON INITIAL BEURTEILEN IS NOT EXIST!");
            $(buttonPrufungBeenden).click();
            return false;
        }
        return true;
    }

    public void clickbuttonBeurteilungStarten() {
        SelenideElement element = $(buttonBeurteilungStarten);
        if (element.has(Condition.visible, Duration.ofMillis(1000))) {
            element.click();
            $(buttonUbernehmen).scrollIntoView(true).click();
        } else {
            System.out.println("Der Benutzer schon als Genehmiger eingetragen ist!" +
                    "Deshalb der Button Beurteilung starten ist nicht vorhanden!");
        }
        $(buttonAnnehmen).scrollIntoView(true).click();
    }

    public void clickButtonPlus(String validEmail) {
        By buttonPlus = By.xpath("//div[contains(text(),'" + validEmail + "')]/button");
        if ($(buttonPlus).has(Condition.visible,Duration.ofMillis(1000))){
            $(buttonPlus).click();
            $(buttonAnnehmenPruferAuswahlen).scrollIntoView(true).click();
        }else {
            $(buttonBestatigen).click();
        }
        $(buttonFortfahren).click();
    }

    public void clickThreePoints() {
//        By buttonThreePoints = By.xpath("(//div[@class='d-flex justify-content-end']//button[@class='btn table-button ms-1 btn-primary'])[2]");
        By buttonThreePoints = By.xpath("(//div[@class='d-flex justify-content-end']//button[contains(@class,'btn table-button')])[2]");
        if ($(buttonThreePoints).is(exist, Duration.ofSeconds(10))) $(buttonThreePoints).click();
    }

    public boolean clickbuttonPrufungBeenden() {

        if ($(buttonPrufungBeenden).exists()) {
            $(buttonPrufungBeenden)
                    .shouldHave(exist, Duration.ofMillis(15000))
                    .scrollIntoView(true)
                    .click();
        } else {
            System.out.println("ERROR! BUTTON  PRÜFUNG BEENDEN IS NOT EXIST!");
            return false;
        }
        return true;
    }

    public void prufungBeenden() {
        $(buttonAnnehmen).scrollIntoView(true).click();
        $(buttonBestatigen).scrollIntoView(true).click();
        $(buttonFortfahren).click();
    }

    public void filterName(String name){
        By otherElement = By.xpath("//app-header/div[1]/div[2]/h1[1]");
        By filter = By.xpath("//thead/tr[2]/td[5]/div[1]/div[1]/input[1]");

        //$(filter).setValue("qayw").pressEnter();
        //actions().pause(1000).perform();
        //actions().sendKeys($(filter),"qayw").sendKeys($(filter), Keys.ENTER).pause(1000).perform();
        //actions().sendKeys($(filter), Keys.BACK_SPACE).sendKeys($(filter), Keys.BACK_SPACE).sendKeys($(filter), Keys.BACK_SPACE).sendKeys($(filter), Keys.BACK_SPACE).pause(500).perform();
        $(filter).shouldBe(visible, Duration.ofSeconds(10)).setValue(name).pressEnter();
        //actions().sendKeys($(filter), name).sendKeys($(filter), Keys.ENTER).pause(2000).perform();
        $(otherElement).click();
        actions().pause(2000).perform();
       // $(filterName).setValue(name);
    }

}
