package se.umu.mavo0072.thirtygame.repository

import se.umu.mavo0072.thirtygame.models.ScoreHistoryModel

/**
 * Holding a list of scoreHistoryModel, statistics for each round
 */
class GameRepository {
    private var scoreHistoryList: List<ScoreHistoryModel> = listOf()

    fun saveScoreHistoryList(scores: List<ScoreHistoryModel>) {
        scoreHistoryList = scores
    }

    fun getScoreHistoryList(): List<ScoreHistoryModel> = scoreHistoryList
}