package com.ipssi.orient_epod.callbacks;

import com.ipssi.orient_epod.model.Invoice;

public interface OnInvoiceSelectedListener {
    void onInvoiceSelected(Invoice invoice);
    void onLrIconClicked(String link);
}
