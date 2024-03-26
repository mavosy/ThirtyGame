package se.umu.mavo0072.thirtygame.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import se.umu.mavo0072.thirtygame.JsonSerializer
import se.umu.mavo0072.thirtygame.R
import se.umu.mavo0072.thirtygame.models.DieModel
import se.umu.mavo0072.thirtygame.models.GameModel
import se.umu.mavo0072.thirtygame.models.ScoreHistoryModel
import se.umu.mavo0072.thirtygame.utils.ScoringTypeUsedMap

/**
 * Manages the state and interactions of the game, including dice rolls, scoring, and state persistence.
 */
class GameViewModel(
    private val gameModel: GameModel,
    private val scoringTypeUsedMap: ScoringTypeUsedMap,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    /**
     * TAG for debugging and logging
     */
    companion object {
        private const val TAG = "GameViewModel"
    }

    /**
     * Enum for holding game state
     */
    enum class GameState {
        SAVING_DICE, SCORING_DICE
    }

    // Live Data passing to MainActivity

    /**
     * Default game state = SAVING_DICE. GameState is saved to state handle in getter and setter of property
     */
    private val _currentGameState = MutableLiveData<GameState>().apply { value = GameState.SAVING_DICE }
    private val currentGameState: LiveData<GameState> = _currentGameState

    // saved in saveStateHandle
    private var _selectedScoringTypeLiveData: MutableLiveData<String?> = savedStateHandle.getLiveData("selectedScoringButton", null)
    val selectedScoringTypeLiveData: LiveData<String?> = _selectedScoringTypeLiveData

    // saved in saveStateHandle
    private val _diceLiveData: MutableLiveData<List<DieModel>> = MutableLiveData()
    val diceLiveData: LiveData<List<DieModel>> get() = _diceLiveData

    private val _scoreTypeTextResId = MutableLiveData(R.string.roll_the_dice)
    val scoreTypeTextResId: LiveData<Int> = _scoreTypeTextResId

    // saved in saveStateHandle as livedata
    private val _totalScoreLiveData = savedStateHandle.getLiveData("totalScore", 0)
    val totalScoreLiveData: LiveData<Int> = _totalScoreLiveData

    // saved in saveStateHandle as livedata
    private val _rollsLeftLiveData = savedStateHandle.getLiveData("rollsLeft", gameModel.getRollsLeft())
    val rollsLeftLiveData: LiveData<Int> = _rollsLeftLiveData

    // saved in saveStateHandle as livedata
    private val _roundNumberLiveData = savedStateHandle.getLiveData("roundNumber", 0)
    val roundNumberLiveData: LiveData<Int> = _roundNumberLiveData

    // saved in saveStateHandle as livedata
    private val _scoringTypeUsedLiveData = MutableLiveData<Map<String, Boolean>>()
    val scoringTypeUsedLiveData: LiveData<Map<String, Boolean>> = _scoringTypeUsedLiveData

    // saved in saveStateHandle as livedata
    private val _sumOfSelectedDice = savedStateHandle.getLiveData("sumOfSelectedDice", 0)
    val sumOfSelectedDice: LiveData<Int> = _sumOfSelectedDice

    // saved in saveStateHandle as livedata
    private val _scoreMeButtonVisible = savedStateHandle.getLiveData("scoreMeButtonVisible", false)
    val scoreMeButtonVisible: LiveData<Boolean> = _scoreMeButtonVisible

    // saved in saveStateHandle as livedata
    private val _hasScoredThisRound = savedStateHandle.getLiveData("hasScoredThisRound", false)
    val hasScoredThisRound: LiveData<Boolean> = _hasScoredThisRound

    val navigateToEndGame: LiveData<Boolean> = gameModel.gameEndEvent

    init {
        restoreDiceState()
        restoreGameState()
        restoreScoringTypeUsedMap()
        restoreScoreHistoryList()
        updateDiceList()
        updateScoringState()
        updateScoringTypeUsed()
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
        saveDiceState()
    }

    private fun updateScoringTypeUsed() {
        _scoringTypeUsedLiveData.value = scoringTypeUsedMap.getScoringTypeUsedMap().toMap()
        saveScoringTypeUsedMap()
    }

    /**
     * Updates the scoring button's availability based on the currently marked dice
     */
    private fun updateScoringState() {
        val sum = gameModel.getDiceList().filter { it.isSelectedForScoring }.sumOf { it.value }
        _sumOfSelectedDice.value = sum
        _scoreMeButtonVisible.value = isSumValidForScoringType(sum)
    }

    /**
     * Changes the selected scoring type and updates the game state.
     */
    fun onScoreSelectionChanged(tag: String?) {
        setSelectedScoringType(tag)
        setGameState(if (selectedScoringTypeLiveData.value == null) GameState.SAVING_DICE else GameState.SCORING_DICE)
    }

    private fun setSelectedScoringType(tag: String?) {
        _selectedScoringTypeLiveData.value = if (selectedScoringTypeLiveData.value == tag) null else tag
    }

    private fun getDie(index: Int) = gameModel.getDiceList()[index]

    /**
     * Toggles the saved state of a die based on its index.
     */
    fun toggleSaveDie(index: Int) {
        gameModel.toggleSaveDie(index)
        updateDiceList()
    }

    /**
     * Initiates a new dice roll if the game state allows.
     */
    fun rollDice() {
        if (currentGameState.value == GameState.SAVING_DICE) gameModel.rollDice()
        updateGameState()
        updateDiceList()
        updateRolls()
        updateRounds()
    }

    fun setScore(scoreType: String) {
        gameModel.setScore()
        updateScore()
        scoringTypeUsedMap.setScoringTypeAsUsed(scoreType)
        updateScoringTypeUsed()
        _scoreMeButtonVisible.value = false
        setHasScoredThisRound(true)
        updateDiceList()
    }

    fun getCurrentGameState(): LiveData<GameState> = currentGameState

    private fun updateGameState() {
        if (gameModel.getRollsLeft() == 0) {
            setGameState(GameState.SCORING_DICE)
        }
    }

    private fun setGameState(gameState: GameState) {
        _currentGameState.value = gameState
        savedStateHandle["CurrentGameState"] = gameState.name
        updateInstructionsForState(gameState)
    }

    private fun updateInstructionsForState(state: GameState) {
        _scoreTypeTextResId.value = when (state) {
            GameState.SCORING_DICE -> R.string.select_dice
            GameState.SAVING_DICE -> R.string.choose_score_type
        }
    }

    /**
     * Called when next round button is pushed, resetting and updating values,
     * randomizing dice and passing game statistics to gameModel
     */
    fun continueGame() {
        gameModel.saveScoreHistory(scoringTypeUsedMap.getCurrentRoundScoringType(), gameModel.getRoundScore())
        setSelectedScoringType(null)
        scoringTypeUsedMap.setCurrentRoundScoringTypeToNull()
        gameModel.resetForNextRound()
        setGameState(GameState.SAVING_DICE)
        setHasScoredThisRound(false)
        updateGameState()
        rollDice()
        updateRounds()
        updateDiceList()
        updateRolls()
        updateScoringState()
        updateScoringTypeUsed()
    }

    /**
     * Toggles the selection of a die for scoring.
     */
    fun toggleDiceSelectionForScoring(diceIndex: Int) {
        val die = getDie(diceIndex)
        die.isSelectedForScoring = !die.isSelectedForScoring
        updateDiceList()
        updateScoringState()
    }

    /**
     * Deselects all dice currently selected for scoring.
     */
    fun deselectAllDiceForScoring() {
        gameModel.resetDiceSelectionForScoring()
        updateDiceList()
        updateScoringState()
    }

    private fun isSumValidForScoringType(sum: Int): Boolean {
        val scoringType = _selectedScoringTypeLiveData.value
        return when (scoringType) {
            "low" -> sum in 1..3
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

    private fun setHasScoredThisRound(hasScored: Boolean) {
        _hasScoredThisRound.value = hasScored
    }

    fun isScoringButtonEnabled(scoringType: String): Boolean {
        return !(hasScoredThisRound.value == true || scoringTypeUsedMap.isScoringTypeUsed(scoringType))
    }

    fun isScoringTypeUsedLive(type: String): Boolean {
        return scoringTypeUsedLiveData.value?.get(type) ?: false
    }

    fun getSelectedScoringTypeLive(): String? {
        return selectedScoringTypeLiveData.value
    }

    // Methods for saving and restoring state

    fun saveDiceState() {
        val diceList = _diceLiveData.value
        savedStateHandle["diceList"] = JsonSerializer.serialize(diceList)
    }

    private fun restoreDiceState() {
        savedStateHandle.get<String>("diceList")?.let {
            val deserializedDiceList = JsonSerializer.deserialize<List<DieModel>>(it)
            _diceLiveData.value = deserializedDiceList
            gameModel.setDiceList(deserializedDiceList)
        }
    }

    fun saveScoringTypeUsedMap() {
        savedStateHandle["scoringTypeUsedMap"] = JsonSerializer.serialize(scoringTypeUsedMap.getScoringTypeUsedMap())
    }

    private fun restoreScoringTypeUsedMap() {
        savedStateHandle.get<String>("scoringTypeUsedMap")?.let {
            val deserializedUsedMap = JsonSerializer.deserialize<LinkedHashMap<String, Boolean>>(it)
            scoringTypeUsedMap.setUsedTypeStatesFromMap(deserializedUsedMap)
        }
    }

    fun saveScoreHistoryList() {
        savedStateHandle["scoringHistoryList"] = JsonSerializer.serialize(gameModel.getScoreHistoryList())
    }

    private fun restoreScoreHistoryList() {
        savedStateHandle.get<String>("scoringHistoryList")?.let {
            val deserializedScoreHistoryList = JsonSerializer.deserialize<List<ScoreHistoryModel>>(it)
            gameModel.setScoreHistoryList(deserializedScoreHistoryList)
        }
    }

    private fun restoreGameState() {
        _currentGameState.value = savedStateHandle.get<String>("CurrentGameState")?.let {
            try {
                GameState.valueOf(it)
            } catch (e: IllegalArgumentException) {
                GameState.SAVING_DICE
            }
        } ?: GameState.SAVING_DICE
    }
}