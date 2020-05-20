package com.hust.baseweb.test.simulator;

import com.google.gson.Gson;
import com.hust.baseweb.applications.customer.entity.PartyDistributor;
import com.hust.baseweb.applications.customer.entity.PartyRetailOutlet;
import com.hust.baseweb.applications.customer.model.PartyCustomerModel;
import com.hust.baseweb.applications.logistics.entity.Facility;
import com.hust.baseweb.applications.logistics.entity.Product;
import com.hust.baseweb.applications.logistics.model.GetListFacilityOutputModel;
import com.hust.baseweb.applications.logistics.model.GetListProductOutputModel;
import com.hust.baseweb.applications.order.model.ModelCreateOrderInput;
import com.hust.baseweb.applications.order.model.ModelCreateOrderInputOrderItem;
import com.hust.baseweb.utils.Constant;
import com.hust.baseweb.utils.DateTimeUtils;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.*;

@Getter
@Setter
public class CreateOrderAgent extends Thread {
    public static final String module = CreateOrderAgent.class.getName();
    //OkHttpClient client = new OkHttpClient();

    private Random rand = new Random();
    private Thread thread = null;
    private String token;
    private String username;
    private String password;
    //    private int idleTime = 120000;// 2 minute of ilde
    private int idleTime = 10;

    public CreateOrderAgent(String username, String password) {
        this.username = username;
        this.password = password;
    }

    //public static final MediaType JSON = MediaType
    //		.get("application/json; charset=utf-8");
    private HttpPostExecutor executor = new HttpPostExecutor();

    private int nbIters = 1000;
    private double maxTime = 0;
    private int agentId;
    private String fromDate;
    private String toDate;
    
    /*
    String execPostUseToken(String url, String json, String token)
            throws IOException {
        System.out.println(module + "::execPostUseToken, url = " + url + ", json = " + json + ", token = " + token);
        RequestBody body = RequestBody.create(Constants.JSON, json);
        Request request = new Request.Builder().url(url)
                .header("X-Auth-Token", token).post(body).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
    */

