package engine;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationManager {

    // 1. L'unica istanza della classe (Singleton)
    private static LocalizationManager instance;

    private ResourceBundle messages;
    private Locale currentLocale;

    // 2. Costruttore privato per impedire la creazione di altre istanze
    private LocalizationManager() {
        // Impostiamo una lingua di default all'avvio
        setLocale(new Locale("en", "US"));
    }

    // 3. Metodo pubblico per ottenere l'unica istanza
    public static LocalizationManager getInstance() {
        if (instance == null) {
            instance = new LocalizationManager();
        }
        return instance;
    }

    // 4. Metodo per caricare un pacchetto di traduzioni
    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        try {
            // Java cercherà i file "messages_xx_XX.properties" nel classpath.
            // Il percorso "assets.lang.messages" corrisponde a "src/assets/lang/messages"
            messages = ResourceBundle.getBundle("assets.lang.messages", currentLocale);
        } catch (Exception e) {
            System.err.println("Impossibile caricare il pacchetto di lingua per " + locale.getLanguage() + ". Caricamento default.");
            // Se fallisce, carica la lingua di default del sistema
            messages = ResourceBundle.getBundle("assets.lang.messages", Locale.getDefault());
        }
    }

    // 5. Metodo per ottenere una stringa tradotta tramite la sua chiave
    public String getString(String key) {
        try {
            return messages.getString(key);
        } catch (Exception e) {
            // Se la chiave non viene trovata, restituiamo un messaggio di errore
            // per rendere evidente il problema durante il debug.
            return "!" + key + "!";
        }
    }
    
    public Locale getCurrentLocale() {
        return currentLocale;
    }
}