package com.gaminglounge.bll;

import com.gaminglounge.dal.ServiceRequestDAL;
import com.gaminglounge.model.ServiceRequest;
import java.util.List;

public class ServiceRequestService {
    private ServiceRequestDAL dal;

    public ServiceRequestService() {
        dal = new ServiceRequestDAL();
    }

    public boolean sendRequest(int customerId, String type, String content) {
        return dal.createRequest(customerId, type, content, "Client");
    }

    public boolean sendReply(int customerId, String content) {
        // Admin reply
        return dal.createRequest(customerId, "Chat", content, "Admin");
    }

    public List<ServiceRequest> getPendingRequests() {
        return dal.getPendingRequests();
    }

    public List<ServiceRequest> getPendingChats() {
        return dal.getPendingChats();
    }

    public List<ServiceRequest> getCustomerHistory(int customerId) {
        return dal.getRequestsByCustomer(customerId);
    }

    public boolean completeRequest(int requestId) {
        return dal.completeRequest(requestId);
    }
}
