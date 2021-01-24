package com.hust.baseweb.applications.postsys.controller;

import com.hust.baseweb.applications.postsys.model.postdriver.PostDriverUpdateInputModel;
import com.hust.baseweb.applications.postsys.model.postdriver.UpdatePostDriverPostOfficeAssignmentInputModel;
import com.hust.baseweb.applications.postsys.model.postman.PostmanUpdateInputModel;
import com.hust.baseweb.applications.postsys.model.postman.SolvePostmanPostOrderAssignmentTspInputModel;
import com.hust.baseweb.applications.postsys.model.postman.UpdatePostmanPostOrderAssignmentInputModel;
import com.hust.baseweb.applications.postsys.service.*;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.List;

@Controller
@Log4j2(topic = "POST_LOG")
public class PostmanController {

    @Autowired
    private PostOfficeService postOfficeService;
    @Autowired
    private PostCustomerService postCustomerService;
    @Autowired
    private PostOrderService postOrderService;
    @Autowired
    PostPackageTypeService postPackageTypeService;
    @Autowired
    PostTripService postTripService;
    @Autowired
    PostmanService postmanService;
    @Autowired
    private PostDriverService postDriverService;


    @GetMapping("/get-postman-list")
    public ResponseEntity getPostmanList() {
        return ResponseEntity.ok().body(postmanService.findAll());
    }

    @GetMapping("/get-postman-list/{postOfficeId}")
    public ResponseEntity getPostmanList(@PathVariable String postOfficeId) {
        return ResponseEntity.ok().body(postmanService.findByPostOfficeId(postOfficeId));
    }

    @PostMapping("/update-postman")
    public ResponseEntity updatePostman(@RequestBody PostmanUpdateInputModel postmanUpdateInputModel) {
        return ResponseEntity.ok().body(postmanService.updatePostman(postmanUpdateInputModel));
    }

    @GetMapping("/get-post-driver-list")
    public ResponseEntity getPostDriverList() {
        return ResponseEntity.ok().body(postDriverService.findAll());
    }

    @PostMapping("/update-post-driver")
    public ResponseEntity updatePostDriver(@RequestBody PostDriverUpdateInputModel postDriverUpdateInputModel) {
        return ResponseEntity.ok().body(postDriverService.updatePostDriver(postDriverUpdateInputModel));
    }

    @PostMapping("/update-post-driver-post-office-assignment")
    public ResponseEntity updatePostDriverPostOfficeAssignment(
        @RequestBody
            UpdatePostDriverPostOfficeAssignmentInputModel updatePostDriverPostOfficeAssignmentInputModel
    ) {
        return ResponseEntity
            .ok()
            .body(postDriverService.updatePostDriverPostOfficeAssignment(updatePostDriverPostOfficeAssignmentInputModel));
    }

    @ApiOperation(value = "Lấy danh sách đơn hàng đã đưọc phân bổ cho postman")
    @GetMapping("/get-order-by-postman-and-date")
    public ResponseEntity getOrderByPostmanAndDate(
        @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy")
            Date fromDate,
        @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") Date toDate,
        Principal principal
    ) {
        return ResponseEntity.ok().body(postmanService.findOrdersByPostmanAndDate(principal, fromDate, toDate));
    }


    @ApiOperation(value = "Lấy bưu cục của postman")
    @GetMapping("/get-post-office-by-postman")
    public ResponseEntity getPostOfficeByPostman(Principal principal) {
        return ResponseEntity.ok().body(postmanService.getPostOfficeByPostman(principal));
    }

    @ApiOperation(value = "Postman cập nhật trạng thái đơn hàng đã đưọc phân bổ")
    @PostMapping("/update-postman-post-order-assignment")
    public ResponseEntity updatePostmanPostOrderAssignment(
        @RequestBody UpdatePostmanPostOrderAssignmentInputModel updatePostmanPostOrderAssignmentInputModel
    ) {
        return ResponseEntity
            .ok()
            .body(postmanService.updatePostOrderAssignment(updatePostmanPostOrderAssignmentInputModel));
    }

    @PostMapping("/solve-postman-post-order-assignment-tsp")
    public ResponseEntity solvePostmanPostOrderAssignmentTsp(
        @RequestBody
            SolvePostmanPostOrderAssignmentTspInputModel solvePostmanPostOrderAssignmentTspInputModel,
        @RequestParam("pick") Boolean pick,
        @RequestParam("postOfficeId") String postOfficeId
    ) {
        return ResponseEntity
            .ok()
            .body(postmanService.solveAssignmentTsp(solvePostmanPostOrderAssignmentTspInputModel));
    }

    @GetMapping("/get-postman-list-order/{postOfficeId}")
    public ResponseEntity getPostmanListAndOrderList(@PathVariable String postOfficeId) {
        return ResponseEntity.ok().body(postmanService.findOrdersByPostOfficeId(postOfficeId));
    }


}
