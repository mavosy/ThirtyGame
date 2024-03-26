package se.umu.mavo0072.thirtygame.views
import android.content.Intent
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import org.koin.androidx.viewmodel.ext.android.viewModel
import se.umu.mavo0072.thirtygame.R
import se.umu.mavo0072.thirtygame.databinding.ActivityMainBinding
import se.umu.mavo0072.thirtygame.models.DieModel
import se.umu.mavo0072.thirtygame.utils.UIUtilities
import se.umu.mavo0072.thirtygame.viewmodels.GameViewModel

/**
 * Represents the main activity and view, handling UI interactions.
 */
class MainActivity : AppCompatActivity() {

    // lazy init of GameViewModel with Koin Dependency Injection delegate: "viewModel()"
    private val gameViewModel: GameViewModel by viewModel()

    // View Binding fields
    private lateinit var binding: ActivityMainBinding
    private lateinit var diceViews: List<ImageView>
    private lateinit var scoreButtons: List<Button>

    private lateinit var throwButton: Button
    private lateinit var continueButton: Button
    private lateinit var scoreMeButton: Button
    private lateinit var roundNumberText: TextView
    private lateinit var throwsLeft: TextView
    private lateinit var scoreSumDice: TextView
    private lateinit var scoreTotal: TextView
    private lateinit var tvInstructions: TextView


    companion object {
        private const val TAG = "MainActivity"
    }

    /**
     * Saves complex game variable's state upon stopping the activity.
     */
    override fun onStop() {
        super.onStop()
        gameViewModel.saveScoringTypeUsedMap()
        gameViewModel.saveDiceState()
        gameViewModel.saveScoreHistoryList()
    }

    /**
     * Initializes the activity, setting up view bindings, observers, listeners, and resetting game state if required.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeViewBindings()
        setupObservers()
        setupListeners()
    }

    /**
     * Initializing View bindings
     */
    private fun initializeViewBindings() {
        tvInstructions = binding.tvInstructions

        roundNumberText = binding.roundNumberText
        throwsLeft = binding.throwsLeftText

        scoreSumDice = binding.scoreSumDiceText
        scoreTotal = binding.scoreTotalText

        scoreMeButton = binding.btnScore
        throwButton = binding.btnThrow
        continueButton = binding.btnContinue

        diceViews = listOf(
            binding.diceView1,
            binding.diceView2,
            binding.diceView3,
            binding.diceView4,
            binding.diceView5,
            binding.diceView6,
        )

        scoreButtons = listOf(
            binding.btnLow,
            binding.btnFour,
            binding.btnFive,
            binding.btnSix,
            binding.btnSeven,
            binding.btnEight,
            binding.btnNine,
            binding.btnTen,
            binding.btnEleven,
            binding.btnTwelve,
        )
    }

    /**
     * Collection method for all listeners
     */
    private fun setupListeners() {
        setupDiceClickListeners()
        setupRollDiceListener()
        setupScoreButtonsListener()
        setupScoreMeButtonListener()
        setupContinueListener()
    }

