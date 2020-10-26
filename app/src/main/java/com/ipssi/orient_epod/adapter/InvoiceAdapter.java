package com.ipssi.orient_epod.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ipssi.orient_epod.callbacks.OnInvoiceSelectedListener;
import com.ipssi.orient_epod.databinding.InvoiceViewBinding;
import com.ipssi.orient_epod.model.Invoice;

import java.util.ArrayList;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.ViewHolder> {

    private final OnInvoiceSelectedListener invoiceSeletecListener;
    private ArrayList<Invoice> dataList;

    public InvoiceAdapter(OnInvoiceSelectedListener listener) {
        this.invoiceSeletecListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(InvoiceViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Invoice invoice = dataList.get(position);
        holder.bind(invoice);
        holder.onItemClick(invoice);
        holder.onLrClick(invoice);
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    public void setData(ArrayList<Invoice> data) {
        dataList = data;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        InvoiceViewBinding binding;

        public void bind(Invoice invoice) {
            binding.setModel(invoice);
        }

        public void onLrClick(Invoice invoice) {
            binding.lrNumberValue.setOnClickListener(v -> invoiceSeletecListener.onLrIconClicked(invoice.getLrLink()));
        }

        public void onItemClick(Invoice invoice) {
            binding.getRoot().setOnClickListener(v -> invoiceSeletecListener.onInvoiceSelected(invoice));
        }

        public ViewHolder(@NonNull InvoiceViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
