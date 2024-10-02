package org.bimportal;

import org.bimportal.pages.LoginPage;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.codeborne.selenide.Selenide.page;

public class Main {
    private static LoginPage loginPage;
    private static AIABearbeiten aiaBearbeiten;
    private static Organisation organisation;
    private static String lastCommand = "";
    private static String organisationName = "";
    private static boolean isLoggedIn = false;
    private static String configLoginFileName = "script_login.txt";
    private static String configOrganisationFileName = "organisation_config.txt";
    private static String username = "";
    private static String password = "";
    private static List<String> organisations = new ArrayList<>();
    private static String currentDirPath = System.getProperty("user.dir");
    private static boolean repeat = true;

    public static void main(String[] args) {
        Main main = new Main();
        try {
            main.initSelenide();
            main.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Press ENTER to exit...");
            try {
                System.in.read();
            } catch (IOException e2) {
                throw new RuntimeException(e2);
            }
        } finally {
            // Close the WebDriver when done
            WebDriverRunner.getWebDriver().quit();
        }
    }

    public void initSelenide() {
        // Extract msedgedriver.exe from resources
        String driverPath = extractDriverFromResources("/msedgedriver.exe");
        System.setProperty("webdriver.edge.driver", driverPath);

        // Set up Edge options
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--start-maximized"); // Start in maximized mode

        // Selenide configuration
        Configuration.browser = "edge";
        Configuration.browserCapabilities = options;
        Configuration.timeout = 10000;

        // Initialize Selenide
        WebDriverRunner.setWebDriver(new EdgeDriver(options));
        // Open the login page
        Selenide.open("https://via-integration.itz.res.bund.de/bim/infrastruktur/login");
    }

    private String extractDriverFromResources(String resourcePath) {
        try {
            File tempDriver = File.createTempFile("msedgedriver", ".exe");
            tempDriver.deleteOnExit(); // Delete file on exit

            try (InputStream in = Main.class.getResourceAsStream(resourcePath);
                 FileOutputStream out = new FileOutputStream(tempDriver)) {
                if (in == null) {
                    throw new IllegalArgumentException("Driver not found in resources: " + resourcePath);
                }
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempDriver.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error extracting driver: " + e.getMessage());
        }
    }

    public void run() {
        // Initialize page objects
        loginPage = page(LoginPage.class);
        aiaBearbeiten = page(AIABearbeiten.class);
        organisation = new Organisation();

        if (!loadConfig()) return;

        // Perform login
        login();
        selectOrganisation();

        // Command input loop
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                if (repeat) mainScenario();
                repeat = false;
                System.out.println("Enter the command (\"exit\" to exit, \"repeat\" to repeat the last command): ");
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                    System.out.println("Script completed.");
                    break;
                } else if (input.equalsIgnoreCase("repeat")) {
                    System.out.println("Repeating last command...");
                    repeat = true;
                } else {
                    System.out.println("ERROR! Command not recognized!");
                }
                // Use JavaScript to make links open in the current tab
                Selenide.executeJavaScript("document.querySelectorAll('a[target=_blank]').forEach(function(link) { link.removeAttribute('target'); });");
            }
        }
    }

    private void mainScenario() {
        // Call methods from aiaBearbeiten to perform operations
        aiaBearbeiten.setAIABearbeiten();
        aiaBearbeiten.clickSortIcon();
        aiaBearbeiten.clickThreePoints();
        if (!aiaBearbeiten.clickButtonInitialBeurteilen()) return;
        aiaBearbeiten.clickbuttonBeurteilungStarten();

        aiaBearbeiten.clickButtonPlus(username);
        aiaBearbeiten.clickSortIcon();
        aiaBearbeiten.clickThreePoints();
        aiaBearbeiten.clickbuttonPrufungBeenden();
        aiaBearbeiten.prufungBeenden();
        aiaBearbeiten.clickSortIcon();
    }

    private boolean loadConfig() {
        // Check for user configuration file
        File configFile = new File(configLoginFileName);
        if (!configFile.exists() || !configFile.isFile()) {
            System.out.println("Config file not found. Please select the config file manually.");
            configFile = chooseFile("Select " + configLoginFileName);
        }
        if (configFile != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                String selectedUserCredentials = chooseUserCredentials(reader);
                if (selectedUserCredentials != null) {
                    String[] credentials = selectedUserCredentials.split(",");
                    username = credentials[0].trim();
                    password = credentials[1].trim();
                    if (username.isEmpty() || password.isEmpty()) {
                        System.out.println("No login or password specified in the config file.");
                        return false;
                    }

                    System.out.println("Selected from selected file - Username: " + username);
                } else {
                    System.out.println("No login credentials selected. Exiting.");
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Reading from " + configLoginFileName + " file in working directory...");
        } else {
            System.out.println("No file selected or file is invalid. Exiting.");
            return false;
        }

        // Read the organization configuration file
        File organisationConfigFile = new File(configOrganisationFileName);
        if (!organisationConfigFile.exists() || !organisationConfigFile.isFile()) {
            System.out.println("Organisation config file not found. Please select the config file manually.");
            organisationConfigFile = chooseFile("Select " + configOrganisationFileName);
        }
        if (organisationConfigFile != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(organisationConfigFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    organisations.add(line.trim());
                }
                if (organisations.isEmpty()) {
                    System.out.println("No organisations found in the configuration file.");
                    return false; // No organizations found
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No file selected or file is invalid. Exiting.");
            return false;
        }

        return true;
    }

    private void login() {
        // Perform login
        if (!username.isEmpty() && !password.isEmpty()) {
            loginPage.validLoginInput(username, password); // This method clears fields and logs in
            isLoggedIn = true; // Assuming loginPage updates this
            System.out.println("Login completed: " + username);
        } else {
            System.out.println("No login credentials found. Please check the config file.");
        }
    }

    private void selectOrganisation() {
        // Convert list to array for display
        String[] organisationsArray = organisations.toArray(new String[0]);
        String selected = (String) JOptionPane.showInputDialog(
                null,
                "Select organisation:",
                "Choose Organisation",
                JOptionPane.PLAIN_MESSAGE,
                null,
                organisationsArray,
                organisationsArray[0]
        );

        if (selected != null) {
            organisationName = selected;
            System.out.println("Selected Organisation: " + organisationName);
            organisation.setOrganisationAuswahlen(organisationName); // Selecting the organization
        } else {
            System.out.println("No organisation selected. Exiting.");
        }
    }

    private String chooseUserCredentials(BufferedReader reader) throws IOException {
        List<String> userCredentials = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            userCredentials.add(line.trim());
        }

        if (userCredentials.isEmpty()) {
            return null; // No credentials found
        }

        // Show a dialog to choose from available users
        String[] credentialsArray = userCredentials.toArray(new String[0]);
        String selected = (String) JOptionPane.showInputDialog(
                null,
                "Select user:",
                "Choose User",
                JOptionPane.PLAIN_MESSAGE,
                null,
                credentialsArray,
                credentialsArray[0]
        );

        return selected;
    }

    private File chooseFile(String dialogTitle) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(dialogTitle);

        // Set the directory to the path where your files are located
        File currentDir = new File(currentDirPath);
        fileChooser.setCurrentDirectory(currentDir);

        int returnValue = fileChooser.showOpenDialog(null);
        return returnValue == JFileChooser.APPROVE_OPTION ? fileChooser.getSelectedFile() : null;
    }
}
