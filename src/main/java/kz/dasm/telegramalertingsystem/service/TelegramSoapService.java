package kz.dasm.telegramalertingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import kz.dasm.telegramalertingsystem.SendMessageRequest;
import kz.dasm.telegramalertingsystem.SendMessageResponse;

@Endpoint
public class TelegramSoapService {
    private static final String NAMESPACE_URI = "http://telegramalertingsystem.dasm.kz/";

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "sendNessageRequest")
    @ResponsePayload
    public SendMessageResponse sendNessage(@RequestPayload SendMessageRequest sendMessageRequest) {
        SendMessageResponse response = new SendMessageResponse();
        response.setReturn(sendMessageRequest.getMessage());
        return response;
    }
}
