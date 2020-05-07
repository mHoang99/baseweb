package com.hust.baseweb.applications.accounting.document;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author Hien Hoang (hienhoang2702@gmail.com)
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document
public class OrderItemBilling {

    @org.springframework.data.annotation.Id
    private Id id;
    private Integer quantity;            // int,
    private Double amount;              // decimal(18, 2),
    private String currencyUomId;
    private Date lastUpdatedStamp;  // TIMESTAMP,
    private Date createdStamp;       // TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    public static class Id {
        private String orderId;            // varchar(60),
        private String orderItemSeqId;   // varchar(60),
        private String invoiceId;          // varchar(60),
        private String invoiceItemSeqId; // varchar(60),
    }
}
