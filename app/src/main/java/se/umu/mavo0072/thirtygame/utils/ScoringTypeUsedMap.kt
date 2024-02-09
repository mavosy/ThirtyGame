package se.umu.mavo0072.thirtygame.utils

/**
 * A dictionary of score types and their use.
 * If a score type is set to true, it has been used this game
 */
class ScoringTypeUsedMap {
    private val usedScoringTypes = LinkedHashMap<String, Boolean>()

    init { initializeScoringTypeMap() }

    private fun initializeScoringTypeMap() {
        val scoringTypes = listOf("Low", "4", "5", "6", "7", "8", "9", "10", "11", "12")
        scoringTypes.forEach { scoringType ->
            usedScoringTypes[scoringType] = false
        }
    }

    fun getUsedStatesList(): List<Boolean> {
        return usedScoringTypes.values.toList()
    }

    fun getLastScoringType(): String? = usedScoringTypes.entries.lastOrNull()?.key

    fun setScoringTypeAsUsed(scoringType: String) {
        if (scoringType in usedScoringTypes) {
            usedScoringTypes[scoringType] = true
        }
    }

    /**
     * matching keys against index to repopulate savedStateHandle data
     * TODO implement better saved state logic
     */
    fun setUsedTypeStatesFromSavedState(usedTypeStates: List<Boolean>?) {
        if (usedTypeStates != null)
            usedScoringTypes.keys.forEachIndexed { index, key ->
                usedScoringTypes[key] = usedTypeStates[index]
        }
    }

    fun getScoringTypeUsedMap(): LinkedHashMap<String, Boolean> {
        return usedScoringTypes
    }

    fun isScoringTypeUsed(type: String): Boolean = usedScoringTypes[type] ?: false
}