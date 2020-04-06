package com.hust.baseweb.applications.geo.service;

import com.google.gson.Gson;
import com.hust.baseweb.applications.geo.embeddable.DistanceTravelTimePostalAddressEmbeddableId;
import com.hust.baseweb.applications.geo.entity.DistanceTravelTimePostalAddress;
import com.hust.baseweb.applications.geo.entity.Enumeration;
import com.hust.baseweb.applications.geo.entity.PostalAddress;
import com.hust.baseweb.applications.geo.model.DistanceTravelTimeElement;
import com.hust.baseweb.applications.geo.model.QueryDistanceTravelTimeInputModel;
import com.hust.baseweb.applications.geo.repo.DistanceTravelTimePostalAddressRepo;
import com.hust.baseweb.applications.geo.repo.EnumerationRepo;
import com.hust.baseweb.applications.geo.repo.PostalAddressJpaRepo;
import com.hust.baseweb.utils.LatLngUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//import java.util.Enumeration;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@Log4j2
//@Transactional
@Service
public class DistanceTravelTimePostalAddressServiceImpl implements DistanceTravelTimePostalAddressService {
    private PostalAddressJpaRepo postalAddressRepo;
    private DistanceTravelTimePostalAddressRepo distanceTraveltimePostalAddressRepo;
    private EnumerationRepo enumerationRepo;

    private int computeMissingDistanceHarvsine() {
        List<PostalAddress> points = postalAddressRepo.findAll();
        List<DistanceTravelTimePostalAddress> distances = new ArrayList<DistanceTravelTimePostalAddress>();
        log.info("computeMissingDistance, points.sz = " + points.size());
        int cnt = 0;//points.size() * points.size();
        for (int i = 0; i < points.size(); i++) {
            PostalAddress p = points.get(i);
            for (int j = 0; j < points.size(); j++) {
                PostalAddress q = points.get(j);
                DistanceTravelTimePostalAddress d = distanceTraveltimePostalAddressRepo.findByDistanceTravelTimePostalAddressEmbeddableId(
                        new DistanceTravelTimePostalAddressEmbeddableId(p.getContactMechId(), q.getContactMechId())
                );
                if (d == null) {
                    cnt++;
                    double latFrom = p.getGeoPoint().getLatitude();
                    double lngFrom = p.getGeoPoint().getLongitude();
                    double latTo = q.getGeoPoint().getLatitude();
                    double lngTo = q.getGeoPoint().getLongitude();
                    int distance = LatLngUtils.distance(latFrom, lngFrom, latTo, lngTo) * 1000; // meter
                    int time = distance * 3600 / 30_000; // second (30km/h)
                    d = new DistanceTravelTimePostalAddress();
                    DistanceTravelTimePostalAddressEmbeddableId id = new DistanceTravelTimePostalAddressEmbeddableId(p.getContactMechId(),
                            q.getContactMechId());
                    d.setDistanceTravelTimePostalAddressEmbeddableId(id);
                    d.setDistance(distance);
                    d.setTravelTime(time);
                    d.setTravelTimeTruck(time);
                    d.setTravelTimeMotorbike(time);
                    d.setEnumID("HAVERSINE");
                    distances.add(d);

                    distanceTraveltimePostalAddressRepo.save(d);

                } else {

                }
                log.info("computeMissingDistance, i = " +
                        i +
                        ", j = " +
                        j +
                        ", cnt = " +
                        cnt +
                        ", sz = " +
                        points.size());
            }
            log.info("computeMissingDistance, finished " + i + "/" + points.size());
        }
        log.info("computeMissingDistance, cnt = " + cnt);
        //List<DistanceTraveltimePostalAddress> distances = distanceTraveltimePostalAddressRepo.findAll();
        //distanceTraveltimePostalAddressRepo.saveAll(distances);
        log.info("computeMissingDistance, finished saveAll = " + distances.size());

        return cnt;

    }

