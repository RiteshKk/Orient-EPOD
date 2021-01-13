package com.ipssi.orient_epod

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.ipssi.orient_epod.databinding.ActivityInvoiceDetailsBinding
import com.ipssi.orient_epod.model.Invoice
import com.ipssi.orient_epod.model.Receiver
import com.ipssi.orient_epod.remote.remote.util.Status
import com.ipssi.orient_epod.remote.util.AppConstant
import com.ipssi.orient_epod.ui.main.PlaceholderFragment
import com.ipssi.orient_epod.ui.main.PlaceholderFragment.Companion.newInstance
import com.ipssi.orient_epod.ui.main.SectionsPagerAdapter
import java.util.*

class InvoiceDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInvoiceDetailsBinding
    private lateinit var viewModel: InvoiceDetailsViewModel
    override fun onDestroy() {
        super.onDestroy()
        totalDamage = 0
        totalQuantity = 0
        totalDeliveredQuantity = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_invoice_details)
        binding.collapsingToolbar.contentScrim = resources.getDrawable(R.drawable.screen_full_bg)
        viewModel = ViewModelProvider(this).get(InvoiceDetailsViewModel::class.java)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.lifecycleOwner = this
        val model: Invoice = intent.getParcelableExtra(AppConstant.MODEL) ?: Invoice()
        totalQuantity = try {
            (20 * model.invoiceQuantity.trim { it <= ' ' }.toFloat()).toInt()
        } catch (e: Exception) {
            Log.e("NumberFormatException", e.message!!)
            0
        }
        val address = StringBuilder()
        if (model.shiptopartyAddress != null && model.shiptopartyAddress.isNotEmpty()) {
            address.append(model.shiptopartyAddress)
        }
        if (model.shiptopartyAddress1 != null && model.shiptopartyAddress1.isNotEmpty()) {
            address.append(if (address.isNotEmpty()) "\n" else "")
            address.append(model.shiptopartyAddress1)
        }
        model.shiptopartyAddress = address.toString()
        binding.model = model
        viewModel.getReceivers(model.invoiceNumber)
        setObserver()
        binding.appBarLayout.addOnOffsetChangedListener(object : OnOffsetChangedListener {
            var isShow = false
            var scrollRange = -1
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.totalScrollRange
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true
                    binding.toolbarTitle.visibility = View.VISIBLE
                } else if (isShow) {
                    isShow = false
                    binding.toolbarTitle.visibility = View.GONE
                }
            }
        })
    }

    private fun setObserver() {
        viewModel.receiversList.observe(this, { (status, data, message) ->
            when (status) {
                Status.SUCCESS -> {
                    val model: Invoice = intent.getParcelableExtra(AppConstant.MODEL) ?: Invoice()
                    val fragments = ArrayList<PlaceholderFragment>()
                    var i = 0
                    while (data != null && i < data.size) {
                        if (i < 3) {
                            val receiverModel = data[i]
                            try {
                                totalDamage += receiverModel.shortage.trim { it <= ' ' }.toInt()
                                totalDeliveredQuantity += receiverModel.bagsRecv.trim { it <= ' ' }.toInt()
                            } catch (e: NumberFormatException) {
                                Log.e("[NumberFormatException]", e.message!!)
                            }
                        }
                        i++
                    }
                    val receiver = if (data == null || data.size == 0) {
                        Receiver(model.shiptopartyName, model.shiptopartyMobileno, "", "", "", if (model.loadType.equals("standard", ignoreCase = true)) 0 else 1, "", "", 1, null)
                    } else {
                        data[0]
                    }
                    fragments.add(newInstance(1, model, receiver))
                    fragments.add(newInstance(2, model, if (data != null && data.size > 1) data[1] else null))
                    fragments.add(newInstance(3, model, if (data != null && data.size > 2) data[2] else null))
                    val sectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager, fragments)
                    binding.viewPager.adapter = sectionsPagerAdapter
                    binding.tabLayout.setupWithViewPager(binding.viewPager)
                    binding.loadingLayout.visibility = View.GONE
                }
                Status.ERROR, Status.OFFLINE -> {
                    binding.loadingLayout.visibility = View.GONE
                    showAlertDialog(this, message, null)
                }
                Status.LOADING -> binding.loadingLayout.visibility = View.VISIBLE
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_logout -> {
                logout(this)
            }
            android.R.id.home -> {
                finish()
            }
            R.id.menu_change_language -> {
                val intent = Intent(this, LanguageSelectorActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        var totalDamage = 0
        var totalQuantity = 0
        var totalDeliveredQuantity = 0
    }
}