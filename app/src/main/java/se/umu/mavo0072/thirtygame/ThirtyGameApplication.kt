package se.umu.mavo0072.thirtygame

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import se.umu.mavo0072.thirtygame.models.GameModel
import se.umu.mavo0072.thirtygame.repository.GameRepository
import se.umu.mavo0072.thirtygame.utils.ScoringTypeUsedMap
import se.umu.mavo0072.thirtygame.viewmodels.GameEndViewModel
import se.umu.mavo0072.thirtygame.viewmodels.GameViewModel

class ThirtyGameApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.NONE)
            androidContext(this@ThirtyGameApplication)
            modules(appModule)
        }
    }
}

// Defining dependencies for Koin DI
val appModule = module {
    // viewModel instance of GameModel, enabled to get SavedStateHandle, GameModel and ScoringTypeUsedMap in constructor
    viewModel { GameViewModel( get(), get()) }
    // viewModel instance of GameModel, enabled to get GameRepository in constructor
    viewModel { GameEndViewModel(get()) }
    // singleton instance of GameModel
    factory { GameModel(get()) }
    // singleton instance of ScoringTypeUsedMap
    single { ScoringTypeUsedMap() }
    // singleton instance of GameRepository
    single { GameRepository() }
}