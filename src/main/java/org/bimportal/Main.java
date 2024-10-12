package org.bimportal;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import org.bimportal.pages.LoginPage;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.codeborne.selenide.Selenide.page;

public class Main {
    private static LoginPage loginPage;
    private static AIABearbeiten aiaBearbeiten;

    private static String configLoginFileName = "script_login.txt";
    private static String username = "";
    private static String password = "";
    private static boolean publish = true;

    private static String selectedUrl = ""; // Variable for the selected URL
    private static final int TIMEOUT = 10000; // 10 seconds
    private static final String DRIVER_NAME = "msedgedriver.exe";

    public static void main(String[] args) {
        Main main = new Main();
        try {
            if (!main.loadConfig()) return;
            main.initSelenide();
            main.run();
        } catch (Exception e) {
            e.printStackTrace();
            promptExit();
        } finally {
            if (WebDriverRunner.hasWebDriverStarted()) {
                WebDriverRunner.getWebDriver().quit();
            }
        }
    }

    public void initSelenide() {
        // Extract msedgedriver.exe from resources
        String driverPath = extractDriverFromResources("/" + DRIVER_NAME);
        if (driverPath != null) {
            System.setProperty("webdriver.edge.driver", driverPath);

            // Set up Edge options
            EdgeOptions options = new EdgeOptions();
            options.addArguments("--start-maximized"); // Launch in maximized mode

            // Selenide configuration
            Configuration.browser = "edge";
            Configuration.browserCapabilities = options;
            Configuration.timeout = TIMEOUT;

            // Initialize Selenide
            WebDriverRunner.setWebDriver(new EdgeDriver(options));

            // Open the selected URL
            Selenide.open(selectedUrl);
        } else {
            throw new RuntimeException("Failed to initialize Edge WebDriver.");
        }
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


        // Perform login
        login();

/*        // Register a shutdown hook for CTRL-C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gracefully...");
            WebDriverRunner.getWebDriver().quit(); // Close the WebDriver
        }));*/

        // Command input loop
        //try (Scanner scanner = new Scanner(System.in)) {
        try{
            while (true) {
                var input = chooseScenario();

//                System.out.println("Enter a command (\"exit\" to quit, \"return\" to repeat the last steps): ");
//                String input = scanner.nextLine().trim().toLowerCase();

                if (input == 0) {
                    System.out.println("Script terminated. Closing the browser...");
                    WebDriverRunner.getWebDriver().quit(); // Close the WebDriver
                    break; // Exit loop on exit command
                } else if (input == 1) {
                    System.out.println("Publishing...");
                    publish = true;
/*                } else if (input == 2) {
                    System.out.println("Repeating the last steps...");
                    publish = true; // Set repeat to true to re-run the last command in the next iteration
*/                } else {
                    System.out.println("Unrecognized command! Enter \"exit\" or \"return\".");
                }
                if (publish) {
                    mainScenario();
                }
                publish = false; // Reset repeat for the next iteration

                // Use JavaScript to make links open in the current tab
                Selenide.executeJavaScript("document.querySelectorAll('a[target=_blank]').forEach(function(link) { link.removeAttribute('target'); });");
            }
        } finally {
            // Ensure the browser is closed when done
            WebDriverRunner.getWebDriver().quit();
            System.out.println("Browser closed.");
        }
    }


    private void mainScenario() {
        try {
//            aiaBearbeiten.setAIABearbeiten();
//            aiaBearbeiten.clickSortIcon();
//            aiaBearbeiten.clickThreePoints();

//            if (!aiaBearbeiten.clickButtonInitialBeurteilen()) {
//                return;
//            }

            aiaBearbeiten.clickbuttonBeurteilungStarten();
            aiaBearbeiten.clickButtonPlus(username);
            aiaBearbeiten.clickSortIcon();
            aiaBearbeiten.clickThreePoints();
            if (aiaBearbeiten.clickbuttonPrufungBeenden()){
                aiaBearbeiten.prufungBeenden();
                aiaBearbeiten.clickSortIcon();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error occurred while executing the scenario.");
        }
    }

    private boolean loadConfig() {
        File configFile = new File(configLoginFileName);

        if (!configFile.exists() || !configFile.isFile()) {
            System.out.println("Configuration file not found. Please select it manually.");
            configFile = chooseFile("Select " + configLoginFileName);
        }

        if (configFile != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                String selectedCredentials = chooseUserCredentials(reader);

                if (selectedCredentials != null) {
                    String[] credentials = selectedCredentials.split(",");
                    if (credentials.length == 3) {
                        selectedUrl = credentials[0].trim();
                        username = credentials[1].trim();
                        password = credentials[2].trim();
                        if (selectedUrl.isEmpty()) {
                            System.out.println("Invalid URL.");
                            return false;
                        }if (username.isEmpty() || password.isEmpty()) {
                            System.out.println("Invalid username or password in the configuration file.");
                            return false;
                        }
                        System.out.println("Selected user: " + username);
                    } else {
                        System.out.println("Invalid credentials format. Use: url,username,password");
                        return false;
                    }
                } else {
                    System.out.println("No credentials selected.");
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            System.out.println("Configuration file not selected.");
            return false;
        }

        return true;
    }

    private void login() {
        if (!username.isEmpty() && !password.isEmpty()) {
            loginPage.validLoginInput(username, password);
            System.out.println("Logged in as: " + username);
/*            try {
                Thread.sleep(5000); // Wait for the page to load
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        } else {
            System.out.println("Credentials not found. Check the configuration file.");
        }
    }

    private int chooseScenario() {
        Object[] options = {
                "Exit",
                "Publish",
//                "Repeat"
        };
        return JOptionPane.showOptionDialog(null,
                "\"Exit\" to quit, \"Publish\" to continue",
                "Choose a command",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);
    }

    private String chooseUserCredentials(BufferedReader reader) throws IOException {
        List<String> userCredentials = new ArrayList<>();
        String line;

        while ((line = reader.readLine()) != null) {
            userCredentials.add(line.trim());
        }

        if (!userCredentials.isEmpty()) {
            String[] credentialsArray = userCredentials.toArray(new String[0]);
            Object[] credentialsToDisplayArray = userCredentials.stream().map(s->s.substring(0, s.lastIndexOf(","))).toArray();

            var selection = (String) JOptionPane.showInputDialog(null, "Select profile:", "Profile Selection",
                    JOptionPane.PLAIN_MESSAGE, null, credentialsToDisplayArray, credentialsToDisplayArray[0]);
            return Arrays.stream(credentialsArray).filter(s->s.startsWith(selection)).findFirst().get();
        }

        return null;
    }

    private File chooseFile(String dialogTitle) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(dialogTitle);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

        int returnValue = fileChooser.showOpenDialog(null);
        return returnValue == JFileChooser.APPROVE_OPTION ? fileChooser.getSelectedFile() : null;
    }

    private static void promptExit() {
        System.out.println("Press ENTER to exit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
