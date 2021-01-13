package com.ipssi.orient_epod

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ipssi.orient_epod.adapter.InvoiceAdapter
import com.ipssi.orient_epod.callbacks.OnInvoiceSelectedListener
import com.ipssi.orient_epod.databinding.FragmentMainBinding
import com.ipssi.orient_epod.location.CoreUtility
import com.ipssi.orient_epod.location.CoreUtility.Companion.cancelBackgroundWorker
import com.ipssi.orient_epod.location.LocationUtils
import com.ipssi.orient_epod.location.LocationUtils.Companion.LOCATION_REQUEST
import com.ipssi.orient_epod.login.SharedViewModel
import com.ipssi.orient_epod.model.Credentials
import com.ipssi.orient_epod.model.Invoice
import com.ipssi.orient_epod.remote.remote.util.Status
import com.ipssi.orient_epod.remote.util.AppConstant
import com.ipssi.orient_epod.service.LocationScanningService


class HomeFragment : Fragment(), OnInvoiceSelectedListener, LocationUtils.TurnLocationListener {

    private lateinit var binding: FragmentMainBinding
    private lateinit var viewModel: SharedViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentMainBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        observeModel()
        binding.invoiceList.adapter = InvoiceAdapter(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding.swipeContainer.setOnRefreshListener {
            getUpdatedData()
            binding.swipeContainer.isRefreshing = false
        }
    }

    override fun onStart() {
        super.onStart()
        getUpdatedData()
        handleBackPress()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_REQUEST && resultCode == RESULT_OK) {
            locationStatus(true)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 2001 && grantResults[0] == PERMISSION_GRANTED) {
            if (CoreUtility.isLocationOn(requireContext())) {
                configureService(requireContext())
                CoreUtility.startBackgroundWorker()
            } else {
                CoreUtility.enableLocation(requireActivity(), this)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun getUpdatedData() {
        val sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        val vehicleName = sharedPreferences.getString(AppConstant.VEHICLE_NUMBER, "") ?: ""
        val mobile1 = sharedPreferences.getString(AppConstant.MOBILE_1, "") ?: ""
        viewModel.getShipmentDetails(Credentials(transporterCode = "", vehicleNo = vehicleName, phone2 = "", phone1 = mobile1))
    }

    override fun onInvoiceSelected(invoice: Invoice?) {
        val intent = Intent(requireContext(), InvoiceDetailsActivity::class.java)
        intent.putExtra(AppConstant.MODEL, invoice)
        startActivity(intent)
    }

    override fun onLrIconClicked(link: String?) {
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        childFragmentManager.beginTransaction().addToBackStack("").replace(R.id.container, WebViewFragment.newInstance(link)).commit()
    }


    private fun handleBackPress() {
        binding.root.requestFocus()
        binding.root.isFocusableInTouchMode = true
        binding.root.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK && childFragmentManager.backStackEntryCount > 0) {
                (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                childFragmentManager.popBackStackImmediate()
            } else false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            childFragmentManager.popBackStackImmediate()
            (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun observeModel() {
        viewModel.shipmentDetailsData.observe(requireActivity(), {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        resource.data.let { dao ->
                            if (dao?.invoices?.size ?: 0 > 0) {
                                (binding.invoiceList.adapter as InvoiceAdapter).setData(dao?.invoices)
                                // for Continuous Location update
                                if (CoreUtility.isLocationPermissionAvailable(requireContext())) {
                                    if (CoreUtility.isLocationOn(requireContext())) {
                                        val prefrences = requireActivity().getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
                                        prefrences.edit().putString(AppConstant.SHIPMENT_NUMBER, dao?.invoices?.get(0)?.shipmentNumber).apply()
                                        configureService(requireContext())
                                        CoreUtility.startBackgroundWorker()
                                    } else {
                                        CoreUtility.enableLocation(requireActivity(), this)
                                    }
                                } else {
                                    CoreUtility.requestPermissions(this, 2001)
                                }
                            } else {
                                (binding.invoiceList.adapter as InvoiceAdapter).setData(dao?.invoices)
                                showAlertDialog(requireActivity(), getString(R.string.all_invoices_complete), clickListener)
                            }
                        }
                        viewModel.isLoading.value = false
                    }
                    Status.LOADING -> {
                        viewModel.isLoading.value = true
                    }
                    Status.ERROR -> {
                        viewModel.isLoading.value = false
                        requireActivity().stopService(Intent(requireContext(), LocationScanningService::class.java))
                        cancelBackgroundWorker()
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

    override fun locationStatus(isTurnOn: Boolean) {
        if (isTurnOn) {
            configureService(requireContext())
            CoreUtility.startBackgroundWorker()
        }
    }

    private val clickListener = DialogInterface.OnClickListener { dialog, _ ->
        requireActivity().stopService(Intent(requireContext(), LocationScanningService::class.java))
        cancelBackgroundWorker()
        logout(requireContext())
        dialog?.dismiss()
    }
}