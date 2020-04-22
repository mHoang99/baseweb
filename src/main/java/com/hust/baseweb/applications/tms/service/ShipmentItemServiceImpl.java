package com.hust.baseweb.applications.tms.service;

import com.hust.baseweb.applications.geo.embeddable.DistanceTravelTimePostalAddressEmbeddableId;
import com.hust.baseweb.applications.geo.entity.DistanceTravelTimePostalAddress;
import com.hust.baseweb.applications.geo.entity.PostalAddress;
import com.hust.baseweb.applications.geo.repo.DistanceTravelTimePostalAddressRepo;
import com.hust.baseweb.applications.logistics.entity.ShipmentItemRole;
import com.hust.baseweb.applications.logistics.repo.ProductRepo;
import com.hust.baseweb.applications.logistics.repo.ShipmentItemRoleRepo;
import com.hust.baseweb.applications.tms.entity.*;
import com.hust.baseweb.applications.tms.model.ShipmentItemModel;
import com.hust.baseweb.applications.tms.repo.*;
import com.hust.baseweb.entity.UserLogin;
import com.hust.baseweb.utils.Constant;
import com.hust.baseweb.utils.PageUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ShipmentItemServiceImpl implements ShipmentItemService {

    private ShipmentItemDeliveryPlanRepo shipmentItemDeliveryPlanRepo;

    private ShipmentItemRepo shipmentItemRepo;

    private ProductRepo productRepo;

    private DeliveryTripRepo deliveryTripRepo;
    private DeliveryTripDetailRepo deliveryTripDetailRepo;

    private DistanceTravelTimePostalAddressRepo distanceTraveltimePostalAddressRepo;

    private DeliveryPlanRepo deliveryPlanRepo;

    private ShipmentItemRoleRepo shipmentItemRoleRepo;

    @Override
    public Page<ShipmentItemModel> findAllInDeliveryPlan(String deliveryPlanId, Pageable pageable) {
        Page<ShipmentItemDeliveryPlan> shipmentItemDeliveryPlanPage
                = shipmentItemDeliveryPlanRepo.findAllByDeliveryPlanId(UUID.fromString(deliveryPlanId), pageable);

        return new PageImpl<>(shipmentItemRepo.findAllByShipmentItemIdIn(
                shipmentItemDeliveryPlanPage.map(ShipmentItemDeliveryPlan::getShipmentItemId).getContent())
                .stream().map(ShipmentItem::toShipmentItemModel)
                .collect(Collectors.toList()),
                pageable,
                shipmentItemDeliveryPlanPage.getTotalElements()
        );
    }

    @Override
    public List<ShipmentItemModel> findAllInDeliveryPlan(String deliveryPlanId) {
        List<ShipmentItemDeliveryPlan> shipmentItemDeliveryPlans = shipmentItemDeliveryPlanRepo.findAllByDeliveryPlanId(
                UUID.fromString(deliveryPlanId));
        List<UUID> shipmentItemIds = shipmentItemDeliveryPlans.stream()
                .map(ShipmentItemDeliveryPlan::getShipmentItemId)
                .distinct()
                .collect(Collectors.toList());
        List<ShipmentItem> shipmentItems = shipmentItemRepo.findAllByShipmentItemIdIn(shipmentItemIds);
        return shipmentItems.stream().map(ShipmentItem::toShipmentItemModel).collect(Collectors.toList());
    }

    @Override
    public List<ShipmentItemModel.DeliveryPlan> findAllInDeliveryPlanNearestDeliveryTrip(String deliveryTripId) {
        DeliveryTrip deliveryTrip = deliveryTripRepo.findById(UUID.fromString(deliveryTripId))
                .orElseThrow(NoSuchElementException::new);
        DeliveryPlan deliveryPlan = deliveryTrip.getDeliveryPlan();
        String deliveryPlanId = deliveryPlan.getDeliveryPlanId().toString();
        List<DeliveryTrip> deliveryTripsInDeliveryPlan = deliveryTripRepo.findAllByDeliveryPlan(deliveryPlan);

        List<ShipmentItem> shipmentItemsInDeliveryPlan = shipmentItemRepo.findAllByShipmentItemIdIn(
                shipmentItemDeliveryPlanRepo.findAllByDeliveryPlanId(UUID.fromString(deliveryPlanId)).stream()
                        .map(ShipmentItemDeliveryPlan::getShipmentItemId).collect(Collectors.toList()));

        List<ShipmentItem> shipmentItemsInDeliveryTrip
                = deliveryTripDetailRepo.findAllByDeliveryTrip(deliveryTrip)
                .stream().map(DeliveryTripDetail::getShipmentItem).collect(Collectors.toList());

        Map<ShipmentItem, Integer> shipmentItemAssignedQuantityMap = new HashMap<>();
        deliveryTripDetailRepo.findAllByDeliveryTripIn(deliveryTripsInDeliveryPlan)
                .forEach(deliveryTripDetail ->
                        shipmentItemAssignedQuantityMap.merge(deliveryTripDetail.getShipmentItem(),
                                deliveryTripDetail.getDeliveryQuantity(), Integer::sum));

        List<ShipmentItem> shipmentItemsNotInDeliveryTrip = shipmentItemsInDeliveryPlan.stream()
                .filter(shipmentItem ->
                        shipmentItemAssignedQuantityMap.getOrDefault(shipmentItem, 0) < shipmentItem.getQuantity())
                .collect(Collectors.toList());

        List<PostalAddress> postalAddressInDeliveryTrip = shipmentItemsInDeliveryTrip.stream()
                .map(ShipmentItem::getShipToLocation)
                .distinct()
                .collect(Collectors.toList());
        List<PostalAddress> postalAddressNotInDeliveryTrip = shipmentItemsNotInDeliveryTrip.stream()
                .map(ShipmentItem::getShipToLocation)
                .distinct()
                .collect(Collectors.toList());

        // TODO
        Map<PostalAddress, Integer> postalAddressToMinDistanceMap = new HashMap<>();
        for (PostalAddress postalAddressNotIn : postalAddressNotInDeliveryTrip) {
            int minDistance = Integer.MAX_VALUE;
            for (PostalAddress postalAddressIn : postalAddressInDeliveryTrip) {
                DistanceTravelTimePostalAddress distanceTravelTimePostalAddress = distanceTraveltimePostalAddressRepo
                        .findByDistanceTravelTimePostalAddressEmbeddableId(
                                new DistanceTravelTimePostalAddressEmbeddableId(postalAddressIn.getContactMechId(),
                                        postalAddressNotIn.getContactMechId()));
                int distance;
                if (distanceTravelTimePostalAddress == null) {   // Haversine formula
                    distance = Integer.MAX_VALUE;
                } else {
                    distance = distanceTravelTimePostalAddress.getDistance();
                }
                if (distance < minDistance) {
                    minDistance = distance;
                    postalAddressToMinDistanceMap.put(postalAddressNotIn, minDistance);
                }
            }
        }

        return shipmentItemsNotInDeliveryTrip.stream()
                .sorted(Comparator.comparingDouble(o -> postalAddressToMinDistanceMap.getOrDefault(
                        o.getShipToLocation(), 0)))
                .map(shipmentItem -> shipmentItem.toShipmentItemDeliveryPlanModel(
                        shipmentItemAssignedQuantityMap.getOrDefault(shipmentItem, 0)))
                .collect(Collectors.toList());
    }

    @Override
    public Page<ShipmentItemModel> findAllNotInDeliveryPlan(String deliveryPlanId, Pageable pageable) {
        Set<String> shipmentItemInDeliveryPlans
                = shipmentItemDeliveryPlanRepo.findAllByDeliveryPlanId(UUID.fromString(deliveryPlanId))
                .stream().map(shipmentItemDeliveryPlan -> shipmentItemDeliveryPlan.getShipmentItemId().toString())
                .collect(Collectors.toSet());
        List<ShipmentItem> allShipmentItems = shipmentItemRepo.findAll();
        List<ShipmentItemModel> shipmentItemModels = allShipmentItems.stream()
                .filter(shipmentItem -> !shipmentItemInDeliveryPlans.contains(shipmentItem.getShipmentItemId()
                        .toString()))
                .map(ShipmentItem::toShipmentItemModel)
                .collect(Collectors.toList());
        return PageUtils.getPage(shipmentItemModels, pageable);
    }

    @Override
    public List<ShipmentItemModel> findAllNotInDeliveryPlan(String deliveryPlanId) {
        // TODO: to be improved by adding indicator (status) fields to shipment_items
        Set<String> shipmentItemInDeliveryPlans
                = shipmentItemDeliveryPlanRepo.findAllByDeliveryPlanId(UUID.fromString(deliveryPlanId))
                .stream().map(shipmentItemDeliveryPlan -> shipmentItemDeliveryPlan.getShipmentItemId().toString())
                .collect(Collectors.toSet());
        List<ShipmentItem> allShipmentItems = shipmentItemRepo.findAll();
        return allShipmentItems.stream()
                .filter(shipmentItem -> shipmentItem.getStatusItem().getStatusId().equals("SHIPMENT_ITEM_CREATED")
                        && !shipmentItemInDeliveryPlans.contains(shipmentItem.getShipmentItemId().toString()))
                .map(ShipmentItem::toShipmentItemModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShipmentItemModel> findAllByUserLoginNotInDeliveryPlan(UserLogin userLogin, String deliveryPlanId) {
        // TODO: to be improved by adding indicator (status) fields to shipment_items
        List<ShipmentItemRole> shipmentItemRoles = shipmentItemRoleRepo.findByPartyAndThruDateNull(userLogin.getParty());
        List<UUID> shipmentItemIds = shipmentItemRoles.stream()
                .map(shipmentItemRole -> shipmentItemRole.getShipmentItem().getShipmentItemId())
                .collect(Collectors.toList());
        List<ShipmentItem> list = shipmentItemRepo.findAllByShipmentItemIdIn(shipmentItemIds);
        Set<String> shipmentItemInDeliveryPlans
                = shipmentItemDeliveryPlanRepo.findAllByDeliveryPlanId(UUID.fromString(deliveryPlanId))
                .stream().map(shipmentItemDeliveryPlan -> shipmentItemDeliveryPlan.getShipmentItemId().toString())
                .collect(Collectors.toSet());

        return list.stream()
                .filter(shipmentItem -> shipmentItem.getStatusItem().getStatusId().equals("SHIPMENT_ITEM_CREATED")
                        && !shipmentItemInDeliveryPlans.contains(shipmentItem.getShipmentItemId().toString()))
                .map(ShipmentItem::toShipmentItemModel)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ShipmentItem> findAll(Pageable pageable) {
        return shipmentItemRepo.findAll(pageable);
    }

    @Override
    public Iterable<ShipmentItem> findAll() {
        return shipmentItemRepo.findAll();
    }

    @Override
    public Page<ShipmentItemModel> findAllByUserLogin(UserLogin userLogin, Pageable pageable) {
        List<ShipmentItemRole> shipmentItemRoles = shipmentItemRoleRepo.findByPartyAndThruDateNull(userLogin.getParty());
        List<UUID> shipmentItemIds = shipmentItemRoles.stream()
                .map(shipmentItemRole -> shipmentItemRole.getShipmentItem().getShipmentItemId())
                .collect(Collectors.toList());
        List<ShipmentItemModel> shipmentItemModels = shipmentItemRepo.findAllByShipmentItemIdIn(shipmentItemIds)
                .stream()
                .map(ShipmentItem::toShipmentItemModel)
                .collect(Collectors.toList());
        return PageUtils.getPage(shipmentItemModels, pageable);
    }

    @Override
    public String saveShipmentItemDeliveryPlan(com.hust.baseweb.applications.tms.model.ShipmentItemModel.CreateDeliveryPlan createDeliveryPlan) {
        DeliveryPlan deliveryPlan = deliveryPlanRepo.findById(UUID.fromString(createDeliveryPlan.getDeliveryPlanId()))
                .orElseThrow(NoSuchElementException::new);
        Map<UUID, ShipmentItem> shipmentItemMap = shipmentItemRepo.findAllByShipmentItemIdIn(createDeliveryPlan.getShipmentItemIds()
                .stream()
                .map(UUID::fromString)
                .collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(ShipmentItem::getShipmentItemId, shipmentItem -> shipmentItem));

        List<ShipmentItemDeliveryPlan> shipmentItemDeliveryPlans = new ArrayList<>();
        double totalWeight = 0.0;
        for (String shipmentItemId : createDeliveryPlan.getShipmentItemIds()) {
            shipmentItemDeliveryPlans.add(new ShipmentItemDeliveryPlan(
                    UUID.fromString(shipmentItemId),
                    UUID.fromString(createDeliveryPlan.getDeliveryPlanId())
            ));
            ShipmentItem shipmentItem = shipmentItemMap.get(UUID.fromString(shipmentItemId));
            totalWeight += shipmentItem.getOrderItem().getProduct().getWeight() * shipmentItem.getQuantity();
        }
        deliveryPlan.setTotalWeightShipmentItems(deliveryPlan.getTotalWeightShipmentItems() + totalWeight);
        deliveryPlanRepo.save(deliveryPlan);

        shipmentItemDeliveryPlanRepo.saveAll(shipmentItemDeliveryPlans);
        return createDeliveryPlan.getDeliveryPlanId();
    }

    @Override
    public boolean deleteShipmentItemDeliveryPlan(ShipmentItemModel.DeleteDeliveryPlan deleteDeliveryPlan) {
        ShipmentItemDeliveryPlan shipmentItemDeliveryPlan = shipmentItemDeliveryPlanRepo.findAllByDeliveryPlanIdAndShipmentItemId(
                UUID.fromString(deleteDeliveryPlan.getDeliveryPlanId()),
                UUID.fromString(deleteDeliveryPlan.getShipmentItemId())
        );
        if (shipmentItemDeliveryPlan != null) {
            DeliveryPlan deliveryPlan = deliveryPlanRepo.findById(UUID.fromString(deleteDeliveryPlan.getDeliveryPlanId()))
                    .orElseThrow(NoSuchElementException::new);
            ShipmentItem shipmentItem = shipmentItemRepo.findById(UUID.fromString(deleteDeliveryPlan.getShipmentItemId()))
                    .orElseThrow(NoSuchElementException::new);

            deliveryPlan.setTotalWeightShipmentItems(deliveryPlan.getTotalWeightShipmentItems() -
                    (shipmentItem.getOrderItem().getProduct().getWeight() * shipmentItem.getQuantity()));
            deliveryPlanRepo.save(deliveryPlan);

            shipmentItemDeliveryPlanRepo.delete(shipmentItemDeliveryPlan);
            return true;
        }
        return false;
    }

    @Override
    public List<ShipmentItemModel> findAllNotScheduled(String deliveryPlanId) {
        List<ShipmentItemDeliveryPlan> shipmentItemDeliveryPlans = shipmentItemDeliveryPlanRepo.findAllByDeliveryPlanId(
                UUID.fromString(deliveryPlanId));
        List<ShipmentItem> shipmentItems = shipmentItemRepo.findAllByShipmentItemIdIn(shipmentItemDeliveryPlans.stream()
                .map(ShipmentItemDeliveryPlan::getShipmentItemId)
                .collect(Collectors.toList()));
        return shipmentItems.stream()
                .filter(shipmentItem -> shipmentItem.getScheduledQuantity() < shipmentItem.getQuantity())
                .map(ShipmentItem::toShipmentItemModel)
                .collect(Collectors.toList());
    }

    @Override
    public ShipmentItemModel.Info getShipmentItemInfo(String shipmentItemId) {
        ShipmentItem shipmentItem = shipmentItemRepo.findById(UUID.fromString(shipmentItemId))
                .orElseThrow(NoSuchElementException::new);
        List<DeliveryTripDetail> deliveryTripDetails = deliveryTripDetailRepo.findAllByShipmentItem(shipmentItem);

        return new ShipmentItemModel.Info(
                shipmentItem.getShipmentItemId().toString(),
                shipmentItem.getOrderItem().getOrderId(),
                shipmentItem.getFacility().getFacilityId(),
                shipmentItem.getOrderItem().getProduct().getProductId(),
                shipmentItem.getOrderItem().getProduct().getProductName(),
                shipmentItem.getQuantity(),
                shipmentItem.getStatusItem().getStatusId(),
                deliveryTripDetails.stream()
                        .map(deliveryTripDetail -> new ShipmentItemModel.Info.DeliveryTripDetail(
                                deliveryTripDetail.getDeliveryTrip().getDeliveryPlan().getDeliveryPlanId().toString(),
                                deliveryTripDetail.getDeliveryTrip().getDeliveryTripId().toString(),
                                Constant.DATE_FORMAT.format(deliveryTripDetail.getDeliveryTrip().getExecuteDate()),
                                deliveryTripDetail.getDeliveryQuantity(),
                                deliveryTripDetail.getDeliveryTrip().getVehicle().getVehicleId(),
                                deliveryTripDetail.getDeliveryTrip().getVehicle().getCapacity(),
                                Optional.ofNullable(deliveryTripDetail.getDeliveryTrip().getPartyDriver())
                                        .map(PartyDriver::getPartyId)
                                        .map(UUID::toString)
                                        .orElse(null)))
                        .collect(Collectors.toList()));
    }


}
