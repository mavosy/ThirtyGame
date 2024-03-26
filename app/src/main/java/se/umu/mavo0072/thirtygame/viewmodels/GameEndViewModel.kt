package se.umu.mavo0072.thirtygame.viewmodels

import androidx.lifecycle.ViewModel
import se.umu.mavo0072.thirtygame.models.ScoreHistoryModel
import se.umu.mavo0072.thirtygame.repository.GameRepository

/**
 * Intermediary view model class for Score History, game statistics
 */
class GameEndViewModel(
    private val gameRepository: GameRepository
) : ViewModel(){

    fun getScoreHistoryList(): List<ScoreHistoryModel> = gameRepository.getScoreHistoryList()
}