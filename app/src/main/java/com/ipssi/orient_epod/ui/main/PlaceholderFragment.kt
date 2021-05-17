package com.ipssi.orient_epod.ui.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.ipssi.orient_epod.InvoiceDetailsActivity
import com.ipssi.orient_epod.R
import com.ipssi.orient_epod.adapter.ImageAdapter
import com.ipssi.orient_epod.callbacks.OnImageDeleteClickListener
import com.ipssi.orient_epod.capturesignature.OnSignedCaptureListener
import com.ipssi.orient_epod.capturesignature.SignatureDialogFragment
import com.ipssi.orient_epod.databinding.FragmentTabbedBinding
import com.ipssi.orient_epod.hideKeyboard
import com.ipssi.orient_epod.location.LocationAPI
import com.ipssi.orient_epod.location.OnLocationChangeCallBack
import com.ipssi.orient_epod.model.*
import com.ipssi.orient_epod.remote.remote.util.Resource
import com.ipssi.orient_epod.remote.remote.util.Status
import com.ipssi.orient_epod.remote.util.AppConstant
import com.ipssi.orient_epod.showAlertDialog
import java.text.DecimalFormat
import java.util.*
import java.util.regex.Pattern

/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment : Fragment(), OnSignedCaptureListener, OnLocationChangeCallBack, OnImageDeleteClickListener {
    private lateinit var binding: FragmentTabbedBinding
    private var locationApi: LocationAPI? = null
    private var adapter: ImageAdapter? = null
    private lateinit var permissionStatus: SharedPreferences
    private lateinit var viewModel: PageViewModel
    private var isFinalSubmit = false
    private var hasError = false
    private var invoice: Invoice? = null
    private var receiverId = 0
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        binding = FragmentTabbedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        permissionStatus = requireContext().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        if (arguments != null) {
            invoice = arguments?.getParcelable(AppConstant.INVOICE)
        }
        viewModel = ViewModelProvider(this).get(PageViewModel::class.java)
        binding.viewModel = viewModel
        locationApi = LocationAPI(requireActivity(), this)
        init()
        setupForLastReceiver()
        handleClickListener()
        setObserver()
        binding.imageList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter = ImageAdapter(this)
        binding.imageList.adapter = adapter
        getUploadedDocs()
    }

    private fun setupForLastReceiver() {
        val index = arguments?.getInt(AppConstant.INDEX, 0)
        if (index == 3) {
            viewModel.isCompleteTripChecked.value = true
            binding.chechBoxCompleteTrip.setOnClickListener {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Alert").setMessage("No More Receiver").setPositiveButton(android.R.string.ok
                ) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    viewModel.isCompleteTripChecked.setValue(true)
                }.show()
            }
        } else {
            binding.chechBoxCompleteTrip.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean -> viewModel.isCompleteTripChecked.setValue(isChecked) }
        }
    }

    private fun getUploadedDocs() {
        viewModel.fetchUploadedImage(invoice)
    }

    private fun init() {
        val receiver: Receiver? = arguments?.getParcelable(AppConstant.RECEIVER)
        binding.totalDamage.editText?.setText(InvoiceDetailsActivity.totalDamage.toString())
        if (invoice?.loadType.equals("Standard", ignoreCase = true)) {
            binding.loadType.editText?.setText(R.string.title_bags)
            binding.layoutBags.hint = getString(R.string.bags_received_for_standard)
            binding.layoutDamageBags.hint = getString(R.string.bags_damage_for_standard)
        } else {
            binding.loadType.editText?.setText(R.string.title_mt)
            binding.layoutDamageBags.isEnabled = false
            binding.layoutBags.hint = getString(R.string.actual_weightment)
            binding.layoutBags.editText?.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            binding.layoutDamageBags.editText?.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            binding.layoutBags.editText?.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(5, 3))
            binding.chechBoxCompleteTrip.visibility = View.GONE
            viewModel.isCompleteTripChecked.setValue(true)
        }
        if (receiver != null) {
            viewModel.name.value = receiver.name
            viewModel.mobile.value = receiver.mobile
            if (receiver.invoiceNumber.isNotEmpty()) {
                viewModel.bagsReceived.value = receiver.bagsRecv
                viewModel.damageBags.value = receiver.shortage
                viewModel.remarks.value = receiver.remarks
                val decode = Base64.decode(receiver.sign, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.size)
                viewModel.signature.value = bitmap
                viewModel.isEditable.value = false
            }
        }
    }

    private fun setObserver() {
        viewModel.isEditable.observe(viewLifecycleOwner, { isEnabled: Boolean? -> binding.btnSubmit.isEnabled = isEnabled!! })
        viewModel.isCompleteTripChecked.observe(viewLifecycleOwner, { isChecked: Boolean ->
            if (isChecked) {
                binding.btnSubmit.isEnabled = true
            } else {
                binding.btnSubmit.isEnabled = viewModel.isEditable.value!!
            }
        })
        viewModel.signature.observe(viewLifecycleOwner, { bitmap: Bitmap? -> binding.sign.setImageBitmap(bitmap) })
        viewModel.name.observe(viewLifecycleOwner, { binding.layoutName.error = null })
        viewModel.mobile.observe(viewLifecycleOwner, { binding.layoutMobile.error = null })
        viewModel.bagsReceived.observe(viewLifecycleOwner, { s: String ->
            hasError = false
            binding.layoutBags.error = null
            if (viewModel.isEditable.value!!) {
                if (s.isEmpty()) {
                    if (!invoice!!.loadType.equals("standard", ignoreCase = true)) {
                        binding.layoutDamageBags.editText?.setText("")
                        binding.totalDamage.editText?.setText("")
                    }
                }
                try {
                    if (!invoice?.loadType.equals("standard", ignoreCase = true)) {
                        if (InvoiceDetailsActivity.totalQuantity / 20f < InvoiceDetailsActivity.totalDeliveredQuantity + InvoiceDetailsActivity.totalDamage + s.toFloat()) {
                            hasError = true
                            binding.layoutBags.error = "You have only " + ((InvoiceDetailsActivity.totalQuantity / 20f) - (InvoiceDetailsActivity.totalDamage + InvoiceDetailsActivity.totalDeliveredQuantity)) + " MT available"
                        } else {
                            val totalQuality = InvoiceDetailsActivity.totalQuantity / 20f - (InvoiceDetailsActivity.totalDeliveredQuantity + InvoiceDetailsActivity.totalDamage)
                            val enteredValue = s.toFloat()
                            val remainingBags = totalQuality - enteredValue
                            val df = DecimalFormat("###.###")
                            if (remainingBags < 0f) {
                                hasError = true
                                binding.layoutDamageBags.error = "You have only $totalQuality MT available"
                            } else {
                                binding.layoutDamageBags.editText?.setText(df.format(remainingBags.toDouble()))
                                binding.totalDamage.editText?.setText(df.format(remainingBags.toDouble()))
                                binding.layoutBags.error = null
                            }
                        }
                    } else {
                        val damageBags = viewModel.damageBags.value.let {
                            if (it.isNullOrEmpty()) {
                                0L
                            } else {
                                it.toLong()
                            }
                        }

                        if (InvoiceDetailsActivity.totalQuantity.toLong() == InvoiceDetailsActivity.totalDeliveredQuantity + InvoiceDetailsActivity.totalDamage + s.toLong() + damageBags) {
                            viewModel.isCompleteTripChecked.value = true
                        } else if (s.isNotEmpty() && InvoiceDetailsActivity.totalQuantity.toLong() > InvoiceDetailsActivity.totalDeliveredQuantity.toLong() + InvoiceDetailsActivity.totalDamage.toLong() + s.toLong() + damageBags) {
                            viewModel.isCompleteTripChecked.value = false
                        } else if (InvoiceDetailsActivity.totalQuantity < InvoiceDetailsActivity.totalDeliveredQuantity + InvoiceDetailsActivity.totalDamage + s.toLong() + damageBags) {
                            hasError = true
                            binding.layoutBags.error = "You have only " + (InvoiceDetailsActivity.totalQuantity - (InvoiceDetailsActivity.totalDamage + InvoiceDetailsActivity.totalDeliveredQuantity + damageBags)) + " bags available"
                        }
                    }
                } catch (e: Exception) {
                    Log.e("exception", "exception : ${e.message}")
                }
            }
        })
        viewModel.damageBags.observe(viewLifecycleOwner, { s: String ->
            binding.layoutDamageBags.error = null
            hasError = false
            if (viewModel.isEditable.value == true) {
                if (s.isNotEmpty()) {
                    if (invoice?.loadType.equals("standard", ignoreCase = true)) {
                        val receivedBagsValue = viewModel.bagsReceived.value
                        if (receivedBagsValue?.isNotEmpty() == true) {
                            val receivedBags = receivedBagsValue.toLong()
                            val damageBags = s.toLong()
                            val totalAvailable = InvoiceDetailsActivity.totalQuantity - (InvoiceDetailsActivity.totalDeliveredQuantity + InvoiceDetailsActivity.totalDamage)
                            if (totalAvailable < (receivedBags + damageBags)) {
                                hasError = true
                                binding.layoutDamageBags.error = when {
                                    totalAvailable - receivedBags < 0 -> {
                                        "Only $totalAvailable bags available"
                                    }
                                    totalAvailable - receivedBags == 0L -> {
                                        "All bags consumed. No bags available"
                                    }
                                    else -> {
                                        "Only ${totalAvailable - receivedBags} bags available"
                                    }
                                }
                            } else if (totalAvailable.toLong() == (receivedBags + damageBags)) {
                                viewModel.isCompleteTripChecked.value = true
                            } else if (totalAvailable > (receivedBags + damageBags)) {
                                viewModel.isCompleteTripChecked.value = false
                            }
                        } else {
                            hasError = true
                            binding.layoutBags.error = "Enter Received bags quantity"
                        }
                    }
                }
            }
        })
        viewModel.uploadStatus.observe(viewLifecycleOwner, { (status, _, message) ->
            when (status) {
                Status.LOADING -> showSnackbar(getString(R.string.uploading_image))
                Status.ERROR -> showSnackbar(message)
                Status.OFFLINE -> showAlertDialog(requireActivity(), message, null)
                Status.SUCCESS -> {
                    showSnackbar(getString(R.string.uploaded))
                    adapter!!.setImages(viewModel.imageList.value!!.data)
                }
            }
        })
        viewModel.imageDeleteObserver.observe(viewLifecycleOwner, { (status, data, message) ->
            when (status) {
                Status.ERROR, Status.OFFLINE -> showAlertDialog(requireActivity(), message, null)
                Status.LOADING -> {
                }
                Status.SUCCESS -> Snackbar.make(binding.root, data!!.message, Snackbar.LENGTH_SHORT).show()
            }
        })
        viewModel.imageList.observe(viewLifecycleOwner, { (status, data, message) ->
            when (status) {
                Status.LOADING -> viewModel.isLoading.setValue(true)
                Status.ERROR, Status.OFFLINE -> {
                    viewModel.isLoading.value = false
                    showAlertDialog(requireActivity(), message, null)
                }
                Status.SUCCESS -> {
                    viewModel.isLoading.value = false
                    adapter!!.setImages(data)
                }
            }
        })
        viewModel.saveReceiverResponse.observe(viewLifecycleOwner, { (status, data, message) ->
            when (status) {
                Status.SUCCESS -> {
                    Snackbar.make(binding.root, data!!.message, Snackbar.LENGTH_SHORT).show()
                    try {
                        receiverId = data.status
                        val totalDamage = binding.layoutDamageBags.editText!!.text.toString().trim { it <= ' ' }.toInt()
                        InvoiceDetailsActivity.totalDamage += totalDamage
                        InvoiceDetailsActivity.totalDeliveredQuantity += binding.layoutBags.editText!!.text.toString().trim { it <= ' ' }.toInt()
                    } catch (e: NumberFormatException) {
                        Log.e("placeHolderFragment", "NumberFormatException->${e.localizedMessage}")
                    }
                    if (isFinalSubmit) {
                        Handler(Looper.myLooper()!!).postDelayed({ requireActivity().finish() }, 2000
                        )
                    }
                    binding.totalDamage.editText?.setText(InvoiceDetailsActivity.totalDamage.toString())
                    viewModel.isEditable.value = false
                    viewModel.isLoading.value = false
                }
                Status.ERROR, Status.OFFLINE -> {
                    viewModel.isLoading.value = false
                    showAlertDialog(requireActivity(), message, null)
                }
                Status.LOADING -> viewModel.isLoading.value = true
            }
        })
    }

    private fun handleClickListener() {
        binding.sign.setOnClickListener {
            hideKeyboard(binding.root, requireContext())
            val dialogFragment = SignatureDialogFragment(this@PlaceholderFragment)
            dialogFragment.show(childFragmentManager, "signature")
        }
        binding.btnSubmit.setOnClickListener {
            if (viewModel.isCompleteTripChecked.value!!) {
                isFinalSubmit = true
            }
            hideKeyboard(binding.root, requireContext())
            onSubmitClick()
        }
        binding.btnCaptureImage.setOnClickListener {
            val value: Resource<ArrayList<UploadDocumentEntity>?>? = viewModel.imageList.value
            if (value?.data == null || value.data.size < 3) {
                checkPermissionAndCaptureImage()
            } else {
                showSnackbar(getString(R.string.max_3_allowed))
            }
        }
    }

    private fun checkPermissionAndCaptureImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                when {
                    ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                        val alert = android.app.AlertDialog.Builder(requireContext())
                        alert.setMessage(R.string.storage_required)
                                .setPositiveButton(R.string.allow) { _: DialogInterface?, _: Int -> requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 2) }
                                .setNegativeButton(android.R.string.cancel) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }.show()
                    }
                    permissionStatus.getBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, false) -> {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", requireActivity().packageName, null)
                        intent.data = uri
                        startActivityForResult(intent, 2)
                    }
                    else -> {
                        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 2)
                    }
                }
                val editor = permissionStatus.edit()
                editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, true)
                editor.apply()
            }
        } else {
            dispatchTakePictureIntent()
        }
    }

    private fun checkPermissionAndGetLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationApi!!.onStart()
            } else {
                requestPermissions()
            }
        } else {
            locationApi!!.onStart()
        }
    }

    private fun onSubmitClick() {
        binding.root.clearFocus()
        val name = binding.layoutName.editText?.text.toString()
        if (name.isEmpty()) {
            binding.layoutName.error = getString(R.string.field_cant_be_empty)
            return
        }
        val mobile = binding.layoutMobile.editText?.text.toString()
        if (mobile.isEmpty()) {
            binding.layoutMobile.error = getString(R.string.field_cant_be_empty)
            return
        } else if (mobile.length != 10) {
            binding.layoutMobile.error = getString(R.string.enter_valid_number)
            return
        }
        if (hasError) {
            return
        }
        if (isFinalSubmit && !invoice?.loadType.equals("standard", ignoreCase = true)) {
            val damageBagsValue = binding.layoutDamageBags.editText?.text.toString()
            val receivedBagsValue = binding.layoutBags.editText?.text.toString()
            var damagedBags = 0f
            var receivedBags = 0f
            try {
                damagedBags = damageBagsValue.trim { it <= ' ' }.toFloat()
            } catch (e2: Exception) {
                Log.e("damageBagsError", e2.message!!)
            }
            try {
                receivedBags = receivedBagsValue.trim { it <= ' ' }.toFloat()
            } catch (ex: Exception) {
                Log.e("receivedBags Error", ex.message!!)
            }
            if ((InvoiceDetailsActivity.totalQuantity / 20f) != InvoiceDetailsActivity.totalDeliveredQuantity + InvoiceDetailsActivity.totalDamage + damagedBags + receivedBags) {
                Snackbar.make(binding.root, "${InvoiceDetailsActivity.totalQuantity / 20f - (InvoiceDetailsActivity.totalDeliveredQuantity + InvoiceDetailsActivity.totalDamage + damagedBags + receivedBags)} MT still not delivered. Please deliver all bags before completing trip", Snackbar.LENGTH_LONG).show()
                return
            }
        }
        val bitmapDrawable = binding.sign.drawable as BitmapDrawable?
        if (bitmapDrawable == null) {
            Snackbar.make(binding.root, R.string.provide_signature, Snackbar.LENGTH_LONG).show()
            return
        } else {
            val image = bitmapDrawable.bitmap
            if (image == null) {
                Snackbar.make(binding.root, R.string.provide_signature, Snackbar.LENGTH_LONG).show()
                return
            }
        }
        if (!invoice?.loadType.equals("standard", ignoreCase = true)) {
            if (isFinalSubmit && (viewModel.imageList.value == null || viewModel.imageList.value!!.data == null || viewModel.imageList.value?.data?.size == 0)) {
                Snackbar.make(binding.root, "Document list is empty. Please scan document", Snackbar.LENGTH_LONG).show()
                return
            }
        }
        checkPermissionAndGetLocation()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
        when {
            shouldProvideRationale -> {
                showSnackBar(R.string.permission_rationale,
                        android.R.string.ok) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            102)
                }
            }
            permissionStatus.getBoolean(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivityForResult(intent, 102)
            }
            else -> {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        102)
            }
        }
        val editor = permissionStatus.edit()
        editor.putBoolean(Manifest.permission.ACCESS_FINE_LOCATION, true)
        editor.apply()
    }

    private fun showSnackBar(mainTextStringId: Int, actionStringId: Int,
                             listener: View.OnClickListener) {
        Snackbar.make(binding.root, getString(mainTextStringId), Snackbar.LENGTH_INDEFINITE).setAction(getString(actionStringId), listener).show()
    }

    private fun showSnackbar(mainTextString: String?) {
        Snackbar.make(binding.root, mainTextString!!, Snackbar.LENGTH_SHORT).show()
    }

    override fun onSignatureCaptured(bitmap: Bitmap, fileUri: String) {
        viewModel.signature.value = bitmap
    }

    override fun onLocationChange(loc: Location) {
        if (arguments != null) {
            val invoice: Invoice = arguments?.getParcelable(AppConstant.INVOICE) ?: Invoice()
            val receiver: Receiver? = arguments?.getParcelable(AppConstant.RECEIVER)
            var id = -1
            if (receiver != null && receiver.id > 0) {
                id = receiver.id
            } else if (receiverId > 0) {
                id = receiverId
            }
            viewModel.saveReceiver(id, invoice.invoiceNumber, if (invoice.loadType.equals("standard", ignoreCase = true)) 0 else 1, loc, isFinalSubmit)
        }
        locationApi?.onStop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 102 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationApi?.onStart()
        } else if (requestCode == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(requireContext().packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            val bitmap = data!!.extras!!["data"] as Bitmap?
            val invoice: Invoice = arguments?.getParcelable(AppConstant.INVOICE) ?: Invoice()
            viewModel.uploadImage(bitmap!!, invoice)
        } else if (requestCode == 102 && resultCode == Activity.RESULT_OK) {
            locationApi?.onStart()
        }
    }

    override fun onImageDeleteClick(entity: UploadDocumentEntity) {
        viewModel.deleteUploadedImage(entity)
    }

    internal class DecimalDigitsInputFilter(digitsBeforeZero: Int, digitsAfterZero: Int) : InputFilter {
        val mPattern = Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?")
        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
            val matcher = mPattern.matcher(dest)
            return if (!matcher.matches()) "" else null
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(index: Int, invoice: Invoice?, receiver: Receiver?): PlaceholderFragment {
            val bundle = Bundle()
            bundle.putInt(AppConstant.INDEX, index)
            bundle.putParcelable(AppConstant.INVOICE, invoice)
            bundle.putParcelable(AppConstant.RECEIVER, receiver)
            val fragment = PlaceholderFragment()
            fragment.arguments = bundle
            return fragment
        }

        const val REQUEST_TAKE_PHOTO = 1
    }
}