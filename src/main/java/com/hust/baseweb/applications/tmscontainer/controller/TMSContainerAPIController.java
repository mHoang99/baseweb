package com.hust.baseweb.applications.tmscontainer.controller;

import com.hust.baseweb.applications.geo.entity.GeoPoint;
import com.hust.baseweb.applications.geo.entity.PostalAddress;
import com.hust.baseweb.applications.geo.repo.GeoPointRepo;
import com.hust.baseweb.applications.geo.repo.PostalAddressJpaRepo;
import com.hust.baseweb.applications.geo.repo.PostalAddressRepo;
import com.hust.baseweb.applications.tmscontainer.entity.*;
import com.hust.baseweb.applications.tmscontainer.model.*;
import com.hust.baseweb.applications.tmscontainer.repo.*;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Log4j2
public class TMSContainerAPIController {
    private ContPortPagingRepo contPortPagingRepo;
    private ContPortRepo contPortRepo;
    private PostalAddressRepo postalAddressRepo;
    private GeoPointRepo geoPointRepo;
    private ContContainerTypeRepo contContainerTypeRepo;
    private ContContainerRepo contContainerRepo;
    private ContDepotContainerRepo contDepotContainerRepo;
    private ContDepotContainerPagingRepo contDepotContainerPagingRepo;
    private ContContainerPagingRepo contContainerPagingRepo;
    private ContDepotTrailerRepo contDepotTrailerRepo;
    private ContDepotTrailerPagingRepo contDepotTrailerPagingRepo;
    private ContDepotTruckRepo contDepotTruckRepo;
    private ContDepotTruckPagingRepo contDepotTruckPagingRepo;
    private ContTrailerPagingRepo contTrailerPagingRepo;
    private ContTrailerRepo contTrailerRepo;
    private PostalAddressJpaRepo postalAddressJpaRepo;


    @PostMapping("/create-port")
    public void createPort(@RequestBody InputContPortModel input){
        ContPort contPort = new ContPort();
        contPort.setPortId(input.getPortId());
        contPort.setPortName(input.getPortName());
        GeoPoint geoPoint = new GeoPoint();
        geoPoint.setLatitude(Double.parseDouble(input.getLat()));
        geoPoint.setLongitude(Double.parseDouble(input.getLng()));
        PostalAddress postalAddress = new PostalAddress();
        postalAddress.setAddress(input.getAddress());
        postalAddress.setGeoPoint(geoPoint);
        contPort.setPostalAddress(postalAddress);
        geoPointRepo.save(geoPoint);
        postalAddressRepo.save(postalAddress);
        contPortRepo.save(contPort);
    }


    @GetMapping("/get-list-cont-port-page")
    public ResponseEntity<?> getListContPortPage(Pageable pageable){
        Page<ContPort> contDepotContainerPage = contPortPagingRepo.findAll(pageable);
        for (ContPort contPort: contDepotContainerPage) {
            contPort.setAddress(contPort.getPostalAddress().getAddress());
        }
        return ResponseEntity.ok(contDepotContainerPage);
    }

    @GetMapping("/get-list-cont-port")
    public ResponseEntity<?> getListContPort(){
        List<ContPort> contPorts = contPortRepo.findAll();
        for (ContPort contDepotContainer: contPorts) {
            contDepotContainer.setLat(contDepotContainer.getPostalAddress().getGeoPoint().getLatitude());
            contDepotContainer.setLng(contDepotContainer.getPostalAddress().getGeoPoint().getLongitude());
        }
        return ResponseEntity.ok(new OutputContPortModel(contPorts));
    }

    @GetMapping("/get-list-cont-container")
    public ResponseEntity<?> getListContContainer(Pageable pageable){
        log.info("getListContContainerPage");
        Page<ContContainer> contContainerPage = contContainerPagingRepo.findAll(pageable);
        log.info("size {}",contContainerPage.getSize());
        for (ContContainer contContainer: contContainerPage){
            contContainer.setContainerType(contContainer.getContContainerType().getDescription());
        }
        return ResponseEntity.ok(contContainerPage);

    }

    @GetMapping("/get-list-container-type")
    public ResponseEntity getListContainerType(){
        log.info("getListContainerType");
        List<ContContainerType> contContainerTypes = contContainerTypeRepo.findAll();
        return ResponseEntity.ok().body(new OutputContContainerTypeModel(contContainerTypes));
    }