    private int computeMissingDistanceOpenStreetMap(int speedTruck, int speedMotorbike, int maxElements) {
        // speedTruck, speedMotobike in km/h
        List<PostalAddress> points = postalAddressRepo.findAll();
        List<DistanceTravelTimePostalAddress> distances = new ArrayList<DistanceTravelTimePostalAddress>();
        List<DistanceTravelTimeElement> listDistances = new ArrayList<>();

        log.info("computeMissingDistanceOpenStreetMap, points.sz = " + points.size());
        int cnt = 0;//points.size() * points.size();
        for (int i = 0; i < points.size(); i++) {
            PostalAddress p = points.get(i);
            for (int j = 0; j < points.size(); j++) {
                PostalAddress q = points.get(j);
                DistanceTravelTimePostalAddress d = distanceTraveltimePostalAddressRepo.findByDistanceTravelTimePostalAddressEmbeddableId(
                        new DistanceTravelTimePostalAddressEmbeddableId(p.getContactMechId(), q.getContactMechId())
                );
                if (d == null) {
                    cnt++;
                    double latFrom = p.getGeoPoint().getLatitude();
                    double lngFrom = p.getGeoPoint().getLongitude();
                    double latTo = q.getGeoPoint().getLatitude();
                    double lngTo = q.getGeoPoint().getLongitude();

                    DistanceTravelTimeElement e = new DistanceTravelTimeElement();
                    e.setFromId(p.getContactMechId().toString());
                    e.setFromLat(latFrom);
                    e.setFromLng(lngFrom);
                    e.setToId(q.getContactMechId().toString());
                    e.setToLat(latTo);
                    e.setToLng(lngTo);
                    e.setDistance(0);
                    e.setTravelTimeMotobike(0);
                    e.setTravelTimeTruck(0);

                    listDistances.add(e);

                    /*
                    DistanceTraveltimePostalAddressEmbeddableId id = new DistanceTraveltimePostalAddressEmbeddableId(p.getContactMechId(),
                            q.getContactMechId());
                    d.setDistanceTraveltimePostalAddressEmbeddableId(id);
                    d.setDistance(distance);
                    d.setTravelTime(time);
                    d.setTravelTimeTruck(time);
                    d.setTravelTimeMotobike(time);
                    d.setEnumID(distanceSource);
                    distances.add(d);

                    distanceTraveltimePostalAddressRepo.save(d);
                    */
                } else {

                }
                log.info("computeMissingDistance, i = " +
                        i +
                        ", j = " +
                        j +
                        ", cnt = " +
                        cnt +
                        ", sz = " +
                        points.size());

                if (cnt > maxElements) {
                    break;
                }
            }
            log.info("computeMissingDistance, finished " + i + "/" + points.size());
            if (cnt > maxElements) {
                break;
            }
        }
        log.info("computeMissingDistance, cnt = " + cnt);
        //List<DistanceTraveltimePostalAddress> distances = distanceTraveltimePostalAddressRepo.findAll();
        //distanceTraveltimePostalAddressRepo.saveAll(distances);

        QueryDistanceTravelTimeInputModel queryDistanceTravelTimeInputModel = new QueryDistanceTravelTimeInputModel();
        queryDistanceTravelTimeInputModel.setParams("OpenStreetMap");
        queryDistanceTravelTimeInputModel.setElements(listDistances);

        Gson gson = new Gson();
        String json = gson.toJson(queryDistanceTravelTimeInputModel);

        try {
            long cur = System.currentTimeMillis();

            String url = "http://118.70.13.71/mo/HoChiMinh/get-distance-elements";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // Setting basic post request
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("apikey", "6OCqwI5lsE9H3RdXaJ5kucvScPQsqfGF");

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(json);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            System.out.println("nSending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String output;
            StringBuffer response = new StringBuffer();

            while ((output = in.readLine()) != null) {
                response.append(output);
            }
            in.close();

            System.out.println("request time = " + (System.currentTimeMillis() - cur));

            QueryDistanceTravelTimeInputModel res = gson.fromJson(response.toString(),
                    QueryDistanceTravelTimeInputModel.class);

            for (DistanceTravelTimeElement d : res.getElements()) {
                DistanceTravelTimePostalAddressEmbeddableId id = new DistanceTravelTimePostalAddressEmbeddableId(
                        UUID.fromString(d.getFromId()),
                        UUID.fromString(d.getToId()));
                DistanceTravelTimePostalAddress dis = new DistanceTravelTimePostalAddress();
                dis.setDistanceTravelTimePostalAddressEmbeddableId(id);
                dis.setDistance((int) d.getDistance());
                Enumeration enumeration = enumerationRepo.findByEnumId("OPEN_STREET_MAP");

                dis.setEnumeration(enumeration);

                //dis.setEnumID("OPEN_STREET_MAP");

                int t_truck = (int) ((int) d.getDistance() / (speedTruck * 1000.0 / 3600));
                int t_motobike = (int) ((int) d.getDistance() / (speedMotorbike * 1000.0 / 3600));

                dis.setTravelTimeMotorbike(t_motobike);
                dis.setTravelTimeTruck(t_truck);
                dis.setTravelTime(0);

                distances.add(dis);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("computeMissingDistanceOpenStreetMap, json = " + json);
        distanceTraveltimePostalAddressRepo.saveAll(distances);
        log.info("computeMissingDistance, finished saveAll = " + distances.size());

        return cnt;

    }


    @Override
    public int computeMissingDistance(String distanceSource, int speedTruck, int speedMotorbike, int maxElements) {

        if (distanceSource.equals("HAVERSINE")) {
            return computeMissingDistanceHarvsine();
        } else {// use open street map
            return computeMissingDistanceOpenStreetMap(speedTruck, speedMotorbike, maxElements);
        }
    }
}
