package com.hust.baseweb.applications.customer.service;

import com.hust.baseweb.applications.customer.entity.PartyContactMechPurpose;
import com.hust.baseweb.applications.customer.entity.PartyRetailOutlet;
import com.hust.baseweb.applications.customer.model.CreateRetailOutletInputModel;
import com.hust.baseweb.applications.customer.repo.PartyContactMechPurposeRepo;
import com.hust.baseweb.applications.customer.repo.PartyRetailOutletRepo;
import com.hust.baseweb.applications.customer.repo.RetailOutletPagingRepo;
import com.hust.baseweb.applications.geo.entity.GeoPoint;
import com.hust.baseweb.applications.geo.entity.PostalAddress;
import com.hust.baseweb.applications.geo.repo.GeoPointRepo;
import com.hust.baseweb.applications.geo.repo.PostalAddressRepo;
import com.hust.baseweb.entity.Party;
import com.hust.baseweb.entity.PartyType;
import com.hust.baseweb.entity.Status;
import com.hust.baseweb.repo.PartyRepo;
import com.hust.baseweb.repo.PartyTypeRepo;
import com.hust.baseweb.repo.StatusRepo;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Log4j2
public class RetailOutletServiceImpl implements  RetailOutletService {
    private RetailOutletPagingRepo retailOutletRepo;
    private PartyRetailOutletRepo partyRetailOutletRepo;
    private GeoPointRepo geoPointRepo;
    private PostalAddressRepo postalAddressRepo;
    private PartyRepo partyRepo;
    private PartyTypeRepo partyTypeRepo;
    private StatusRepo statusRepo;
    private PartyContactMechPurposeRepo partyContactMechPurposeRepo;

    @Override
    @Transactional
    public PartyRetailOutlet save(CreateRetailOutletInputModel input) {
        PartyType partyType = partyTypeRepo.findByPartyTypeId("PARTY_RETAILOUTLET");

        //UUID partyId = UUID.randomUUID();
        //Party party = new Party();
        //party.setPartyId(partyId);// KHONG WORK vi partyId khi insert vao DB se duoc sinh tu dong, no se khac voi partyId sinh ra boi SPRING
        Party party = new Party(null, partyTypeRepo.getOne(PartyType.PartyTypeEnum.PERSON.name()), "",
                statusRepo.findById(Status.StatusEnum.PARTY_ENABLED.name()).orElseThrow(NoSuchElementException::new),
                false);
        party.setType(partyType);

        partyRepo.save(party);

        UUID partyId = party.getPartyId();
        log.info("save party " + partyId);

        PartyRetailOutlet retailOutlet = new PartyRetailOutlet();
        retailOutlet.setPartyId(partyId);
        retailOutlet.setRetailOutletCode(input.getRetailOutletCode());
        //customer.setParty(party);
        retailOutlet.setPartyType(partyType);
        retailOutlet.setRetailOutletName(input.getRetailOutletName());
        retailOutlet.setPostalAddress(new ArrayList<>());

        log.info("save, prepare save retailOutlet partyId = " + retailOutlet.getPartyId());
        retailOutlet = retailOutletRepo.save(retailOutlet);
//        customerRepo.save(customer);

        GeoPoint geoPoint = new GeoPoint();
        //UUID geoPointId = UUID.randomUUID();
        geoPoint.setLatitude(input.getLatitude());
        geoPoint.setLongitude(input.getLongitude());
        //geoPoint.setGeoPointId(geoPointId);// KHONG WORK vi khi save vao DB thi geoPointId se duoc sinh voi DB engine
        geoPoint = geoPointRepo.save(geoPoint);
        UUID geoPointId = geoPoint.getGeoPointId();

        //UUID contactMechId = UUID.randomUUID();
        PostalAddress address = new PostalAddress();
        //address.setContactMechId(contactMechId);// KHONG WORL vi contactMechId se duoc sinh tu dong boi DB uuid_generate_v1()
        address.setGeoPoint(geoPoint);

        address.setAddress(input.getAddress());


        address = postalAddressRepo.save(address);
        UUID contactMechId = address.getContactMechId();


        log.info("save, start save party_contact_mech_purpose");
        // write to PartyContactMech
        PartyContactMechPurpose partyContactMechPurpose = new PartyContactMechPurpose();
        partyContactMechPurpose.setContactMechId(contactMechId);
        partyContactMechPurpose.setPartyId(partyId);
        partyContactMechPurpose.setContactMechPurposeTypeId("PRIMARY_LOCATION");
        partyContactMechPurpose.setFromDate(new Date());
        partyContactMechPurposeRepo.save(partyContactMechPurpose);


        return retailOutlet;

    }

    @Override
    public List<PartyRetailOutlet> findAll() {
        return partyRetailOutletRepo.findAll();
    }
}
