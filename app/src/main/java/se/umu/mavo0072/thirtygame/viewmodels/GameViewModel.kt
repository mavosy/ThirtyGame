package se.umu.mavo0072.thirtygame.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import se.umu.mavo0072.thirtygame.R
import se.umu.mavo0072.thirtygame.models.DieModel
import se.umu.mavo0072.thirtygame.models.GameModel
import se.umu.mavo0072.thirtygame.utils.ScoringTypeUsedMap

class GameViewModel(private val gameModel: GameModel, private val scoringTypeUsedMap: ScoringTypeUsedMap) : ViewModel() {

    /**
     * Enum for holding game state
     * TODO Check if this is necessary, might either expand or remove
     */
    enum class GameState {
        SAVING_DICE, SCORING_DICE
    }

    /**
     * Holds a list of dice chosen by user for scoring
     * TODO refactor to only use DieModel property isSelectedForScoring instead
     */
    private val selectedDiceForScoring = mutableListOf<DieModel>()

    private var selectedScoringType: String? = null

    // Live Data passing to MainActivity

    /**
     * Default game state = SAVING_DICE
      */
    private val currentGameState = MutableLiveData(GameState.SAVING_DICE)

    private val _diceLiveData = MutableLiveData<List<DieModel>>()
    val diceLiveData: LiveData<List<DieModel>> = _diceLiveData

    private val _scoreTypeTextResId = MutableLiveData(R.string.text_instructions)
    val scoreTypeTextResId: LiveData<Int> = _scoreTypeTextResId

    private val _totalScoreLiveData = MutableLiveData<Int>()
    val totalScoreLiveData: LiveData<Int> = _totalScoreLiveData

    private val _rollsLeftLiveData = MutableLiveData<Int>()
    val rollsLeftLiveData: LiveData<Int> = _rollsLeftLiveData

    private val _roundNumberLiveData = MutableLiveData<Int>()
    val roundNumberLiveData: LiveData<Int> = _roundNumberLiveData

    private val _scoringTypeUsedLiveData = MutableLiveData<MutableMap<String, Boolean>>()
    val scoringTypeUsedLiveData: LiveData<MutableMap<String, Boolean>> = _scoringTypeUsedLiveData

    val navigateToEndGame: LiveData<Boolean> = gameModel.gameEndEvent

    init {
        updateDiceList()
    }

    // Updates Live Data with latest values

    private fun updateScore() {
        _totalScoreLiveData.value = gameModel.getTotalScore()
    }

    private fun updateRolls() {
        _rollsLeftLiveData.value = gameModel.getRollsLeft()
    }

    private fun updateRounds() {
        _roundNumberLiveData.value = gameModel.getRoundNumber()
    }

    private fun updateDiceList() {
        _diceLiveData.value = gameModel.getDiceList()
    }

    private fun updateScoringTypeUsed() {
        _scoringTypeUsedLiveData.value = scoringTypeUsedMap.getScoringTypeUsedMap()
    }

    private fun getDie(index: Int) = gameModel.getDiceList()[index]
    fun getDiceList(): List<DieModel> = gameModel.getDiceList()

    fun toggleSaveDie(index: Int) {
        gameModel.toggleSaveDie(index)
        updateDiceList()
    }

    // Can only roll dice in SAVING_DICE game state
    fun rollDice() {
        if (currentGameState.value == GameState.SAVING_DICE) gameModel.rollDice()
        updateGameState()
        updateDiceList()
        updateRolls()
        updateRounds()
    }

    fun setScore(scoreType: String) {
        gameModel.setScore(scoreType)
        scoringTypeUsedMap.setScoringTypeAsUsed(scoreType)
        updateScore()
        updateScoringTypeUsed()
    }

    fun getCurrentGameState(): LiveData<GameState> = currentGameState

    private fun updateGameState() {
        if (gameModel.getRollsLeft() == 0) {
            setGameState(GameState.SCORING_DICE)
        }
    }

    fun setGameState(state: GameState) {
        currentGameState.value = state
    }

    /**
     * Called when next round button is pushed, resetting and updating values,
     * randomizing dice and passing game statistics to gameModel
     */
    fun continueGame() {
        gameModel.saveScoreHistory(scoringTypeUsedMap.getLastScoringType(), gameModel.getRoundScore())
        setSelectedScoringType(null)
        gameModel.resetForNextRound()
        setGameState(GameState.SAVING_DICE)
        updateGameState()
        rollDice()
        updateRounds()
        updateDiceList()
        updateRolls()
    }

    fun toggleDiceSelectionForScoring(diceIndex: Int) {
        val die = getDie(diceIndex)
        if (die.isSelectedForScoring) {
            die.isSelectedForScoring = false
            selectedDiceForScoring.remove(die)
        } else {
            die.isSelectedForScoring = true
            selectedDiceForScoring.add(die)
        }
        updateDiceList()
    }

    fun isSumValidForScoringType(sum: Int): Boolean {
        return when (selectedScoringType) {
            "low" -> sum <= 3
            "4" -> sum == 4
            "5" -> sum == 5
            "6" -> sum == 6
            "7" -> sum == 7
            "8" -> sum == 8
            "9" -> sum == 9
            "10" -> sum == 10
            "11" -> sum == 11
            "12" -> sum == 12
            else -> false
        }
    }

    /**
     * Changing instructions text
     */
    fun onScoringTypeSelected(isSelected: Boolean) {
        _scoreTypeTextResId.value = if (isSelected) {
            R.string.select_dice
        } else {
            R.string.choose_score_type
        }
    }

    fun isScoringTypeUsed(type: String): Boolean = scoringTypeUsedMap.isScoringTypeUsed(type)

    fun setSelectedScoringType(scoreType: String?) {
        selectedScoringType = scoreType
    }

    fun getSelectedScoringType(): String? {
        return selectedScoringType
    }
}