package com.ipssi.orient_epod

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.google.android.material.snackbar.Snackbar
import com.ipssi.orient_epod.databinding.FragmentWebViewBinding
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


private const val ARG_PARAM1 = "param1"

class WebViewFragment : Fragment() {
    private lateinit var permissionStatus: SharedPreferences
    private lateinit var binding: FragmentWebViewBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    inner class PDFWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            view?.loadUrl(url)
            return true
        }
    }

    inner class PDFChromeClient : WebChromeClient(){

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            binding.progressView.visibility = View.VISIBLE
            binding.progressView.progress = newProgress
            if(newProgress == 100){
                binding.progressView.visibility = View.GONE
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionStatus = requireContext().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        arguments?.let {
            val link = it.getString(ARG_PARAM1)
            binding.pdfView.settings.javaScriptEnabled = true
            binding.pdfView.settings.loadsImagesAutomatically = true
            binding.pdfView.webViewClient = PDFWebViewClient()
            binding.pdfView.webChromeClient = PDFChromeClient()
            binding.pdfView.loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=$link")
            binding.lrViewLayout.visibility = View.VISIBLE
        }

        binding.btnDownload.setOnClickListener {
            checkPermissionDownloadPDF()


        }
    }

    private fun downloadPDF() {
        arguments?.let {
            val link = it.getString(ARG_PARAM1)
            val extStorageDirectory = Environment.getExternalStorageDirectory().toString()
            val folder = File(extStorageDirectory, "OrientEpod")
            folder.mkdir()
            val fileName = link?.substringAfterLast("/")
            PRDownloader.download(link, extStorageDirectory, fileName).build().start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    Snackbar.make(binding.root, "Download Complete", Snackbar.LENGTH_SHORT).show()
                }

                override fun onError(error: Error?) {
                    if (error?.isConnectionError == true) {
                        Snackbar.make(binding.root, "Connection Error", Snackbar.LENGTH_SHORT).show()
                    } else {
                        Snackbar.make(binding.root, "Server Error", Snackbar.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    private fun checkPermissionDownloadPDF() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                downloadPDF()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    val alert = AlertDialog.Builder(requireContext())
                    alert.setMessage("Storage read and write permissions are required to store downloaded pdf file")
                        .setPositiveButton("Allow") { _: DialogInterface?, _: Int -> requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 2) }
                        .setNegativeButton("cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }.show()
                } else {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 2)
                }
            }
        } else {
            downloadPDF()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            downloadPDF()
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String?) =
            WebViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}