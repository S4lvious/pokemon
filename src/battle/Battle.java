package battle;

import entities.Pokemon;

public final class Battle {

    private final Pokemon playerPokemon;
    private final Pokemon enemyPokemon;
    private boolean battleOver;

    public Battle(Pokemon playerPokemon, Pokemon enemyPokemon) {
        this.playerPokemon = playerPokemon;
        this.enemyPokemon = enemyPokemon;
        this.battleOver = false;
    }

    public String playerAttack() {
        if (battleOver) return "La battaglia è già finita.";

        int damage = playerPokemon.calculateDamage(enemyPokemon);
        enemyPokemon.takeDamage(damage);
        checkBattleOver();

        return formatAttackMessage(playerPokemon, enemyPokemon, damage);
    }

    public String enemyAttack() {
        if (battleOver) return "La battaglia è già finita.";

        int damage = enemyPokemon.calculateDamage(playerPokemon);
        playerPokemon.takeDamage(damage);
        checkBattleOver();

        return formatAttackMessage(enemyPokemon, playerPokemon, damage);
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
            if (!playerPokemon.isFainted()) {
                log.append(playerAttack()).append("\n");
            }
        }

        return log.toString().trim();
    }

    private void checkBattleOver() {
        battleOver = playerPokemon.isFainted() || enemyPokemon.isFainted();
    }

    private String formatAttackMessage(Pokemon attacker, Pokemon defender, int damage) {
        return attacker.getName() + " infligge " + damage + " danni a " + defender.getName() + "!";
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
