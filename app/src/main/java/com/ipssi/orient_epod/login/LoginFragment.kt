package com.ipssi.orient_epod.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.common.api.GoogleApiClient
import com.ipssi.orient_epod.R
import com.ipssi.orient_epod.callbacks.OnLoginListener
import com.ipssi.orient_epod.databinding.FragmentLoginBinding
import com.ipssi.orient_epod.hideKeyboard
import com.ipssi.orient_epod.remote.remote.util.Status
import com.ipssi.orient_epod.showAlertDialog


class LoginFragment : Fragment(), GoogleApiClient.ConnectionCallbacks {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: SharedViewModel
    private lateinit var loginListener: OnLoginListener

    private val RESOLVE_HINT: Int = 1001


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnLoginListener) {
            loginListener = context
        } else {
            throw Exception("Login Listener is null")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        observeModel()
        initializedHintRequest()
        addTextWatcher()
        handleClick()
    }

    private fun handleClick() {
        binding.btnLogin.setOnClickListener {
            onButtonClick()
        }
        binding.truckNumber.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onButtonClick()
                true
            }
            if (event != null && event.action == KeyEvent.KEYCODE_ENTER) {
                onButtonClick()
                true
            }
            false
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }

    private fun onButtonClick() {
        hideKeyboard(binding.root, requireContext())
        var hasError = false
        val mNumber = binding.mobileNumber.text.toString()
        val vehicleName = binding.truckNumber.text.toString()

        if (mNumber.isEmpty()) {
            binding.mobileNumber.error = getString(R.string.field_cant_be_empty)
            hasError = true
        } else if (mNumber.length != 10) {
            binding.mobileNumber.error = getString(R.string.invalid_number)
            hasError = true
        } else if (vehicleName.isEmpty()) {
            binding.truckNumber.error = getString(R.string.field_cant_be_empty)
            hasError = true
        }
        if (hasError.not()) {
            viewModel.mobileNumber = "+91$mNumber"
            viewModel.vehicle = vehicleName
            viewModel.getOTP()
        }
    }

    private fun observeModel() {
        viewModel.otpLiveData.observe(viewLifecycleOwner, {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        val apiOtp = resource.data
                        if (apiOtp?.length == 4) {
                            Log.d("otp", apiOtp)
                            loginListener.showOTPView()
                        } else {
                            showAlertDialog(requireActivity(), apiOtp)
                        }
                        viewModel.isLoading.value = false
                    }
                    Status.ERROR -> {
                        viewModel.isLoading.value = false
                        showAlertDialog(requireActivity(), resource.message)
                    }
                    Status.LOADING -> {
                        viewModel.isLoading.value = true
                    }
                    Status.OFFLINE -> {
                        viewModel.isLoading.value = false
                        showAlertDialog(requireActivity(), resource.message)
                    }
                }
            }
        })
    }

    private fun addTextWatcher() {
        binding.mobileNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.mobileNumber.error = null
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
        binding.truckNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.truckNumber.error = null
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESOLVE_HINT && resultCode == AppCompatActivity.RESULT_OK) {
            val credential: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)
            var selectedMobileNumber = credential?.id
            Log.d("MainActivity", "$selectedMobileNumber")
            selectedMobileNumber = selectedMobileNumber?.replace("+91","")
            selectedMobileNumber=selectedMobileNumber?.removePrefix("0")
            binding.mobileNumber.setText(selectedMobileNumber)
        }
    }

    /*private fun login() {
        val mNumber = binding.mobileNumber.text.toString()
        val vehicleName = binding.truckNumber.text.toString()
        viewModel.getShipmentDetails(Credentials(transporterCode = null, vehicleNo = vehicleName, phone1 = mNumber
                ?: "", phone2 = null))
    }*/

    /*   private fun observeModel() {
           viewModel.shipmentDetailsData.observe(requireActivity(), {
               it.let { resource ->
                   when (resource.status) {
                       SUCCESS -> {
                           resource.data.let {
                               if (resource.data?.invoices?.size ?: 0 > 0) {
                                   val mNumber = binding.mobileNumber.text.toString()
                                   requireActivity().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE).edit()
                                           .putString(AppConstant.VEHICLE_NUMBER, binding.truckNumber.text.toString())
                                           .putString(AppConstant.SHIPMENT_NUMBER, resource.data?.invoices?.get(0)?.shipmentNumber)
                                           .putString(AppConstant.MOBILE_1, mNumber)
                                           .putBoolean(AppConstant.IS_LOGIN, true)
                                           .apply()
                                   loginListener.onLoginSuccess()
                               } else {
                                   Snackbar.make(binding.root, getString(R.string.no_records_found), Snackbar.LENGTH_LONG).show()
                               }
                           }
                           viewModel.isLoading.value = false
                       }
                       LOADING -> {
                           viewModel.isLoading.value = true
                       }
                       ERROR -> {
                           viewModel.isLoading.value = false
                           showAlertDialog(requireActivity(), resource.message)
                       }
                       OFFLINE -> {
                           viewModel.isLoading.value = false
                           showAlertDialog(requireActivity(), resource.message)
                       }
                   }
               }
           })
       }*/

    private fun initializedHintRequest() {
        val hintRequest = HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build()

        val googleApiClient = GoogleApiClient.Builder(requireActivity())
                .addApi(Auth.CREDENTIALS_API)
                .addOnConnectionFailedListener {
                    Log.d("LoginFragment", it.errorMessage ?: "failed to connect")
                }.addConnectionCallbacks(this)
                .build()

        googleApiClient.connect()


        val intent = Auth.CredentialsApi.getHintPickerIntent(googleApiClient, hintRequest)
        startIntentSenderForResult(
                intent.intentSender,
                RESOLVE_HINT, null, 0, 0, 0, null)
    }

    override fun onConnected(p0: Bundle?) {
        Log.d("LoginFragment", "onConnected")
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.d("LoginFragment", "onConnectionSuspended")
    }
}