package org.bimportal.pages;

import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class LoginPage {
    /* Elements */
    private static final By emailInput = By.xpath("//input[@id='username']");
    private static final By pwdInput = By.xpath("//input[@id='password']");
    private static final By loginBtn = By.xpath("//button[contains(text(),'Anmelden')]");

    public void validLoginInput(String username, String password) {
        clearFields();
        $(emailInput).setValue(username);
        $(pwdInput).setValue(password);
        $(loginBtn).click();
    }

    private void clearFields() {
        executeJavaScript("document.getElementById('username').value = '';");
        executeJavaScript("document.getElementById('password').value = '';");
    }

    // Method to check if login was successful
    public boolean isLoginSuccessful() {
        try {
            // Replace this with your logic to check for successful login
            return $(By.xpath("//p[contains(text(),'Wählen Sie die Organisation, in der Sie arbeiten m')]")).exists();
        } catch (Exception e) {
            return false;
        }
    }
}
