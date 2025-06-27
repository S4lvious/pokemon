package entities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Party {

    private final List<Pokemon> pokemonList;
    private final String saveFileName;

    public Party(String saveFileName) {
        this.pokemonList = new ArrayList<>();
        this.saveFileName = saveFileName;
    }
    
    public void swap(int index1, int index2) {
        if (index1 < 0 || index1 >= pokemonList.size() || index2 < 0 || index2 >= pokemonList.size()) {
            // Potremmo lanciare un'eccezione, ma per ora non facciamo nulla per sicurezza.
            System.err.println("Indici per lo scambio fuori dai limiti.");
            return;
        }
        // La classe Collections di Java ha un metodo molto comodo per scambiare elementi in una lista.
        Collections.swap(pokemonList, index1, index2);
    }

    // Metodi di gestione della lista

    public boolean addPokemon(Pokemon pokemon) {
        if (isFull()) {
            return false;
        }
        return this.pokemonList.add(pokemon);
    }

    public Pokemon getPokemon(int index) {
        return this.pokemonList.get(index);
    }
    
    public Pokemon getFirstAvailablePokemon() {
        for (Pokemon p : pokemonList) {
            if (!p.isFainted()) {
                return p;
            }
        }
        return null; // O il primo, se sono tutti esausti
    }

    public int getSize() {
        return this.pokemonList.size();
    }

    public boolean isFull() {
        return this.pokemonList.size() >= 6;
    }

    // Restituisce una copia non modificabile della lista per la visualizzazione,
    // proteggendo la lista originale (buona pratica di incapsulamento).
    public List<Pokemon> getPokemonList() {
        return Collections.unmodifiableList(pokemonList);
    }
    
    // Metodi di Salvataggio e Caricamento

    public void loadFromFile() {
        this.pokemonList.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/assets/saves/" + this.saveFileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String name = parts[0];
                    int level = Integer.parseInt(parts[1]);
                    int maxHp = Integer.parseInt(parts[2]);
                    int currentHp = Integer.parseInt(parts[3]);
                    int attack = Integer.parseInt(parts[4]);
                    int speed = Integer.parseInt(parts[5]);

                    Pokemon p = new Pokemon(name, level, maxHp, attack, 0, speed); // defense a 0 momentaneamente
                    p.setCurrentHp(currentHp);
                    addPokemon(p);
                }
            }
            System.out.println("Squadra caricata da: " + this.saveFileName);
        } catch (IOException e) {
            System.err.println("Errore nel caricare la squadra: " + e.getMessage());
            // Potremmo voler creare una squadra di default se il file non esiste
        }
    }

    public boolean saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/assets/saves/" + this.saveFileName))) {
            for (Pokemon p : this.pokemonList) {
                String line = String.join(",",
                    p.getName(),
                    String.valueOf(p.getLevel()),
                    String.valueOf(p.getMaxHp()),
                    String.valueOf(p.getCurrentHp()),
                    String.valueOf(p.getAttack()),
                    String.valueOf(p.getSpeed())
                );
                writer.write(line);
                writer.newLine();
            }
            return true;
        } catch (Exception e) {
            System.err.println("Errore nel salvare la squadra: " + e.getMessage());
            return false;
        }
    }
}