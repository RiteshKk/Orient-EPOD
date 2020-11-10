package com.ipssi.orient_epod

import android.app.Activity.RESULT_OK
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ipssi.orient_epod.adapter.InvoiceAdapter
import com.ipssi.orient_epod.callbacks.OnInvoiceSelectedListener
import com.ipssi.orient_epod.databinding.FragmentMainBinding
import com.ipssi.orient_epod.location.CoreUtility
import com.ipssi.orient_epod.location.LocationUtils
import com.ipssi.orient_epod.location.LocationUtils.Companion.LOCATION_REQUEST
import com.ipssi.orient_epod.login.SharedViewModel
import com.ipssi.orient_epod.model.Credentials
import com.ipssi.orient_epod.model.Invoice
import com.ipssi.orient_epod.remote.remote.util.Status
import com.ipssi.orient_epod.remote.util.AppConstant


class HomeFragment : Fragment(), OnInvoiceSelectedListener, LocationUtils.TurnLocationListener {

    private lateinit var binding: FragmentMainBinding
    private lateinit var viewModel: SharedViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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
        binding.swipeContainer.setOnRefreshListener {
            getUpdatedData()
            binding.swipeContainer.isRefreshing = false
        }
    }

    override fun onStart() {
        super.onStart()
        getUpdatedData()
        handleBackPress()
        // for Continuous Location update
        if (CoreUtility.isLocationPermissionAvailable(requireContext())) {
            if (CoreUtility.isLocationOn(requireContext())) {
                configureService(requireContext())
                CoreUtility.startBackgroundWorker()
            } else {
                CoreUtility.enableLocation(requireActivity(), this)
            }
        } else {
            CoreUtility.requestPermissions(this, 2001)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_REQUEST && resultCode == RESULT_OK) {
            locationStatus(true)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 2001 && grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED) {
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
        val tCode = sharedPreferences.getString(AppConstant.TRANSPORTER_CODE, "") ?: ""
        val vehicleName = sharedPreferences.getString(AppConstant.VEHICLE_NUMBER, "") ?: ""
        viewModel.getShipmentDetails(Credentials(transporterCode = tCode, vehicleNo = vehicleName))
    }

    override fun onInvoiceSelected(invoice: Invoice?) {
        val intent = Intent(requireContext(), InvoiceDetailsActivity::class.java)
        intent.putExtra(AppConstant.MODEL, invoice)
        startActivity(intent)
    }

    override fun onLrIconClicked(link: String?) {
        childFragmentManager.beginTransaction().addToBackStack("").replace(R.id.container, WebViewFragment.newInstance(link)).commit()
    }


    private fun handleBackPress() {
        binding.root.requestFocus()
        binding.root.isFocusableInTouchMode = true
        binding.root.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK && childFragmentManager.backStackEntryCount > 0) {
                childFragmentManager.popBackStackImmediate()
            } else false
        }
    }

    private fun observeModel() {
        viewModel.shipmentDetailsData.observe(requireActivity(), {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        resource.data.let { dao ->
                            (binding.invoiceList.adapter as InvoiceAdapter).setData(dao?.invoices)
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

    override fun locationStatus(isTurnOn: Boolean) {
        if (isTurnOn) {
            configureService(requireContext())
            CoreUtility.startBackgroundWorker()
        }
    }
}