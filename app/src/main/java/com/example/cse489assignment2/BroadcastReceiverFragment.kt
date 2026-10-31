package com.example.cse489assignment2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cse489assignment2.databinding.FragmentBroadcastReceiverBinding

/**
 * Drawer option A - Broadcast Receiver.
 *
 * Shows a spinner list to select the type of broadcast operation
 * (1. Custom broadcast receiver / 2. System battery notification receiver)
 * and a "Proceed" button that opens the next activity after selection.
 */
class BroadcastReceiverFragment : Fragment() {

    private var _binding: FragmentBroadcastReceiverBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBroadcastReceiverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnProceed.setOnClickListener {
            val type =
                if (binding.spinnerBroadcast.selectedItemPosition == 0)
                    BroadcastReceiverActivity.TYPE_CUSTOM
                else
                    BroadcastReceiverActivity.TYPE_BATTERY

            val intent = Intent(requireContext(), BroadcastReceiverActivity::class.java)
                .putExtra(BroadcastReceiverActivity.EXTRA_TYPE, type)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
