package com.hust.baseweb.applications.salesroutes.controller;

import com.hust.baseweb.applications.customer.entity.PartyRetailOutlet;
import com.hust.baseweb.applications.salesroutes.entity.*;
import com.hust.baseweb.applications.salesroutes.model.salesmancheckinout.SalesmanCheckInOutInputModel;
import com.hust.baseweb.applications.salesroutes.model.salesrouteconfig.CreateSalesRouteConfigInputModel;
import com.hust.baseweb.applications.salesroutes.model.salesrouteconfig.GetListSalesRouteConfigOutputModel;
import com.hust.baseweb.applications.salesroutes.model.salesrouteconfig.GetSalesRouteConfigInputModel;
import com.hust.baseweb.applications.salesroutes.model.salesrouteconfigcustomer.CreateSalesRouteConfigRetailOutletInputModel;
import com.hust.baseweb.applications.salesroutes.model.salesroutedetail.GenerateSalesRouteDetailInputModel;
import com.hust.baseweb.applications.salesroutes.model.salesroutedetail.GetCustomersVisitedBySalesmanDayInputModel;
import com.hust.baseweb.applications.salesroutes.model.salesroutedetail.GetCustomersVisitedDayOfUserLogin;
import com.hust.baseweb.applications.salesroutes.model.salesrouteplanningperiod.CreateSalesRoutePlanningPeriodInputModel;
import com.hust.baseweb.applications.salesroutes.model.salesrouteplanningperiod.GetSalesRoutePlanningPeriodInputModel;
import com.hust.baseweb.applications.salesroutes.model.salesrouteplanningperiod.GetSalesRoutePlanningPeriodOutputModel;
import com.hust.baseweb.applications.salesroutes.model.salesroutevisitfrequency.ListSalesRouteVisitFrequencyOutputModel;
import com.hust.baseweb.applications.salesroutes.repo.SalesRouteVisitFrequencyRepo;
import com.hust.baseweb.applications.salesroutes.service.*;
import com.hust.baseweb.entity.UserLogin;
import com.hust.baseweb.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@Log4j2
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SalesRouteAPIController {

    private SalesRouteConfigService salesRouteConfigService;
    private SalesRouteConfigRetailOutletService salesRouteConfigRetailOutletService;
    private SalesRoutePlanningPeriodService salesRoutePlanningPeriodService;
    private SalesRouteVisitFrequencyRepo salesRouteVisitFrequencyRepo;
    private SalesRouteDetailService salesRouteDetailService;
    private UserService userService;
    private SalesmanCheckinHistoryService salesmanCheckinService;

    @PostMapping("/salesman-checkin-customer")
    public ResponseEntity<?> salesmanCheckInCustomer(Principal principal, @RequestBody SalesmanCheckInOutInputModel input) {
        UserLogin userLogin = userService.findById(principal.getName());
        SalesmanCheckinHistory sch = salesmanCheckinService.save(userLogin, input.getPartyId(), "Y", input.getLatitude() + "," + input.getLongitude());
        return ResponseEntity.ok().body(sch);
    }

    @PostMapping("/salesman-checkout-customer")
    public ResponseEntity<?> salesmanCheckOutCustomer(Principal principal, @RequestBody SalesmanCheckInOutInputModel input) {
        UserLogin userLogin = userService.findById(principal.getName());
        SalesmanCheckinHistory sch = salesmanCheckinService.save(userLogin, input.getPartyId(), "N", input.getLatitude() + "," + input.getLongitude());
        return ResponseEntity.ok().body(sch);
    }

    @GetMapping("/salesman-checkin-history")
    public ResponseEntity<?> getSalesmanCheckInHistory(Principal principal, Pageable page, @RequestParam(required = false) String param) {
        UserLogin userLogin = userService.findById(principal.getName());

        log.info("getSalesmanCheckInHistory, user = " + userLogin.getUserLoginId());

        Page<SalesmanCheckinHistory> list = salesmanCheckinService.findAll(page);
        return ResponseEntity.ok().body(list);
    }


    @PostMapping("/create-sales-route-config")
    public ResponseEntity<?> createSalesRouteConfig(Principal principal,
                                                    @RequestBody CreateSalesRouteConfigInputModel input) {
        log.info("createSalesRouteConfig, days = " + input.getDays() + ", repeatWeek = " + input.getRepeatWeek());

        SalesRouteConfig salesRouteConfig = salesRouteConfigService.save(input.getDays(), input.getRepeatWeek());

        return ResponseEntity.ok().body(salesRouteConfig);
    }
    @PostMapping("/get-list-sales-route-config")
    public ResponseEntity<?> getListSalesRouteConfig(Principal prinicpal, @RequestBody GetSalesRouteConfigInputModel input){
        List<SalesRouteConfig> salesRouteConfigs = salesRouteConfigService.findAll();
        return ResponseEntity.ok().body(new GetListSalesRouteConfigOutputModel(salesRouteConfigs));
    }

    //@PostMapping("/create-sales-route-config-customer")
    @PostMapping("/create-sales-route-config-retail-outlet")
    public ResponseEntity<?> createSalesRouteConfigRetailOutlet(Principal principal, @RequestBody CreateSalesRouteConfigRetailOutletInputModel input) {
        log.info("createSalesRouteConfigRetailOutlet, salesRouteConfigId = " + input.getSalesRouteConfigId() );

        SalesRouteConfigRetailOutlet salesRouteConfigRetailOutlet = salesRouteConfigRetailOutletService.save(
                input.getRetailOutletSalesmanVendorId(),
                input.getVisitFrequencyId(),
                input.getSalesRouteConfigId(),
                input.getSalesRoutePlanningPeriodId(),
                input.getStartExecuteDate());

        return ResponseEntity.ok().body(salesRouteConfigRetailOutlet);
    }

    @PostMapping("/create-sales-route-planning-period")
    public ResponseEntity<?> createSalesRoutePlanningPeriod(Principal principal, @RequestBody CreateSalesRoutePlanningPeriodInputModel input) {
        log.info("createSalesRoutePlanningPeriod, fromDate = " + input.getFromDate() + ", toDate = " + input.getToDate() + ", description = " + input.getDescription());
        SalesRoutePlanningPeriod salesRoutePlanningPeriod = salesRoutePlanningPeriodService.save(input.getFromDate(), input.getToDate(), input.getDescription());

        return ResponseEntity.ok().body(salesRoutePlanningPeriod);
    }
    @PostMapping("/get-list-sales-route-planning-period")
    public ResponseEntity<?> getListSalesRoutePlanningPeriod(Principal principal, @RequestBody GetSalesRoutePlanningPeriodInputModel input){
        List<SalesRoutePlanningPeriod> salesRoutePlanningPeriodList = salesRoutePlanningPeriodService.findAll();
        return ResponseEntity.ok().body(new GetSalesRoutePlanningPeriodOutputModel(salesRoutePlanningPeriodList));

    }
    @GetMapping("/get-list-sales-route-visit-frequency")
    public ResponseEntity<?> getListSalesRouteVisitFrequency(Principal principal){
        List<SalesRouteVisitFrequency> salesRouteVisitFrequencies = salesRouteVisitFrequencyRepo.findAll();
        return ResponseEntity.ok().body(new ListSalesRouteVisitFrequencyOutputModel(salesRouteVisitFrequencies));
    }

    @PostMapping("/generate-sales-route-detail")
    public ResponseEntity<?> generateSalesRouteDetail(Principal principal, @RequestBody GenerateSalesRouteDetailInputModel input) {
        log.info("generateSalesRouteDetail, salesmanId = " + input.getPartySalesmanId());
        int cnt = salesRouteDetailService.generateSalesRouteDetailOfSalesman(input.getPartySalesmanId(), input.getSalesRoutePlanningPeriodId());
        return ResponseEntity.ok().body(cnt);
    }

    //@PostMapping("/get-customers-visited-salesman-date")
    @PostMapping("/get-retail-outlets-visited-salesman-date")
    public ResponseEntity<?> getCustomersVisitedSalesmanDay(Principal principal, @RequestBody GetCustomersVisitedBySalesmanDayInputModel input) {
        List<PartyRetailOutlet> customers = salesRouteDetailService.getRetailOutletsVisitedSalesmanDay(input.getPartySalesmanId(), input.getDate());
        return ResponseEntity.ok().body(customers);
    }

    //@PostMapping("/get-customers-visited-date-of-user-login")
    @PostMapping("/get-retail-outlets-visited-date-of-user-login")
    public ResponseEntity<?> getCustomersVisitedDateOfUserLogin(Principal principal, @RequestBody GetCustomersVisitedDayOfUserLogin input) {
        UserLogin userLogin = userService.findById(principal.getName());
        UUID partySalesmanId = userLogin.getParty().getPartyId();
        log.info("getCustomersVisitedDateOfUserLogin, partySalesmanId = " + partySalesmanId + ", date = " + input.getDate());
        List<PartyRetailOutlet> customers = salesRouteDetailService.getRetailOutletsVisitedSalesmanDay(partySalesmanId, input.getDate());
        return ResponseEntity.ok().body(customers);
    }
}
