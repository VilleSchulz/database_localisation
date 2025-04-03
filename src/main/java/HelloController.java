import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class HelloController extends Application {
    public TextField translationNameField;
    public TextField keyNameField;
    @FXML
    private Button fetch_button;
    @FXML
    private Label error_label;
    @FXML
    private ListView<String> job_titles;
    @FXML
    private Label title;
    @FXML
    private ComboBox<String> languageSelector;

    private Locale locale;
    private ResourceBundle bundle;

    private final String DB_url = "jdbc:mysql://localhost:3306/open";
    private final String DB_username = "root";
    private final String DB_password = "";

    @Override
    public void start(Stage stage) throws Exception {
        // Määritä oletuskieli ennen FXML-latausta
        locale = new Locale("en", "US");
        bundle = ResourceBundle.getBundle("languages", locale);
        // Lataa FXML ja välitä kieliresurssi
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("helloview.fxml"));
        fxmlLoader.setResources(bundle);
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void initialize() {
        // Varmistetaan, että kielivalitsin ei ole null
        if (languageSelector != null) {
            languageSelector.getItems().addAll("English", "Spanish", "French", "汉语");
            languageSelector.getSelectionModel().select("English");
            languageSelector.setOnAction(event -> changeLanguage());
        }

        // Aseta käyttöliittymä oikeaan kieleen
        changeLanguage();
    }

    private void changeLanguage() {
        if (languageSelector == null) return;  // Varmistetaan ettei tule NullPointerExceptionia

        String selectedLanguage = languageSelector.getSelectionModel().getSelectedItem();
        String CountryCode = getLanguageCode(selectedLanguage)[1];
        String languageCode = getLanguageCode(selectedLanguage)[0];

        // Lataa uusi resurssipaketti ja päivitä UI
        locale = new Locale(languageCode, CountryCode);
        bundle = ResourceBundle.getBundle("languages", locale);

        if (title != null) title.setText(bundle.getString("title"));
        if (fetch_button != null) fetch_button.setText(bundle.getString("button"));
        translationNameField.setPromptText((bundle.getString("translationName")));
        keyNameField.setPromptText(bundle.getString("keyName"));
        fetchLocalizedData(languageCode);
    }

    private void fetchLocalizedData(String languageCode) {
        String query = "SELECT key_name,translation_text FROM translations WHERE language_code = ?";
        try (Connection conn = DriverManager.getConnection(DB_url, DB_username, DB_password);
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, languageCode);
            ResultSet rs = ps.executeQuery();
            job_titles.getItems().clear();

            while (rs.next()) {
                job_titles.getItems().add(rs.getString("key_name")+": " + rs.getString("translation_text"));
            }

        } catch (SQLException e) {
            if (error_label != null) {
                String databaseError = MessageFormat.format(bundle.getString("databaseError"),e.getMessage());
                error_label.setText(databaseError);
            }
        }
    }

    private String[] getLanguageCode(String language) {
        return switch (language) {
            case "English" -> new String[]{"en", "US"};
            case "Spanish" -> new String[]{"es", "ES"};
            case "French" -> new String[]{"fr", "FR"};
            case "汉语" -> new String[]{"zh", "CN"};
            default -> new String[]{"en", "US"};
        };
    }

    public void addJobTitle() {
        String keyName = keyNameField.getText();
        String translationName = translationNameField.getText();

        // Correct SQL query for INSERT
        String query = "INSERT INTO translations (key_name, translation_text, language_code) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_url, DB_username, DB_password);
             PreparedStatement ps = conn.prepareStatement(query)) {

            // Get the selected language code
            String languageCode = getLanguageCode(languageSelector.getSelectionModel().getSelectedItem())[0];

            // Set parameters for the query
            ps.setString(1, keyName);
            ps.setString(2, translationName);
            ps.setString(3, languageCode);

            // Execute the update (insert operation)
            ps.executeUpdate();

            // Fetch localized data after insertion
            fetchLocalizedData(languageCode);

        } catch (SQLException e) {
            if (error_label != null) {
                String databaseError = MessageFormat.format(bundle.getString("databaseError"), e.getMessage());
                error_label.setText(databaseError);
            }
        }
    }


    }
