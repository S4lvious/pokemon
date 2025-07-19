package engine;

import static java.util.Locale.getDefault;
import static java.util.ResourceBundle.getBundle;

import java.util.Locale;
import java.util.ResourceBundle;

public final class LocalizationManager {

    private static LocalizationManager instance;

    private ResourceBundle messages;
    private Locale currentLocale;

    private LocalizationManager() {
        setLocale(Locale.of("it", "IT")); // Default locale
    }

    public static LocalizationManager getInstance() {
        if (instance == null) {
            synchronized (LocalizationManager.class) {
                if (instance == null) {
                    instance = new LocalizationManager();
                }
            }
        }
        return instance;
    }

    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        try {
            messages = getBundle("assets.lang.messages", currentLocale);
        } catch (Exception e) {
            System.err.println("Impossibile caricare il pacchetto di lingua per " + locale.getLanguage() + ". Caricamento default.");
            messages = getBundle("assets.lang.messages", getDefault());
        }
    }

    public String getString(String key) {
        try {
            return messages.getString(key);
        } catch (Exception e) {
            return "!" + key + "!"; // Evidenzia chiavi mancanti
        }
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }
}
