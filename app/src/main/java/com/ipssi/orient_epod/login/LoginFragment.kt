package com.ipssi.orient_epod.login

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.ipssi.orient_epod.R
import com.ipssi.orient_epod.callbacks.OnLoginListener
import com.ipssi.orient_epod.databinding.FragmentLoginBinding
import com.ipssi.orient_epod.hideKeyboard
import com.ipssi.orient_epod.model.Credentials
import com.ipssi.orient_epod.remote.remote.util.Status
import com.ipssi.orient_epod.remote.util.AppConstant
import com.ipssi.orient_epod.showAlertDialog

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: SharedViewModel
    private lateinit var loginListener: OnLoginListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnLoginListener) {
            loginListener = context
        } else {
            throw Exception("Login Listener is null")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        binding.viewModel = viewModel

        observeModel()

        binding.transporterCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.transporterCode.error = null
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding.truckNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.truckNumber.error = null
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding.btnLogin.setOnClickListener {
            hideKeyboard(binding.root, requireContext())
            val tCode = binding.transporterCode.text.toString()
            val vehicleName = binding.truckNumber.text.toString()

            if (tCode.isNullOrBlank()) {
                binding.transporterCode.error = getString(R.string.field_cant_be_empty)
            } else if (vehicleName.isNullOrBlank()) {
                binding.truckNumber.error = getString(R.string.field_cant_be_empty)
            }

            if (tCode.isNotEmpty() && vehicleName.isNotEmpty()) {
                viewModel.getShipmentDetails(Credentials(transporterCode = tCode, vehicleNo = vehicleName))
            }
        }
    }

    private fun observeModel() {
        viewModel.shipmentDetailsData.observe(requireActivity(), {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        resource.data.let {
                            if (resource.data?.invoices?.size ?: 0 > 0) {
                                requireActivity().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE).edit()
                                        .putString(AppConstant.TRANSPORTER_CODE, binding.transporterCode.text.toString())
                                        .putString(AppConstant.VEHICLE_NUMBER, binding.truckNumber.text.toString())
                                        .putString(AppConstant.SHIPMENT_NUMBER, resource.data?.invoices?.get(0)?.shipmentNumber)
                                        .putBoolean(AppConstant.IS_LOGIN, true)
                                        .apply()
                                loginListener.onLoginSuccess()
                            } else {
                                Snackbar.make(binding.root, getString(R.string.no_records_found), Snackbar.LENGTH_LONG).show()
                            }
                        }
                        viewModel.isLoading.value = false
                    }
                    Status.LOADING -> {
                        viewModel.isLoading.value = true
                    }
                    Status.ERROR -> {
                        viewModel.isLoading.value = false
                        showAlertDialog(requireActivity(), resource.message)
                    }
                    Status.OFFLINE -> {
                        viewModel.isLoading.value = false
                        showAlertDialog(requireActivity(), resource.message)
                    }
                }
            }
        })
    }
}