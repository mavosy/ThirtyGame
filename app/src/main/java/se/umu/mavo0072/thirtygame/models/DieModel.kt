package se.umu.mavo0072.thirtygame.models

/**
 * Holds properties of a die
 */
data class DieModel(
    var value: Int = (1..6).random(),
    var isSaved: Boolean = false,
    var isSelectedForScoring: Boolean = false,
    var hasContributedToScore: Boolean = false
) {
    fun roll() {
        if (!isSaved) {
            value = (1..6).random()
        }
    }
}