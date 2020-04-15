package com.hust.baseweb.applications.tms.entity;

import com.hust.baseweb.applications.geo.entity.PostalAddress;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContDepotContainer {
    @Id
    @Column(name = "depot_container_id")
    private String depotContainerId;

    @Column(name = "depot_container_name")
    private String depotContainerName;

    @JoinColumn(name = "contact_mech_id", referencedColumnName = "contact_mech_id")
    @ManyToOne
    private PostalAddress postalAddress;

    @Column(name = "last_updated_stamp")
    private Date lastUpdatedStamp;

    @Column(name = "created_stamp")
    private Date createdStamp;

    @Transient
    private String address;

    @Transient
    private Double lat;

    @Transient
    private Double lng;
}
