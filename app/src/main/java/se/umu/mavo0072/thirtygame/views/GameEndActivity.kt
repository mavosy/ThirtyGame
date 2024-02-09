package se.umu.mavo0072.thirtygame.views

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import org.koin.androidx.viewmodel.ext.android.viewModel
import se.umu.mavo0072.thirtygame.R
import se.umu.mavo0072.thirtygame.databinding.ActivityGameEndBinding
import se.umu.mavo0072.thirtygame.viewmodels.GameEndViewModel

class GameEndActivity : AppCompatActivity() {

    // Fields

    private val gameEndViewModel: GameEndViewModel by viewModel()
    private lateinit var binding: ActivityGameEndBinding
    private lateinit var startNewGameButton: Button
    private lateinit var scoreHistoryContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameEndBinding.inflate(layoutInflater)
        setContentView(binding.root)

        scoreHistoryContainer = binding.scoreHistoryContainer
        startNewGameButton = binding.btnStartNewGame
        setupNewGameListener()

        showScoreHistory()
    }

    /**
     * Programmatically generating containers and string holders
     * for game statistics from GameRepository and ScoreHistoryModel,
     * and publishing this in UI View at the end of the game
     */
    private fun showScoreHistory() {
        gameEndViewModel.getScoreHistoryList().let { historyList ->
            val totalGameScore = historyList.sumOf { it.score }
            val totalScoreView = TextView(this).apply {
                text = buildString {
                    append("Nice job! Your total score was: ${totalGameScore}\n")
                }
                textSize = 22f
                typeface = Typeface.DEFAULT_BOLD
            }
            val summaryPresenter = TextView(this).apply {
                text = buildString {
                    append("Here's how you performed each round:\n")
                }
                textSize = 20f
            }
            scoreHistoryContainer.addView(totalScoreView)
            scoreHistoryContainer.addView(summaryPresenter)

            historyList.forEachIndexed { index, history ->
                val roundView = TextView(this).apply {
                    text = buildString {
                        append("Round ${index + 1}\n")
                        append("Score type: ${history.scoreType}\n")
                        append("Dice: ${history.diceValues.joinToString(", ")}\n")
                        append("Score: ${history.score}\n")
                        append("Total score: ${history.score}\n")
                    }
                    textSize = 20f
                }
                binding.scoreHistoryContainer.addView(roundView)
            }
            val spacerView = TextView(this).apply {
                text = context.getString(R.string.spacesThree)
                textSize = 20f
            }
            binding.scoreHistoryContainer.addView(spacerView)
        }
    }

    private fun setupNewGameListener() {
        startNewGameButton.setOnClickListener {
            startNewGame()
        }
    }

    /**
     * Starting a new game and switching to Main Activity, closing game loop
     * TODO - needs to either have GameModel as singleton and handle all resets manually
     * TODO - or GameModel as factory and make savedStateHandle work better
     */
    private fun startNewGame() {
        val intent = Intent(this, MainActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}