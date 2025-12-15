package com.certicom.certifact_boletas_service_ng.jms;

import com.certicom.certifact_boletas_service_ng.dto.others.ResponsePSE;
import com.certicom.certifact_boletas_service_ng.service.DocumentsSummaryService;
import com.certicom.certifact_boletas_service_ng.util.ConstantesParameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqsConsumer {

    private final SqsClient amazonSQSClient;
    private final DocumentsSummaryService documentsSummaryService;
    private final ObjectMapper objectMapper;

    @Value("${apifact.aws.sqs.processSummary}")
    private String processSummary;

    // 👇 Ejecuta cada 5 segundos
    @Scheduled(fixedDelay = 5000)  // Se ejecuta cada 5 segundos.
    public void receiveMessageQueueProcessSummary () {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(processSummary)
                .maxNumberOfMessages(5)
                .waitTimeSeconds(10)
                .messageAttributeNames("All")
                .build();

        List<Message> messages = amazonSQSClient.receiveMessage(request).messages();
        ResponsePSE resp = new ResponsePSE();
        for (Message msg : messages) {
            try {
                log.info("Se recibio mensaje Para Procesar resumen diario >>>>>>>>>>>>>>>>>> {}", msg.body());
                String userName = ConstantesParameter.USER_API_QUEUE;
                String rucEmisor = null;
                resp = documentsSummaryService.processSummaryTicket(objectMapper.readValue(msg.body(), String.class), userName, rucEmisor);

                amazonSQSClient.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(processSummary)
                        .receiptHandle(msg.receiptHandle())
                        .build());
                System.out.println("✅ Mensaje eliminado de la cola");

            } catch (Exception e) {
                log.error("❌ ERROR ❌: {}", e.getMessage());
                // No llamamos deleteMessage → el mensaje reaparecerá después del visibility timeout
            }
        }
    }

}
