package kz.dasm.telegramalertingsystem.service;

import kz.dasm.telegramalertingsystem.ObjectFactory;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import kz.dasm.telegramalertingsystem.SendMessageRequest;
import kz.dasm.telegramalertingsystem.SendMessageResponse;

import javax.xml.bind.JAXBElement;

@Endpoint
public class TelegramSoapService {
    private static final String NAMESPACE_URI = "http://telegramalertingsystem.dasm.kz/";

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "SendMessageRequest")
    public @ResponsePayload JAXBElement<SendMessageResponse> sendMessage(@RequestPayload SendMessageRequest sendMessageRequest) {
        System.out.println("Here! ");
        SendMessageResponse response = new SendMessageResponse();
        response.setReturn(sendMessageRequest.getMessage());
        ObjectFactory factory = new ObjectFactory();
        return factory.createSendMessageResponse(response);
    }
}