    public CreateOrderAgent(int id) {
        this.agentId = id;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public static void main(String[] args) {

        CreateOrderAgent a = new CreateOrderAgent(0);
        a.start();
    }

    public void setNbIters(int nbIters) {
        this.nbIters = nbIters;
    }

    public void start() {
//        System.out.println(module + ":: start running...");
        if (thread == null) {
            thread = new Thread(this, module);
            thread.start();
        }
    }

    public double getMaxTime() {
        return this.maxTime;
    }

    public void createOrders(int nbIters, String fromDateStr, String toDateStr) {
        maxTime = 0;
        List<String> dates = new ArrayList<String>();
        String curDate = fromDateStr;
        int cnt = 0;
        while (!curDate.equals(toDateStr)) {
            dates.add(curDate);
            String s = curDate + " 00:00:00";
            s = DateTimeUtils.next(s, 1);
            String[] tmp = s.split(" ");
            curDate = tmp[0].trim();

            if (cnt > 1000) {
                System.out.println("EXCEPTION too many dates!!!!");
                assert (false);
            }
            cnt++;
        }
        Random R = new Random();

        for (int i = 1; i <= nbIters; i++) {
            Date timePoint = new Date();
            Random random = new Random();
            double t0 = System.currentTimeMillis();

            curDate = dates.get(R.nextInt(dates.size()));// pickup a random date
            // genrate random hh:mm:ss
            String hh = DateTimeUtils.std2Digit(R.nextInt(24));
            String mm = DateTimeUtils.std2Digit(R.nextInt(60));
            String ss = DateTimeUtils.std2Digit(R.nextInt(60));

            curDate = curDate + " " + hh + ":" + mm + ":" + ss;

            //Date orderDate = new Date();// take current date-time
            Date orderDate = null;
            try {
//            	System.out.println("Consider curDate = " + curDate);
                orderDate = Constant.DATE_FORMAT.parse(curDate);
//            	System.out.println("GEN orderDate " + orderDate);


                double time = createOrder(orderDate);

                double t = System.currentTimeMillis() - t0;
                if (maxTime < time) {
                    maxTime = time;
                }

                Thread.sleep(idleTime);

                System.out.println("finished " + i + "/" + nbIters + ", time = " + time + ", maxTime = " + maxTime);

            } catch (Exception e) {
//                System.out.println("NOT CORRECT date-time " + curDate);
//            	e.printStackTrace();break;
                //return;
            }
        }

        System.out.println(module + "[" + agentId + "] finished, maxTime = " + maxTime);

    }

    public void run() {
        Simulator.threadRunningCounter.incrementAndGet();
//        System.out.println(module + "::run....");

        token = Login.login(username, password);

        createOrders(nbIters, fromDate, toDate);

        Simulator.threadRunningCounter.decrementAndGet();
    }

    public String name() {
        return module + "[" + agentId + "]";
    }

    public List<Product> getProducts() {
//        System.out.println("createorderagent getProducts");
        try {
            String json = "{\"statusId\":null}";
            String rs = executor.execPostUseToken(Constants.URL_ROOT + "/api/get-list-product", json, token);
            //System.out.println(module + "::getProductIds, rs = " + rs);
            Gson gson = new Gson();
            GetListProductOutputModel products = gson.fromJson(rs, GetListProductOutputModel.class);
            return products.getProducts();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Facility> getFacilities() {
        try {
            String json = "{\"statusId\":null}";
            String rs = executor.execPostUseToken(Constants.URL_ROOT + "/api/get-list-facility", json, token);
            //System.out.println(module + "::getFacilities, rs = " + rs);
            Gson gson = new Gson();
            GetListFacilityOutputModel facilities = gson.fromJson(rs, GetListFacilityOutputModel.class);
            return facilities.getFacilities();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<PartyCustomerModel> getCustomers() {
        try {
            String json = "{\"statusId\":null}";

            //String rs = executor.execPostUseToken(Constants.URL_ROOT + "/api/get-list-party-customers", json, token);
            String rs = executor.execGetUseToken(Constants.URL_ROOT + "/api/get-list-party-customers", null, token);
//            System.out.println(name() + "::getCustomers, rs = " + rs);
            Gson gson = new Gson();
            //GetListPartyCustomerOutputModel customers = gson.fromJson(rs, GetListPartyCustomerOutputModel.class);
            //return customers.getCustomers();
            PartyCustomerModel[] arr = gson.fromJson(rs, PartyCustomerModel[].class);
            List<PartyCustomerModel> lst = new ArrayList<PartyCustomerModel>();
            if (arr != null) {
                lst.addAll(Arrays.asList(arr));
            }

            return lst;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public double createOrder(Date orderDate) {
        try {
            ProductManager productManager = new ProductManager(token);
            CustomerManager customerManager = new CustomerManager(token);
            FacilityManager facilityManager = new FacilityManager(token);

            Gson gson = new Gson();
            String[] salesmanIds = {"dungpq", "datnt", "admin", "nguyenvanseu"};
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            List<Product> products = productManager.getProducts();//getProducts();
            List<Facility> facilities = facilityManager.getListFacility();//getFacilities();
            List<PartyCustomerModel> customers = getCustomers();
            List<PartyDistributor> distributors = customerManager.getDistributors();
            List<PartyRetailOutlet> retailOutlets = customerManager.getRetailOutlets();

            if (facilities.isEmpty() || customers.isEmpty() || distributors.isEmpty() || retailOutlets.isEmpty()) {
                return 0;
            }

//            System.out.println(name() + "::createOrder, products.sz = " + products.size() + ", facilities.sz = "
//            + facilities.size() + ", customers.sz = " + customers.size());

            //Product sel_p = products.get(rand.nextInt(products.size()));
            Facility selectedFacility = facilities.get(rand.nextInt(facilities.size()));
            //PartyCustomerModel selectedCustomer = customers.get(rand.nextInt(customers.size()));
            PartyDistributor selectedDistributor = distributors.get(rand.nextInt(distributors.size()));
            PartyRetailOutlet selectedRetailOutlet = retailOutlets.get(rand.nextInt(retailOutlets.size()));

            ModelCreateOrderInput input = new ModelCreateOrderInput();
            input.setFacilityId(selectedFacility.getFacilityId());
            input.setSalesChannelId(null);
            input.setSalesmanId(salesmanIds[rand.nextInt(salesmanIds.length)]);

            //input.setOrderDate(formatter.format(new Date()));
            input.setOrderDate(formatter.format(orderDate));

            //input.setPartyCustomerId(selectedCustomer.getPartyId());
            //input.setToCustomerId(UUID.fromString(selectedCustomer.getPartyCustomerId()));
            input.setToCustomerId(selectedRetailOutlet.getPartyId());
            input.setFromVendorId(selectedDistributor.getPartyId());

            ModelCreateOrderInputOrderItem[] orderItems = new ModelCreateOrderInputOrderItem[products.size()];
            for (int i = 0; i < orderItems.length; i++) {
                orderItems[i] = new ModelCreateOrderInputOrderItem();
                orderItems[i].setProductId(products.get(i).getProductId());
                orderItems[i].setQuantity(rand.nextInt(100) + 1);
                /// orderItems[i].setUnitPrice(new BigDecimal((rand.nextInt(100) + 1) * 10000000));
                //BigDecimal total = orderItems[i].getUnitPrice().multiply(new BigDecimal(orderItems[i].getQuantity()));
                //orderItems[i].setTotalItemPrice(total);
            }

            input.setOrderItems(orderItems);
            String json = gson.toJson(input);

            double t0 = System.currentTimeMillis();
            String rs = executor.execPostUseToken(Constants.URL_ROOT + "/api/create-order-distributor-to-retail-outlet",
                json,
                token);
            //System.out.println(module + "::createOrder, rs = " + rs);
            return System.currentTimeMillis() - t0;


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

}
