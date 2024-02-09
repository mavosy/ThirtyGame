package se.umu.mavo0072.thirtygame.utils

import se.umu.mavo0072.thirtygame.R

/**
 * Holding some current and future helper functions
 */
object UIUtilities {
    /**
     * Return the drawable resource ID based on the dice value
     */
     fun getDiceImageResource(value: Int): Int {
        return when (value) {
            1 -> R.drawable.dice_one_svg
            2 -> R.drawable.dice_two_svg
            3 -> R.drawable.dice_three_svg
            4 -> R.drawable.dice_four_svg
            5 -> R.drawable.dice_five_svg
            6 -> R.drawable.dice_six_svg
            else -> throw IllegalArgumentException("Invalid dice value, only value 1-6 is allowed")
        }
    }
}