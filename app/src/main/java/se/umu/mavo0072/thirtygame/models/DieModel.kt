package se.umu.mavo0072.thirtygame.models

/**
 * Holds properties of a die and the function to roll it.
 */
data class DieModel(
    var value: Int = (1..6).random(),
    var isSaved: Boolean = false,
    var isSelectedForScoring: Boolean = false,
    var hasContributedToScore: Boolean = false
) {

    /**
     * Rolls the die to a new random value between 1 and 6 if the die is not marked as saved.
     *
     */
    fun roll() {
        if (!isSaved) {
            value = (1..6).random()
        }
    }
}