    /**
     * Setting up listeners for clicking the dice images, toggling isSaved
     * and dice selection for scoring, depending on game state
      */
    private fun setupDiceClickListeners() {
        diceViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener{
                gameViewModel.getCurrentGameState().value?.let { gameState ->
                    when (gameState) {
                        GameViewModel.GameState.SAVING_DICE -> {
                            gameViewModel.toggleSaveDie(index)
                        }
                        GameViewModel.GameState.SCORING_DICE -> {
                            gameViewModel.toggleDiceSelectionForScoring(index)
                        }
                    }
                }
            }
        }
    }

    private fun setupRollDiceListener() {
        throwButton.setOnClickListener {
            gameViewModel.rollDice()
        }
    }

    /**
     * Listener for score buttons.
     * Clicking score button toggles game state, colors and boolean flags
     */
    private fun setupScoreButtonsListener() {
        scoreButtons.forEach { button ->
            button.setOnClickListener {
                val buttonScoreType = button.tag.toString()
                gameViewModel.onScoreSelectionChanged(buttonScoreType)
            }
        }
    }

    private fun setupScoreMeButtonListener() {
        scoreMeButton.setOnClickListener {
            gameViewModel.selectedScoringTypeLiveData.value?.let { tag ->
                gameViewModel.setScore(tag)
            }
        }
    }

    private fun setupContinueListener() {
        continueButton.setOnClickListener {
            gameViewModel.continueGame()
            tvInstructions.setText(R.string.roll_the_dice)
        }
    }

    /**
     * Collection method for all observers
     */
    private fun setupObservers(){
        observeDiceChanges()
        observeScoreTypeTextChange()
        observeTotalScore()
        observeRoundNumber()
        observeRollsLeft()
        observeGameState()
        observeScoringButtons()
        observeObserveGameEndEvent()
        observeScoringState()
    }

    // Observers, using Live data to update values in UI
    /**
     * Uses LiveData to update the changes to the dice everytime a value in diceList Changes
     */
    private fun observeDiceChanges() {
        gameViewModel.diceLiveData.observe(this) { diceList ->
            updateDiceDisplay(diceList)
            updateDiceEnabledState(diceList)
        }
    }
    private fun observeScoringState() {
        gameViewModel.sumOfSelectedDice.observe(this) { sum ->
            scoreSumDice.text = sum.toString()
        }

        gameViewModel.scoreMeButtonVisible.observe(this) { isVisible ->
            scoreMeButton.isVisible = isVisible
        }
    }

    private fun observeTotalScore() {
        gameViewModel.totalScoreLiveData.observe(this) { totalScore ->
            scoreTotal.text = totalScore.toString()
        }
    }

    private fun observeRoundNumber() {
        gameViewModel.roundNumberLiveData.observe(this) { roundNumber ->
            roundNumberText.text = roundNumber.toString()
        }
    }

    /**
     * Observes the number of rolls left in the current round.
     */
    private fun observeRollsLeft() {
        gameViewModel.rollsLeftLiveData.observe(this) { rollsLeft ->
            throwsLeft.text = rollsLeft.toString()
            val gameStateIsSavingDice = gameViewModel.getCurrentGameState().value == GameViewModel.GameState.SAVING_DICE
            if (rollsLeft == 0 && gameStateIsSavingDice) {
                tvInstructions.setText(R.string.choose_score_type)
            }
        }
    }

    private fun observeGameState() {
        val currentGameState = gameViewModel.getCurrentGameState()
        currentGameState.observe(this) { gameState ->
            handleGameState(gameState)
        }
    }

    private fun observeScoreTypeTextChange() {
        gameViewModel.scoreTypeTextResId.observe(this) { textResId ->
            tvInstructions.setText(textResId)
        }
    }

    private fun observeScoringButtons() {
        gameViewModel.scoringTypeUsedLiveData.observe(this) {
            updateScoringButtonsState()
        }

        gameViewModel.selectedScoringTypeLiveData.observe(this) {
            updateScoringButtonsState()
        }

        gameViewModel.hasScoredThisRound.observe(this) {
            updateScoringButtonsState()
        }
    }

    private fun observeObserveGameEndEvent() {
        gameViewModel.navigateToEndGame.observe(this) { gameEnded ->
            if (gameEnded) {
                navigateToEndGame()
            }
        }
    }

    /**
     * Updates the display of dice based on their current state.
     */
    private fun updateDiceDisplay(diceList: List<DieModel>) {
        diceViews.forEachIndexed { index, imageView ->
            val die = diceList[index]
            imageView.setImageResource(UIUtilities.getDiceImageResource(die.value))

            imageView.colorFilter = null

            when {
                die.hasContributedToScore -> imageView.setColorFilter(ContextCompat.getColor(this, R.color.dark_red), PorterDuff.Mode.SRC_IN)
                die.isSelectedForScoring -> imageView.setColorFilter(ContextCompat.getColor(this, R.color.dark_blue), PorterDuff.Mode.SRC_IN)
                die.isSaved -> imageView.setColorFilter(ContextCompat.getColor(this, R.color.dark_green), PorterDuff.Mode.SRC_IN)
                else -> imageView.clearColorFilter()
            }
        }
    }

    /**
     * Updates the enabled state of dice views based on if the dice has contributed to score this round.
     */
    private fun updateDiceEnabledState(diceList: List<DieModel>) {
        diceViews.forEachIndexed { index, imageView ->
            val die = diceList[index]
            imageView.isEnabled = die.let {
                when {
                    it.hasContributedToScore -> false
                    else -> true
                }
            }
        }
    }

    /**
     * Enables/disables throw button when game state changes
     */
    private fun handleGameState(gameState: GameViewModel.GameState?) {
        when (gameState) {
            GameViewModel.GameState.SAVING_DICE -> {
                throwButton.isEnabled = true
                gameViewModel.deselectAllDiceForScoring()
            }
            GameViewModel.GameState.SCORING_DICE -> {
                throwButton.isEnabled = false
            }

            else -> {
                throw NullPointerException("The game state was null")
            }
        }
    }

    /**
     * Updates the enabled state and appearance of scoring buttons.
     */
    private fun updateScoringButtonsState() {

        scoreButtons.forEach { button ->
            val buttonScoreType = button.tag.toString()
            val isScoringTypeUsed = gameViewModel.isScoringTypeUsedLive(buttonScoreType)
            val isSelected = buttonScoreType == gameViewModel.getSelectedScoringTypeLive()

            button.isEnabled = gameViewModel.isScoringButtonEnabled(buttonScoreType)

            Log.d(TAG, "Setting button $buttonScoreType isEnabled to ${button.isEnabled}")

            when {
                isSelected -> button.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_green))
                isScoringTypeUsed -> button.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_red))
                else -> button.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
            }
        }
    }

    /**
     * Initiates navigation to the game end summary activity.
     */
    private fun navigateToEndGame() {
        val intent = Intent(this, GameEndActivity::class.java)
        startActivity(intent)
        finish()
    }
}