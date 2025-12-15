package com.certicom.certifact_boletas_service_ng.jms;

import com.certicom.certifact_boletas_service_ng.exception.ServiceException;
import com.certicom.certifact_boletas_service_ng.util.LogHelper;
import com.certicom.certifact_boletas_service_ng.util.LogMessages;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.io.Serializable;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class SqsProducer {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${apifact.aws.sqs.processSummary}")
    private String processSummary;

    public void produceProcessSummary(String ticket, String rucEmisor) {
        try {
            send(processSummary, ticket);
        }catch (Exception e){
            watchLogs(e);
        }
    }

    public <MESSAGE extends Serializable> void send(String queueUrl, MESSAGE payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);

            SendMessageRequest.Builder requestBuilder = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(jsonPayload)
                    .messageAttributes(Map.of(
                            "documentType",
                            MessageAttributeValue.builder()
                                    .dataType("String")
                                    .stringValue(payload.getClass().getName())
                                    .build()
                    ));

            if (queueUrl.endsWith(".fifo")) {
                requestBuilder
                        .messageGroupId("messageGroup1")
                        .messageDeduplicationId("1" + System.currentTimeMillis());
            }

            sqsClient.sendMessage(requestBuilder.build());
            LogHelper.infoLog(LogMessages.currentMethod(), "Se envio mensaje a la cola sqs: "+queueUrl);

        } catch (Exception e) {
            LogHelper.errorLog(LogMessages.currentMethod(), "Ocurrio un error al momento de enviar mensaje a sqs", e);
            throw new ServiceException("Ocurrio un error al momento de enviar mensaje a sqs", e);
        }
    }

    void watchLogs(Exception e) {
        log.error("ERROR: {}", e.getMessage());
    }

}
