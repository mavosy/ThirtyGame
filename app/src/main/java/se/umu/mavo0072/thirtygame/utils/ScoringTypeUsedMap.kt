package se.umu.mavo0072.thirtygame.utils

/**
 * A dictionary of score types and their usage.
 * If a score type is set to true, it has been used this game.
 */
class ScoringTypeUsedMap {
    private val usedScoringTypes = LinkedHashMap<String, Boolean>()
    private var currentRoundScoringType: String? = null

    init { initializeScoringTypeMap() }

    /**
     * Initializes the map with all scoring types set to unused.
     */
    private fun initializeScoringTypeMap() {
        val scoringTypes = listOf("low", "4", "5", "6", "7", "8", "9", "10", "11", "12")
        scoringTypes.forEach { scoringType ->
            usedScoringTypes[scoringType] = false
        }
    }

    fun getScoringTypeUsedMap(): LinkedHashMap<String, Boolean> = usedScoringTypes

    fun getCurrentRoundScoringType(): String? = currentRoundScoringType

    fun setCurrentRoundScoringTypeToNull() {
        currentRoundScoringType = null
    }

    /**
     * Marks a scoring type as used in the game.
     */
    fun setScoringTypeAsUsed(scoringType: String) {
        if (scoringType in usedScoringTypes) {
            usedScoringTypes[scoringType] = true
            currentRoundScoringType = scoringType
        }
    }

    /**
     * Updates the map with the states from the provided map map.
     */
    fun setUsedTypeStatesFromMap(map: LinkedHashMap<String, Boolean>) {
        usedScoringTypes.clear()
        usedScoringTypes.putAll(map)
    }

    fun isScoringTypeUsed(type: String): Boolean = usedScoringTypes[type] ?: false

    fun resetMap() {
        currentRoundScoringType = null
        usedScoringTypes.clear()
        initializeScoringTypeMap()
    }
}