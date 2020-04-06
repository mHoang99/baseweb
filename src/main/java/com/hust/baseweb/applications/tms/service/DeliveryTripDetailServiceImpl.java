package com.hust.baseweb.applications.tms.service;

import com.hust.baseweb.applications.geo.entity.GeoPoint;
import com.hust.baseweb.applications.logistics.repo.ProductRepo;
import com.hust.baseweb.applications.tms.entity.DeliveryTrip;
import com.hust.baseweb.applications.tms.entity.DeliveryTripDetail;
import com.hust.baseweb.applications.tms.entity.ShipmentItem;
import com.hust.baseweb.applications.tms.entity.status.DeliveryTripDetailStatus;
import com.hust.baseweb.applications.tms.entity.status.ShipmentItemStatus;
import com.hust.baseweb.applications.tms.model.DeliveryTripDetailModel;
import com.hust.baseweb.applications.tms.model.DeliveryTripModel;
import com.hust.baseweb.applications.tms.repo.DeliveryTripDetailRepo;
import com.hust.baseweb.applications.tms.repo.DeliveryTripRepo;
import com.hust.baseweb.applications.tms.repo.ShipmentItemRepo;
import com.hust.baseweb.applications.tms.repo.status.DeliveryTripDetailStatusRepo;
import com.hust.baseweb.applications.tms.repo.status.ShipmentItemStatusRepo;
import com.hust.baseweb.entity.StatusItem;
import com.hust.baseweb.repo.StatusItemRepo;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Log4j2
@Transactional
public class DeliveryTripDetailServiceImpl implements DeliveryTripDetailService {

    private DeliveryTripDetailRepo deliveryTripDetailRepo;
    private ShipmentItemRepo shipmentItemRepo;
    private ProductRepo productRepo;

    private DeliveryTripRepo deliveryTripRepo;
    private DeliveryTripService deliveryTripService;

    private StatusItemRepo statusItemRepo;

    private ShipmentItemStatusRepo shipmentItemStatusRepo;
    private DeliveryTripDetailStatusRepo deliveryTripDetailStatusRepo;


