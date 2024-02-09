package se.umu.mavo0072.thirtygame.views
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

    // Field holder for selected scoring button
    private var selectedScoringButton: Button? = null

    /**
     * Main initializer. Initializing all view binding, setting up observers and listeners
      */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeViewBindings()
        setupObservers()
        setupListeners()
        setDisableButtons()
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
            setScoreButtonsEnabled(true)
            continueButton.isVisible = true
        }
    }

    /**
     * Listener for score buttons.
     * Clicking score button toggles game state, colors and boolean flags
     */
    private fun setupScoreButtonsListener() {
        scoreButtons.forEach { button ->
            button.setOnClickListener {
                if (selectedScoringButton == it) {
                    selectedScoringButton = null
//                    gameViewModel.setSelectedScoringType(button.tag.toString())
                    gameViewModel.setGameState(GameViewModel.GameState.SAVING_DICE)
                    it.isSelected = false
                    it.setBackgroundColor(getColor(R.color.black))
                    gameViewModel.onScoringTypeSelected(true)
                } else {
                    if (!gameViewModel.isScoringTypeUsed(selectedScoringButton?.tag.toString())){
                        selectedScoringButton?.setBackgroundColor(getColor(R.color.black))
                    }
                    selectedScoringButton?.isSelected = false
                    selectedScoringButton = it as Button
                    gameViewModel.setGameState(GameViewModel.GameState.SCORING_DICE)
                    it.isSelected = true
                    it.setBackgroundColor(getColor(R.color.dark_green))
                    gameViewModel.setSelectedScoringType(button.tag.toString())
                    gameViewModel.onScoringTypeSelected(true)
                }
            }
        }
    }

    private fun setupScoreMeButtonListener() {
        scoreMeButton.setOnClickListener {
            val currentScoringType = gameViewModel.getSelectedScoringType()
            if (currentScoringType != null) {
                gameViewModel.setScore(currentScoringType)
            }
            setScoreButtonsEnabled(false)
            scoreMeButton.isVisible = false
            updateDiceDisplay(gameViewModel.getDiceList())
        }
    }

    private fun setupContinueListener() {
        continueButton.setOnClickListener {
            gameViewModel.continueGame()
            selectedScoringButton?.isSelected = false
            tvInstructions.setText(R.string.text_instructions)
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
        observeScoringTypeUsed()
        observeObserveGameEndEvent()
    }

    /**
     * Updates the dice images in UI to reflect the value of the dice
     */
    private fun updateDiceDisplay(diceList: List<DieModel>) {
        diceViews.forEachIndexed { index, imageView ->
            val die = diceList[index]
            imageView.setImageResource(UIUtilities.getDiceImageResource(die.value))
            if (die.hasContributedToScore) {
                imageView.setColorFilter(ContextCompat.getColor(this, R.color.dark_red))
                imageView.isEnabled = false
            } else if (die.isSelectedForScoring) {
                imageView.setColorFilter(ContextCompat.getColor(this, R.color.dark_blue))
            } else if ( die.isSaved ) {
                imageView.setColorFilter(ContextCompat.getColor(this, R.color.dark_green))
            } else {
                imageView.setColorFilter(ContextCompat.getColor(this, R.color.default_Dice_Color))
                imageView.isEnabled = true
            }
        }
    }

    // Observers, using Live data to update values in UI

    /**
     * Uses LiveData to update the changes to the dice everytime a value in diceList Changes
     */
    private fun observeDiceChanges() {
        gameViewModel.diceLiveData.observe(this) { diceList: List<DieModel> ->
            updateDiceDisplay(diceList)

            val sumOfSelectedDice = diceList.filter { it.isSelectedForScoring }.sumOf { it.value }
            val canScore = gameViewModel.isSumValidForScoringType(sumOfSelectedDice)
            scoreSumDice.text = sumOfSelectedDice.toString()
            scoreMeButton.isVisible = canScore
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

    private fun observeRollsLeft() {
        gameViewModel.rollsLeftLiveData.observe(this) { rollsLeft ->
            throwsLeft.text = rollsLeft.toString()
            if (rollsLeft == 0) {
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

    private fun observeScoringTypeUsed() {
        gameViewModel.scoringTypeUsedLiveData.observe(this) { scoringTypeUsed ->
            scoreButtons.forEach { button ->
                val isScoringTypeUsed = scoringTypeUsed[button.tag.toString()] == true
                if (isScoringTypeUsed) {
                    button.setBackgroundColor(getColor(R.color.dark_red))
                    button.isEnabled = false
                }
            }
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
     * Enables/disables buttons when game state changes
     */
    private fun handleGameState(gameState: GameViewModel.GameState?) {
        when (gameState) {
            GameViewModel.GameState.SAVING_DICE -> {
                throwButton.isEnabled = true
            }
            GameViewModel.GameState.SCORING_DICE -> {
                throwButton.isEnabled = false
            }

            else -> {
                throw NullPointerException("The game state was null")
            }
        }
    }

    private fun setDisableButtons() {
        setScoreButtonsEnabled(false)
    }

    /**
     * Setting each scoreButton to enabled or not, based on boolean flag
     */
    private fun setScoreButtonsEnabled(enabled: Boolean){
        scoreButtons.forEach { button ->
            if (button.isEnabled == !enabled) {
                val scoreType = button.tag.toString()
                val isUsed = gameViewModel.isScoringTypeUsed(scoreType)
                if (!isUsed) button.isEnabled = enabled
            }
        }
    }

    /**
     * Navigates to
     */
    private fun navigateToEndGame() {
        val intent = Intent(this, GameEndActivity::class.java)
        startActivity(intent)
        finish()
    }
}
