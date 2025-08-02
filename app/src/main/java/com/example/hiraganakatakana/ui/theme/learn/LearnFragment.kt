// ui/learn/LearnFragment.kt
package com.example.hiraganakatakana.ui.learn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hiraganakatakana.databinding.FragmentLearnBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LearnFragment : Fragment() {

    private var _binding: FragmentLearnBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LearnViewModel by viewModels()
    private var currentType = "hiragana"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentType = arguments?.getString("type") ?: "hiragana"

        setupObservers()
        setupClickListeners()
        startLearningSession()
    }

    private fun setupObservers() {
        viewModel.currentCharacter.observe(viewLifecycleOwner) { character ->
            character?.let {
                binding.characterDisplay.text = it.character
                binding.characterRomaji.text = it.romaji
                binding.characterType.text = it.type
                binding.answerInput.text?.clear()
            }
        }

        viewModel.score.observe(viewLifecycleOwner) { score ->
            binding.scoreText.text = "Score: $score"
        }

        viewModel.streak.observe(viewLifecycleOwner) { streak ->
            binding.streakText.text = "Streak: $streak"
        }

        viewModel.sessionProgress.observe(viewLifecycleOwner) { progress ->
            binding.progressText.text = "Progress: ${progress.charactersStudied} studied"
            binding.accuracyText.text = "Accuracy: ${progress.accuracy.toInt()}%"
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.submitButton.isEnabled = !isLoading
        }
    }

    private fun setupClickListeners() {
        binding.submitButton.setOnClickListener {
            val userAnswer = binding.answerInput.text.toString().trim()
            if (userAnswer.isNotEmpty()) {
                viewModel.submitAnswer(userAnswer, currentType)
                showFeedback(userAnswer)
            } else {
                Toast.makeText(context, "Please enter an answer", Toast.LENGTH_SHORT).show()
            }
        }

        binding.skipButton.setOnClickListener {
            viewModel.loadNextCharacter(currentType)
        }

        binding.hintButton.setOnClickListener {
            showHint()
        }

        binding.typeToggle.setOnClickListener {
            toggleType()
        }
    }

    private fun startLearningSession() {
        viewModel.startLearnSession(currentType)
        binding.typeToggle.text = currentType.capitalize()
    }

    private fun showFeedback(userAnswer: String) {
        val character = viewModel.currentCharacter.value
        if (character != null) {
            val isCorrect = userAnswer.lowercase() == character.romaji.lowercase()
            val feedbackText = if (isCorrect) {
                "Correct! ✓"
            } else {
                "Incorrect. The answer is: ${character.romaji}"
            }

            binding.feedbackText.text = feedbackText
            binding.feedbackText.visibility = View.VISIBLE

            // Hide feedback after 2 seconds
            binding.feedbackText.postDelayed({
                binding.feedbackText.visibility = View.GONE
            }, 2000)
        }
    }

    private fun showHint() {
        val character = viewModel.currentCharacter.value
        if (character != null) {
            val hint = "Starts with '${character.romaji.first()}'"
            Toast.makeText(context, hint, Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleType() {
        currentType = if (currentType == "hiragana") "katakana" else "hiragana"
        binding.typeToggle.text = currentType.capitalize()
        startLearningSession()
    }

    override fun onPause() {
        super.onPause()
        viewModel.endSession()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}