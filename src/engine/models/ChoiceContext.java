package engine.models;

import java.util.List;

/**
 * Questa classe contiene tutte le informazioni necessarie per presentare
 * una scelta all'utente e gestire il risultato.
 */
public class ChoiceContext {

    private final String question;
    private final List<String> options;
    private final List<Runnable> actions;
    private final Runnable cancelAction; // Azione da eseguire se l'utente annulla (es. preme X)

    /**
     * Costruttore per il contesto di una scelta.
     * @param question La domanda da visualizzare.
     * @param options La lista di stringhe per le opzioni (es. "Sì", "No").
     * @param actions La lista di azioni (Runnable) corrispondenti. Deve avere la stessa dimensione di 'options'.
     * @param cancelAction L'azione da eseguire se l'utente annulla la scelta.
     */
    public ChoiceContext(String question, List<String> options, List<Runnable> actions, Runnable cancelAction) {
        if (options.size() != actions.size()) {
            throw new IllegalArgumentException("La lista delle opzioni e delle azioni devono avere la stessa dimensione!");
        }
        this.question = question;
        this.options = options;
        this.actions = actions;
        this.cancelAction = cancelAction;
    }
    
    // Metodi "getter" per accedere ai dati dall'esterno (dallo ChoiceState)

    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    public Runnable getAction(int index) {
        return actions.get(index);
    }
    
    public Runnable getCancelAction() {
        return cancelAction;
    }
}