package com.hust.baseweb.applications.tms.repo;

import com.hust.baseweb.applications.tms.entity.DeliveryTrip;
import com.hust.baseweb.applications.tms.entity.DeliveryTripDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.UUID;

public interface DeliveryTripDetailRepo extends
        PagingAndSortingRepository<DeliveryTripDetail, UUID> {
    Page<DeliveryTripDetail> findAllByDeliveryTrip(DeliveryTrip deliveryTrip, Pageable pageable);

    List<DeliveryTripDetail> findAllByDeliveryTrip(DeliveryTrip deliveryTrip);

    List<DeliveryTripDetail> findAllByDeliveryTripIn(List<DeliveryTrip> deliveryTrips);

    DeliveryTripDetail findByDeliveryTripDetailId(UUID deliveryTripDetailId);
}
