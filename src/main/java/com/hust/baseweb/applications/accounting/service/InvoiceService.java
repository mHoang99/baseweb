package com.hust.baseweb.applications.accounting.service;

import com.hust.baseweb.applications.accounting.document.Invoice;

import java.util.List;

/**
 * @author Hien Hoang (hienhoang2702@gmail.com)
 */
public interface InvoiceService {

    List<Invoice.Model> getAllInvoice();

    List<Invoice.Model> getAllUnpaidInvoices();

    List<Invoice.DistributorUnpaidModel> getAllUnpaidInvoiceGroupByDistributor();

    Invoice.DistributorUnpaidModel getUnpaidInvoiceByDistributor(String distributorId);

    Invoice.Model getInvoice(String invoiceId);

    Invoice save(Invoice invoice);

    List<Invoice> saveAll(List<Invoice> invoices);
}