    @Override
    public int save(String deliveryTripId,
                    List<DeliveryTripDetailModel.Create> inputs) {
        Date now = new Date();

        UUID deliveryTripIdUuid = UUID.fromString(deliveryTripId);
        DeliveryTrip deliveryTrip = deliveryTripRepo.findById(deliveryTripIdUuid)
                .orElseThrow(NoSuchElementException::new);

        Map<UUID, ShipmentItem> shipmentItemMap = buildShipmentItemMap(inputs);

        List<DeliveryTripDetail> deliveryTripDetails = new ArrayList<>();

        StatusItem shipmentItemScheduledTripStatus = statusItemRepo.findById("SHIPMENT_ITEM_SCHEDULED_TRIP")
                .orElseThrow(NoSuchElementException::new);
        StatusItem deliveryTripDetailScheduledTripStatus = statusItemRepo.findById("DELIVERY_TRIP_DETAIL_SCHEDULED_TRIP")
                .orElseThrow(NoSuchElementException::new);

        Map<ShipmentItem, List<ShipmentItemStatus>> shipmentItemToStatusMap = shipmentItemStatusRepo.findAllByShipmentItemIn(
                shipmentItemMap.values())
                .stream()
                .collect(Collectors.groupingBy(ShipmentItemStatus::getShipmentItem));

        for (DeliveryTripDetailModel.Create input : inputs) {
            log.info("save, input quantity = " + input.getDeliveryQuantity());
            DeliveryTripDetail deliveryTripDetail = new DeliveryTripDetail();
            deliveryTripDetail.setDeliveryTrip(deliveryTrip);

            ShipmentItem shipmentItem = shipmentItemMap.get(input.getShipmentItemId());

            log.info("save, find ShipmentItem " +
                    shipmentItem.getShipment().getShipmentId() +
                    "," +
                    shipmentItem.getShipmentItemId() +
                    ", product = " +
                    shipmentItem.getOrderItem().getProduct().getProductId());

            deliveryTripDetail.setShipmentItem(shipmentItem);
            deliveryTripDetail.setDeliveryQuantity(input.getDeliveryQuantity());
            deliveryTripDetail.setStatusItem(deliveryTripDetailScheduledTripStatus);

            shipmentItem.setScheduledQuantity(shipmentItem.getScheduledQuantity() + input.getDeliveryQuantity());
            if (shipmentItem.getScheduledQuantity() == shipmentItem.getQuantity()) {
                shipmentItem.setStatusItem(shipmentItemScheduledTripStatus);
                List<ShipmentItemStatus> shipmentItemStatuses = shipmentItemToStatusMap.get(shipmentItem);
                for (ShipmentItemStatus shipmentItemStatus : shipmentItemStatuses) {
                    if (shipmentItemStatus.getThruDate() == null) {
                        shipmentItemStatus.setThruDate(now);
                        break;
                    }
                }
                shipmentItemStatuses.add(new ShipmentItemStatus(null,
                        shipmentItem,
                        shipmentItemScheduledTripStatus,
                        now,
                        null));
            }

            deliveryTripDetails.add(deliveryTripDetail);
        }

        deliveryTripDetailRepo.saveAll(deliveryTripDetails);
        deliveryTripDetailStatusRepo.saveAll(deliveryTripDetails.stream()
                .map(deliveryTripDetail -> new DeliveryTripDetailStatus(null,
                        deliveryTripDetail,
                        deliveryTripDetailScheduledTripStatus,
                        now,
                        null,
                        null)).collect(Collectors.toList()));   // TODO: update user login id

        shipmentItemRepo.saveAll(shipmentItemMap.values()); // update scheduled quantity
        shipmentItemStatusRepo.saveAll(shipmentItemToStatusMap.values().stream().flatMap(Collection::stream).collect(
                Collectors.toList())); // convert List<List<T>> --> List<T>

        DeliveryTripModel.Tour deliveryTripInfo = deliveryTripService.getDeliveryTripInfo(deliveryTripId,
                new ArrayList<>());
        deliveryTrip.setTotalWeight(deliveryTripInfo.getTotalWeight());
        deliveryTrip.setTotalPallet(deliveryTripInfo.getTotalPallet());
        deliveryTrip.setDistance(deliveryTripInfo.getTotalDistance());
        deliveryTrip = deliveryTripRepo.save(deliveryTrip);

        updateDeliveryTripDetailSequence(deliveryTrip, deliveryTripInfo);

        return inputs.size();
    }

    @NotNull
    private Map<UUID, ShipmentItem> buildShipmentItemMap(List<DeliveryTripDetailModel.Create> inputs) {
        return shipmentItemRepo.findAllByShipmentItemIdIn(
                inputs.stream()
                        .map(DeliveryTripDetailModel.Create::getShipmentItemId)
                        .distinct()
                        .collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(ShipmentItem::getShipmentItemId, shipmentItem -> shipmentItem));
    }

    public void updateDeliveryTripDetailSequence(DeliveryTrip deliveryTrip, DeliveryTripModel.Tour deliveryTripInfo) {
        List<GeoPoint> tour = deliveryTripInfo.getTour();
        Map<GeoPoint, Integer> geoPointIndexMap = new HashMap<>();
        for (int i = 0; i < tour.size(); i++) {
            geoPointIndexMap.put(tour.get(i), i);
        }

        List<DeliveryTripDetail> deliveryTripDetails = deliveryTripDetailRepo.findAllByDeliveryTrip(deliveryTrip);
        for (DeliveryTripDetail deliveryTripDetail : deliveryTripDetails) {
            GeoPoint geoPoint = deliveryTripDetail.getShipmentItem().getShipToLocation().getGeoPoint();
            deliveryTripDetail.setSequenceId(geoPointIndexMap.get(geoPoint));
        }
        deliveryTripDetailRepo.saveAll(deliveryTripDetails);
    }

