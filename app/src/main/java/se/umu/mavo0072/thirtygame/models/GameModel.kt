package se.umu.mavo0072.thirtygame.models
import androidx.lifecycle.MutableLiveData
import se.umu.mavo0072.thirtygame.repository.GameRepository

class GameModel(private val gameRepository: GameRepository) {

    // Fields

    private val maxRollsPerRound = 3
    private val diceList: MutableList<DieModel> = MutableList(6) { DieModel() }
    private val scoreHistoryList = mutableListOf<ScoreHistoryModel>()
    private var roundNumber: Int = 1
    private var numberOfRolls: Int = 0
    private var totalScore: Int = 0
    private var roundScore: Int = 0

    val gameEndEvent = MutableLiveData<Boolean>()


    // Getters and setters to protect encapsulation

    fun getDiceList(): List<DieModel> = diceList.toList()

    fun getScoreHistoryList(): List<ScoreHistoryModel> = scoreHistoryList.toList()

    fun getRoundNumber(): Int = roundNumber

    fun getRollsLeft(): Int = 3 - numberOfRolls

    fun getTotalScore(): Int = totalScore

    fun getRoundScore(): Int = roundScore

    fun setDiceList(savedDiceList: List<DieModel>?) {
        if (savedDiceList != null) {
            diceList.clear()
            diceList.addAll(savedDiceList)
        }
    }

    fun setScoreHistoryList(savedStateHistoryList: List<ScoreHistoryModel>?) {
        if (savedStateHistoryList != null) {
            scoreHistoryList.clear()
            scoreHistoryList.addAll(savedStateHistoryList)
        }
    }

    fun setRoundNumber(roundNumber: Int) {
        this.roundNumber = roundNumber
    }

    fun setNumberOfRolls(rollsLeft: Int) {
        this.numberOfRolls = 3 - rollsLeft
    }

    fun setTotalScore(totalScore: Int) {
        this.totalScore = totalScore
    }

    fun setRoundScore(roundScore: Int) {
        this.roundScore = roundScore
    }

    /**
     * Rolling the dice in the array of dice
     */
    fun rollDice() {
        if (numberOfRolls < maxRollsPerRound) {
            diceList.forEach { it.roll() }
            numberOfRolls++
        } else if (numberOfRolls == maxRollsPerRound) {
            resetForNextRound()
        }
    }

    /**
     * Marking a die with a given index as kept from being rolled again,
     * and toggling back to rollable if already marked
     * @param index The index of the die to be saved
      */
    fun toggleSaveDie(index: Int) {
        if (index in diceList.indices) {
            diceList[index].isSaved = !diceList[index].isSaved
        }
        else {
            throw IllegalArgumentException("Invalid index for diceList, only value 0-5 is allowed")
        }
    }

    /**
     * Resets when next round button is pressed, resetting a number of properties,
     * or initialing gameEndEvent
     */
    fun resetForNextRound() {
        if (roundNumber < 10) {
            roundNumber++
            roundScore = 0
            numberOfRolls = 0
            diceList.forEach {
                it.isSaved = false
                it.isSelectedForScoring = false
                it.hasContributedToScore = false
            }
        } else {
            roundNumber = 0
            gameEndEvent.value = true
        }
    }


    private fun calculateScoreForDice(scoreType: String): Int {
        return when (scoreType){
            "low" -> calculateLowScore()
            else -> calculateSelectedDiceScore()
        }
    }

    /**
     * Calculating score using collection operations
     */
    private fun calculateLowScore(): Int {
        return diceList
            .filter { it.value <= 3 && !it.hasContributedToScore}
            .sumOf { it.value }
            .also { markDiceAsScored(diceList.filter { it.value <= 3 }) }
    }

    private fun calculateSelectedDiceScore(): Int {
        val selectedDice = diceList.filter { it.isSelectedForScoring}
        return selectedDice
            .filter { !it.hasContributedToScore }
            .sumOf { it.value }
            .also { markDiceAsScored(selectedDice) }
    }

    /**
     * Setting the score for the round and adds to total score
     */
    fun setScore(scoreType: String) {
        val score = calculateScoreForDice(scoreType)
        totalScore += score
        roundScore += score
        resetDiceSelectionForScoring()
    }

    private fun markDiceAsScored(dice: List<DieModel>) {
        dice.forEach { it.hasContributedToScore = true }
    }

    private fun resetDiceSelectionForScoring() {
        diceList.forEach { it.isSelectedForScoring = false }
    }

    /**
     * gathering data for the score summary and updates gameRepository
     */
    fun saveScoreHistory(roundScoreType: String?, roundScore: Int) {
        val roundDiceValues = diceList.map { it.value }
        scoreHistoryList.add(ScoreHistoryModel(roundScoreType, roundDiceValues, roundScore))
        gameRepository.saveScoreHistoryList(scoreHistoryList)
    }
}