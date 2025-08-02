// ui/home/HomeFragment.kt
package com.example.hiraganakatakana.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.hiraganakatakana.R
import com.example.hiraganakatakana.databinding.FragmentHomeBinding
import com.example.hiraganakatakana.ui.main.MainViewModel
import com.example.hiraganakatakana.utils.SharedPreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    @Inject
    lateinit var sharedPrefsManager: SharedPreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
        updateUI()
    }

    private fun setupObservers() {
        viewModel.hiraganaLearnedCount.observe(viewLifecycleOwner) { count ->
            binding.hiraganaProgress.progress = count
        }

        viewModel.katakanaLearnedCount.observe(viewLifecycleOwner) { count ->
            binding.katakanaProgress.progress = count
        }

        viewModel.hiraganaTotalCount.observe(viewLifecycleOwner) { total ->
            binding.hiraganaProgress.max = total
            binding.hiraganaTotal.text = "/ $total"
        }

        viewModel.katakanaTotalCount.observe(viewLifecycleOwner) { total ->
            binding.katakanaProgress.max = total
            binding.katakanaTotal.text = "/ $total"
        }
    }

    private fun setupClickListeners() {
        binding.cardHiragana.setOnClickListener {
            val bundle = Bundle().apply {
                putString("type", "hiragana")
            }
            findNavController().navigate(R.id.action_home_to_learn, bundle)
        }

        binding.cardKatakana.setOnClickListener {
            val bundle = Bundle().apply {
                putString("type", "katakana")
            }
            findNavController().navigate(R.id.action_home_to_learn, bundle)
        }

        binding.btnQuickQuiz.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_quiz)
        }

        binding.btnContinueStudy.setOnClickListener {
            // Navigate to the last studied type or show selection
            val lastType = sharedPrefsManager.getLastStudyTime()
            // Logic to determine which type to continue with
            findNavController().navigate(R.id.action_home_to_learn)
        }
    }

    private fun updateUI() {
        val streak = viewModel.getStudyStreak()
        val totalTime = viewModel.getTotalStudyTime()

        binding.studyStreak.text = streak.toString()
        binding.totalStudyTime.text = formatStudyTime(totalTime)

        // Show motivational message based on progress
        val motivationMessage = getMotivationalMessage(streak)
        binding.motivationText.text = motivationMessage
    }

    private fun formatStudyTime(timeInMillis: Long): String {
        val minutes = timeInMillis / (1000 * 60)
        val hours = minutes / 60
        val remainingMinutes = minutes % 60

        return if (hours > 0) {
            "${hours}h ${remainingMinutes}m"
        } else {
            "${remainingMinutes}m"
        }
    }

    private fun getMotivationalMessage(streak: Int): String {
        return when {
            streak == 0 -> "Ready to start your Japanese journey? 🌟"
            streak < 7 -> "Great start! Keep going! 💪"
            streak < 30 -> "You're on fire! ${streak} days strong! 🔥"
            else -> "Amazing dedication! ${streak} days! 🏆"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}