package battle;

import entities.Pokemon;

public class Battle {
    private final Pokemon playerPokemon;
    private final Pokemon enemyPokemon;
    private boolean battleOver = false;

    public Battle(Pokemon playerPokemon, Pokemon enemyPokemon) {
        this.playerPokemon = playerPokemon;
        this.enemyPokemon = enemyPokemon;
    }

    public String playerAttack() {
        if (battleOver) return "La battaglia è già finita.";
        int damage = playerPokemon.calculateDamage(enemyPokemon);
        enemyPokemon.takeDamage(damage);
        checkBattleOver();
        return playerPokemon.getName() + " infligge " + damage + " danni a " + enemyPokemon.getName() + "!";
    }


    public String performTurn() {
        if (battleOver) return "La battaglia è finita.";

        StringBuilder log = new StringBuilder();
        boolean playerFirst = playerPokemon.getSpeed() >= enemyPokemon.getSpeed();
        if (playerFirst) {
            log.append(playerAttack()).append("\n");
            if (!enemyPokemon.isFainted()) {
                log.append(enemyAttack()).append("\n");
            }
        } else {
            log.append(enemyAttack()).append("\n");
            if (!enemyPokemon.isFainted()) {
                log.append(playerAttack()).append("\n");
            }
        }
        return log.toString();
    }

    public String enemyAttack() {
        if (battleOver) return "La battaglia è già finita.";
        int damage = enemyPokemon.calculateDamage(playerPokemon);
        playerPokemon.takeDamage(damage);
        checkBattleOver();
        return enemyPokemon.getName() + " infligge " + damage + " danni a " + playerPokemon.getName() + "!";
    }

    private void checkBattleOver() {
        if (enemyPokemon.isFainted() || playerPokemon.isFainted()) {
            battleOver = true;
        }
    }

    public boolean isBattleOver() {
        return battleOver;
    }

    public Pokemon getPlayerPokemon() {
        return playerPokemon;
    }

    public Pokemon getEnemyPokemon() {
        return enemyPokemon;
    }
}