    @PostMapping("/save-container-to-db")
    public void saveContainer(@RequestBody InputContainerModel input){
        log.info("saveContainer");
        ContContainer contContainer = new ContContainer();
        contContainer.setContainerId(input.getContainerId());
        contContainer.setContainerName(input.getContainerName());
        contContainer.setContContainerType(contContainerTypeRepo.findByContainerTypeId(input.getContainerType()));
        contContainerRepo.save(contContainer);
    }




    @PostMapping("/create-depot-container")
    public void createDepotContainer(@RequestBody InputDepotContainerModel input){
        log.info("createDepotContainer");
        ContDepotContainer contDepotContainer = new ContDepotContainer();
        contDepotContainer.setDepotContainerId(input.getDepotContainerId());
        contDepotContainer.setDepotContainerName(input.getDepotContainerName());
        PostalAddress postalAddress = new PostalAddress();
        if(input.getContactMechId() != null){
            postalAddress = postalAddressJpaRepo.findByContactMechId(UUID.fromString(input.getContactMechId()));
        }else{
            GeoPoint geoPoint = new GeoPoint();
            geoPoint.setLatitude(Double.parseDouble(input.getLat()));
            geoPoint.setLongitude(Double.parseDouble(input.getLng()));

            postalAddress.setAddress(input.getAddress());
            postalAddress.setGeoPoint(geoPoint);
            geoPointRepo.save(geoPoint);
        }

        contDepotContainer.setPostalAddress(postalAddress);






        postalAddressRepo.save(postalAddress);
        contDepotContainerRepo.save(contDepotContainer);
        log.info("input : {}", input.toString());
    }

    @GetMapping("/get-list-depot-container-page")
    public ResponseEntity<?> getListDepotContainerPage(Pageable pageable){
        Page<ContDepotContainer> contDepotContainerPage = contDepotContainerPagingRepo.findAll(pageable);
        for (ContDepotContainer contDepotContainer: contDepotContainerPage) {
            contDepotContainer.setAddress(contDepotContainer.getPostalAddress().getAddress());
        }
        return ResponseEntity.ok(contDepotContainerPage);
    }

    @GetMapping("/get-list-depot-container")
    public ResponseEntity<?> getListDepotContainer(){
        List<ContDepotContainer> depotContainers = contDepotContainerRepo.findAll();
        for (ContDepotContainer contDepotContainer: depotContainers) {
            contDepotContainer.setLat(contDepotContainer.getPostalAddress().getGeoPoint().getLatitude());
            contDepotContainer.setLng(contDepotContainer.getPostalAddress().getGeoPoint().getLongitude());
        }
        return ResponseEntity.ok(new OutputContDepotContainerModel(depotContainers));
    }


    @PostMapping("/create-depot-trailer")
    public void createDepotTrailer(@RequestBody InputDepotTrailerModel input){
        ContDepotTrailer contDepotTrailer = new ContDepotTrailer();
        contDepotTrailer.setDepotTrailerId(input.getDepotTrailerId());
        contDepotTrailer.setDepotTrailerName(input.getDepotTrailerName());
        GeoPoint geoPoint = new GeoPoint();
        geoPoint.setLatitude(Double.parseDouble(input.getLat()));
        geoPoint.setLongitude(Double.parseDouble(input.getLng()));
        PostalAddress postalAddress = new PostalAddress();
        postalAddress.setAddress(input.getAddress());
        postalAddress.setGeoPoint(geoPoint);
        contDepotTrailer.setPostalAddress(postalAddress);
        geoPointRepo.save(geoPoint);
        postalAddressRepo.save(postalAddress);
        contDepotTrailerRepo.save(contDepotTrailer);
    }


    @GetMapping("/get-list-depot-trailer-page")
    public ResponseEntity<?> getListDepotTrailerPage(Pageable pageable){
        Page<ContDepotTrailer> contDepotTrailerPage = contDepotTrailerPagingRepo.findAll(pageable);
        for (ContDepotTrailer contDepotTrailer: contDepotTrailerPage) {
            contDepotTrailer.setAddress(contDepotTrailer.getPostalAddress().getAddress());
        }
        return ResponseEntity.ok(contDepotTrailerPage);
    }