    @Override
    public boolean delete(String deliveryTripDetailId) {
        UUID deliveryTripDetailIdUuid = UUID.fromString(deliveryTripDetailId);
        DeliveryTripDetail deliveryTripDetail = deliveryTripDetailRepo.findById(deliveryTripDetailIdUuid)
                .orElseThrow(NoSuchElementException::new);
        DeliveryTrip deliveryTrip = deliveryTripDetail.getDeliveryTrip();
        deliveryTripDetailRepo.deleteById(deliveryTripDetailIdUuid);
        DeliveryTripModel.Tour deliveryTripInfo = deliveryTripService.getDeliveryTripInfo(deliveryTrip.getDeliveryTripId()
                .toString(), new ArrayList<>());
        deliveryTrip.setTotalWeight(deliveryTripInfo.getTotalWeight());
        deliveryTrip.setTotalPallet(deliveryTripInfo.getTotalPallet());
        deliveryTrip.setDistance(deliveryTripInfo.getTotalDistance());
        deliveryTripRepo.save(deliveryTrip);

        // TODO: delivery trip status??

        updateDeliveryTripDetailSequence(deliveryTrip, deliveryTripInfo);

        return true;
    }

    @Override
    public Page<DeliveryTripDetail> findAll(String deliveryTripId, Pageable pageable) {
        DeliveryTrip deliveryTrip = deliveryTripRepo.findById(UUID.fromString(deliveryTripId))
                .orElseThrow(NoSuchElementException::new);
        return deliveryTripDetailRepo.findAllByDeliveryTrip(deliveryTrip, pageable);
    }

    @Override
    public DeliveryTripDetailModel.OrderItems findAll(String deliveryTripId) {
        DeliveryTrip deliveryTrip = deliveryTripRepo.findById(UUID.fromString(deliveryTripId))
                .orElseThrow(NoSuchElementException::new);
        List<DeliveryTripDetail> deliveryTripDetails = deliveryTripDetailRepo.findAllByDeliveryTrip(deliveryTrip);
        if (deliveryTripDetails == null || deliveryTripDetails.isEmpty()) {
            return new DeliveryTripDetailModel.OrderItems(new ArrayList<>(), null, null);
        }
        GeoPoint facilityGeoPoint = deliveryTripDetails.get(0)
                .getShipmentItem()
                .getFacility()
                .getPostalAddress()
                .getGeoPoint();
        return new DeliveryTripDetailModel.OrderItems(
                deliveryTripDetails.stream()
                        .map(deliveryTripDetail -> deliveryTripDetail.toDeliveryTripDetailModel(
                                deliveryTripDetail.getShipmentItem().getOrderItem().getProduct()))
                        .collect(Collectors.toList()),
                Double.parseDouble(facilityGeoPoint.getLatitude()),
                Double.parseDouble(facilityGeoPoint.getLongitude())
        );

    }

    @Override
    @Transactional
    public DeliveryTripDetail updateStatusDeliveryTripDetail(
            UUID deliveryTripDetailId, String statusId) {
        log.info("updateStatusDeliveryTripDetail, deliveryTripDetailId = " +
                deliveryTripDetailId +
                ", statusId = " +
                statusId);
        StatusItem statusItem = statusItemRepo.findByStatusId(statusId);
        DeliveryTripDetail dtd = deliveryTripDetailRepo.findByDeliveryTripDetailId(deliveryTripDetailId);
        if (dtd == null) {
            return null;
        }
        dtd.setStatusItem(statusItem);
        dtd = deliveryTripDetailRepo.save(dtd);
        log.info("updateStatusDeliveryTripDetail, deliveryTripDetailId = " +
                deliveryTripDetailId +
                ", statusId = " +
                statusId +
                " DONE");

        return dtd;
    }

}
