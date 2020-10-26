package com.ipssi.orient_epod

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.ipssi.orient_epod.adapter.InvoiceAdapter
import com.ipssi.orient_epod.callbacks.OnInvoiceSelectedListener
import com.ipssi.orient_epod.databinding.FragmentMainBinding
import com.ipssi.orient_epod.login.SharedViewModel
import com.ipssi.orient_epod.model.Credentials
import com.ipssi.orient_epod.model.Invoice
import com.ipssi.orient_epod.remote.remote.util.Status
import com.ipssi.orient_epod.remote.util.AppConstant


class HomeFragment : Fragment(), OnInvoiceSelectedListener {

    private lateinit var binding: FragmentMainBinding
    private lateinit var viewModel: SharedViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
        observeModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.invoiceList.adapter = InvoiceAdapter(this)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        val tCode = sharedPreferences.getString(AppConstant.TRANSPORTER_CODE, "") ?: ""
        val vehicleName = sharedPreferences.getString(AppConstant.VEHICLE_NUMBER, "") ?: ""
        viewModel.getShipmentDetails(Credentials(transporterCode = tCode, vehicleNo = vehicleName))
        handleBackPress()
    }

    override fun onInvoiceSelected(invoice: Invoice?) {
        val intent = Intent(requireContext(), InvoiceDetailsActivity::class.java)
        intent.putExtra(AppConstant.MODEL, invoice)
        startActivity(intent)
    }

    override fun onLrIconClicked(link: String?) {
        binding.pdfView.fromAsset("output.pdf").load()
        binding.lrViewLayout.visibility = View.VISIBLE
    }


    private fun handleBackPress() {
        binding.root.requestFocus()
        binding.root.isFocusableInTouchMode = true
        binding.root.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK && binding.lrViewLayout.visibility == View.VISIBLE) {
                binding.lrViewLayout.visibility = View.GONE
                true
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
                        Snackbar.make(binding.root, AppConstant.SERVER_ERROR, Snackbar.LENGTH_LONG)
                            .show()
                    }
                    Status.OFFLINE -> {
                        viewModel.isLoading.value = false
                        Snackbar.make(binding.root, AppConstant.OFFLINE_ERROR, Snackbar.LENGTH_LONG)
                            .show()
                    }
                    else -> {
                        Log.d("LoginActivity", "else block")
                    }
                }
            }
        })
    }
}