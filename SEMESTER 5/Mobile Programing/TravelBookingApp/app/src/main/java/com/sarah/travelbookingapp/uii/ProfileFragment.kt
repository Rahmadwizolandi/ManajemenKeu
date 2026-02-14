package com.sarah.travelbookingapp.uii

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.sarah.travelbookingapp.database.AppDatabase
import com.sarah.travelbookingapp.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("LOGGED_IN_USER_ID", -1)

        if (userId != -1) {
            lifecycleScope.launch {
                val user = db.appDao().getUserById(userId)
                if (user != null) {
                    binding.tvFullname.text = user.fullname
                    binding.tvUsername.text = user.username
                    binding.tvRegistrationDate.text = user.registrationDate
                }
            }
        }

        binding.btnLogout.setOnClickListener {
            // Clear the logged-in user ID from SharedPreferences
            with(sharedPreferences.edit()) {
                remove("LOGGED_IN_USER_ID")
                apply()
            }

            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}