    @GetMapping("/get-list-depot-trailer")
    public ResponseEntity<?> getListDepotTrailer(){
        List<ContDepotTrailer> depotTrailers = contDepotTrailerRepo.findAll();
        for (ContDepotTrailer contDepotTrailer: depotTrailers) {
            contDepotTrailer.setLat(contDepotTrailer.getPostalAddress().getGeoPoint().getLatitude());
            contDepotTrailer.setLng(contDepotTrailer.getPostalAddress().getGeoPoint().getLongitude());
        }
        return ResponseEntity.ok(new OutputContDepotTrailerModel(depotTrailers));
    }




    @PostMapping("/create-depot-truck")
    public void createDepotTruck(@RequestBody InputDepotTruckModel input){
        ContDepotTruck contDepotTruck = new ContDepotTruck();
        contDepotTruck.setDepotTruckId(input.getDepotTruckId());
        contDepotTruck.setDepotTruckName(input.getDepotTruckName());
        GeoPoint geoPoint = new GeoPoint();
        geoPoint.setLatitude(Double.parseDouble(input.getLat()));
        geoPoint.setLongitude(Double.parseDouble(input.getLng()));
        PostalAddress postalAddress = new PostalAddress();
        postalAddress.setAddress(input.getAddress());
        postalAddress.setGeoPoint(geoPoint);
        contDepotTruck.setPostalAddress(postalAddress);
        geoPointRepo.save(geoPoint);
        postalAddressRepo.save(postalAddress);
        contDepotTruckRepo.save(contDepotTruck);
    }

    @GetMapping("/get-list-depot-truck-page")
    public ResponseEntity<?> getListDepotTruckPage(Pageable pageable){
        Page<ContDepotTruck> contDepotTruckPage = contDepotTruckPagingRepo.findAll(pageable);
        for (ContDepotTruck contDepotTruck: contDepotTruckPage) {
            contDepotTruck.setAddress(contDepotTruck.getPostalAddress().getAddress());
        }
        return ResponseEntity.ok(contDepotTruckPage);
    }

    @GetMapping("/get-list-depot-truck")
    public ResponseEntity<?> getListDepotTruck(){
        List<ContDepotTruck> contDepotTrucks = contDepotTruckRepo.findAll();
        for (ContDepotTruck contDepotTruck: contDepotTrucks) {
            contDepotTruck.setLat(contDepotTruck.getPostalAddress().getGeoPoint().getLatitude());
            contDepotTruck.setLng(contDepotTruck.getPostalAddress().getGeoPoint().getLongitude());
        }
        return ResponseEntity.ok(new OutputContDepotTruckModel(contDepotTrucks));
    }


    @GetMapping("/get-list-cont-trailer")
    public ResponseEntity<?> getListContTrailer(Pageable pageable){
        Page<ContTrailer> contTrailers = contTrailerPagingRepo.findAll(pageable);

        return ResponseEntity.ok(contTrailers);

    }

    @PostMapping("/save-trailer-to-db")
    void saveTrailer(@RequestBody InputTrailerModel input){
        ContTrailer contTrailer = new ContTrailer();
        contTrailer.setTrailerId(input.getTrailerId());
        contTrailer.setDescription(input.getDescription());
        contTrailerRepo.save(contTrailer);
    }

    @PostMapping("/get-list-address-suggestion")
    ResponseEntity<?> getListAddressSuggestion(@RequestBody AddressInputForSuggestion input){
        log.info("getListPostalAddressSuggestion");
        if(input == null || input.getAddress() == null ){
            PostalAddress postalAddress = new PostalAddress();
            postalAddress.setAddress("");
            List<PostalAddress> postalAddressList = new ArrayList<PostalAddress>();
            postalAddressList.add(postalAddress);
            return ResponseEntity.ok(new OutputPostalAddressSuggestion(postalAddressList));
        }
        List<PostalAddress> postalAddressList = postalAddressJpaRepo.findTop5ByAddressContaining(input.getAddress());
        //PostalAddress postalAddress = new PostalAddress();
        //postalAddress.setAddress(input.getAddress());
        //postalAddressList.add(postalAddress);
        for(PostalAddress postalAddress: postalAddressList){
            postalAddress.setLat(postalAddress.getGeoPoint().getLatitude());
            postalAddress.setLng(postalAddress.getGeoPoint().getLongitude());
        }
        return ResponseEntity.ok(new OutputPostalAddressSuggestion(postalAddressList));
    }





}
