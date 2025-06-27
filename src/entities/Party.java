package entities;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Party {

    private final List<Pokemon> party;
    private final String fileName;

    public Party(List<Pokemon> party, String fileName) {
        this.party = party;
        this.fileName = fileName;
    }

    public Party(String fileName) throws IOException {
        this.fileName = fileName;
        this.party = load();
    }

    public boolean save() {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter("src/assets/saves/" + this.fileName))) {
            for (Pokemon p : this.party) {
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
            return false;
        }
    }

    // Ci sono delle discrepanze nel design, a volte stampi l'eccezione, a volte no.
    // In mancanza di ulteriore contesto e linee guide esplicite
    // ho fornito due diversi metodi per il loading del party.
    public List<Pokemon> load() throws IOException {
        if (this.party != null) {
            this.party.clear();
        }
        List<Pokemon> returnList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/assets/saves/" + this.fileName))) {
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

                    Pokemon pokemon = new Pokemon(name, level, maxHp, attack, speed, speed);
                    pokemon.setCurrentHp(currentHp);
                    returnList.add(pokemon);
                }
            }
        }
        return returnList;
    }

    public List<Pokemon> loadOrThrow(RuntimeException exception) throws IOException {
        try {
            return load();
        } catch (IOException e) {
            throw exception;
        }
    }

    public Pokemon getFirst() {
        return this.party.getFirst();
    }

    public int getSize() {
        return this.party.size();
    }

    public boolean addPokemon(Pokemon pokemon) {
        if (isFull()) {
            return false;
        }
        this.party.add(pokemon);
        return true;
    }

    public void swap(int index1, int index2) {
        if (index1 < 0 || index1 >= this.party.size() || index2 < 0 || index2 >= this.party.size()) {
            throw new IndexOutOfBoundsException("Index out of range");
        }
        Pokemon temp = this.party.get(index1);
        this.party.set(index1, this.party.get(index2));
        this.party.set(index2, temp);
    }

    public void swap(Pokemon p1, Pokemon p2) {
        int i1 = this.party.indexOf(p1);
        int i2 = this.party.indexOf(p2);
        if (i1 == -1 || i2 == -1) {
            throw new IllegalArgumentException("Pokemon not found in party");
        }
        swap(i1, i2);
    }

    public boolean isFull() {
        return party.size() <= 6;
    }

    public boolean isEmpty() {
        return this.party.isEmpty();
    }

    public List<Pokemon> getExplicitParty() {
        return this.party;
    }
}
