package com.ipssi.orient_epod.login

import `in`.aabhasjindal.otptextview.OTPListener
import android.content.Context
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.ipssi.orient_epod.R
import com.ipssi.orient_epod.callbacks.OnLoginListener
import com.ipssi.orient_epod.databinding.FragmentOtpBinding
import com.ipssi.orient_epod.hideKeyboard
import com.ipssi.orient_epod.remote.remote.util.Status
import com.ipssi.orient_epod.remote.util.AppConstant
import com.ipssi.orient_epod.showAlertDialog
import com.ipssi.orient_epod.smsreceiver.MySMSBroadcastReceiver
import kotlinx.android.synthetic.main.fragment_otp.*

class OTPFragment : Fragment(), MySMSBroadcastReceiver.OTPReceiveListener {
    private lateinit var binding: FragmentOtpBinding
    private lateinit var viewModel: SharedViewModel
    private var smsReceiver: MySMSBroadcastReceiver? = null
    var timeMilliSeconds = 0L
    var apiOtp: String? = null
    private lateinit var loginListener: OnLoginListener


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnLoginListener) {
            loginListener = context
        } else {
            throw Exception("Login Listener is null")
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        observeModel()
        sendOTP()
        initializeViews()
    }

    private fun sendOTP() {
        viewModel.getOTP()
    }

    private fun observeModel() {
        viewModel.otpLiveData.observe(viewLifecycleOwner, {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        apiOtp = resource.data
                        Log.d("OTP", apiOtp ?: "null")
                        viewModel.isLoading.value = false
                    }
                    Status.ERROR -> {
                        viewModel.isLoading.value = false
//                        showAlertDialog(requireActivity(), resource.message)
                    }
                    Status.LOADING -> {
                        viewModel.isLoading.value = true
                    }
                    Status.OFFLINE -> {
                        viewModel.isLoading.value = false
//                        showAlertDialog(requireActivity(), resource.message)
                    }
                }
            }
        })
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }

    private fun initializeViews() {
        startSmsRetriever()
        startTimer()
        binding.textViewTimeLeft.isEnabled = false
        textViewTimeLeft.setOnClickListener {
            binding.textViewTimeLeft.isEnabled = false
            sendOTP()
            if (timeMilliSeconds <= 1000L) {
                timeMilliSeconds = 0L
                startTimer()
            }
        }
        binding.otpView.setOnTouchListener { _, _ -> true }
        binding.otpView.otpListener = object : OTPListener {
            override fun onInteractionListener() {
            }

            override fun onOTPComplete(otp: String) {
                if (otp.equals(apiOtp, ignoreCase = true)) {
                    requireActivity().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE).edit()
                            .putString(AppConstant.VEHICLE_NUMBER, viewModel.vehicle)
                            .putString(AppConstant.MOBILE_1, viewModel.mobileNumber)
                            .putBoolean(AppConstant.IS_LOGIN, true)
                            .apply()
                    Handler(Looper.getMainLooper()).postDelayed({ loginListener.onLoginSuccess() }, 1000)
                } else {
                    Toast.makeText(requireActivity(), "You have entered wrong OTP", Toast.LENGTH_SHORT)
                            .show()
                }
            }
        }
    }

    override fun onDestroy() {
        Log.v("OTPStop", "otp stop receiver onDestroy")
        if (smsReceiver != null) {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(smsReceiver!!)
        }
        super.onDestroy()
    }


    private fun startSmsRetriever() {
        try {
            smsReceiver = MySMSBroadcastReceiver()
            smsReceiver!!.initOTPListener(this)

            val intentFilter = IntentFilter()
            intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
            requireContext().registerReceiver(smsReceiver, intentFilter)

            val client = SmsRetriever.getClient(requireActivity())

            val task = client.startSmsRetriever()
            task.addOnSuccessListener {
                // API successfully started
            }

            task.addOnFailureListener {
                // Fail to start API
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun startTimer() {
        val timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeMilliSeconds = millisUntilFinished
                updateTextUI()
            }

            override fun onFinish() {
                updateTextUIFinish()
            }
        }
        timer.start()
    }

    private fun updateTextUI() {
        val minute = (timeMilliSeconds / 1000) / 60
        val seconds = (timeMilliSeconds / 1000) % 60
        if (isVisible) {
            val timerMessage = String.format(getString(R.string.timer_message), minute, seconds)
            val spannable = SpannableStringBuilder(timerMessage)
            spannable.setSpan(
                    ForegroundColorSpan(resources.getColor(R.color.colorBlueStripFont)),
                    timerMessage.indexOf("Resend"),
                    timerMessage.length,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
            binding.textViewTimeLeft.text = spannable
        }
    }

    private fun updateTextUIFinish() {
        if (isVisible) {
            binding.textViewTimeLeft.isEnabled = true
            binding.textViewTimeLeft.text = "Resend OTP"
        }
    }

    override fun onOTPReceived(otp: String) {
        val finalOTP = otp.substring(0, 4)
        Log.v("OTPReceived", finalOTP)
        if (isVisible) {
            binding.otpView.setOTP(finalOTP)
            if (smsReceiver != null) {
                LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(smsReceiver!!)
            }
        }
    }

    override fun onOTPTimeOut() {
        Log.v("OTPTimeOut", "otp timeout processed")
    }

}