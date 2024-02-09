package se.umu.mavo0072.thirtygame.models

/**
 * Data class for the score summary. Every round has its own ScoreHistoryModel,
 * which is saved in gameRepository, holding properties for statistics for each round
 */
data class ScoreHistoryModel( val scoreType: String?, val diceValues: List<Int>, val score: Int )