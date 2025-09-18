package com.certicom.certifact_boletas_service_ng.jms;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Slf4j
@RequiredArgsConstructor
public class SqsProducer {

    private final AmazonSQS amazonSQS;
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
            // Serializamos el payload a JSON
            String jsonPayload = objectMapper.writeValueAsString(payload);

            // Construimos la request
            SendMessageRequest request = new SendMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMessageBody(jsonPayload);

            // Si la cola es FIFO, agregamos groupId y deduplicationId
            if (queueUrl.endsWith(".fifo")) {
                request.withMessageGroupId("messageGroup1")
                        .withMessageDeduplicationId("1" + System.currentTimeMillis());
            }

            // Atributo adicional (equivalente a "documentType" en JMS)
            request.addMessageAttributesEntry(
                    "documentType",
                    new com.amazonaws.services.sqs.model.MessageAttributeValue()
                            .withDataType("String")
                            .withStringValue(payload.getClass().getName())
            );

            amazonSQS.sendMessage(request);
            System.out.println("✅ SE ENVIO MENSAJE A LA COLA SQS: " + queueUrl);

        } catch (Exception e) {
            watchLogs(e);
            throw new RuntimeException("❌ Error enviando mensaje a SQS", e);
        }
    }

    void watchLogs(Exception e) {
        log.error("ERROR: {}", e.getMessage());
    }

}
