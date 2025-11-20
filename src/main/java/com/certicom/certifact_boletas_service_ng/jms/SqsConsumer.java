package com.certicom.certifact_boletas_service_ng.jms;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.certicom.certifact_boletas_service_ng.dto.others.ResponsePSE;
import com.certicom.certifact_boletas_service_ng.service.DocumentsSummaryService;
import com.certicom.certifact_boletas_service_ng.util.ConstantesParameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqsConsumer {

    private final AmazonSQS amazonSQSClient;
    private final DocumentsSummaryService documentsSummaryService;
    private final ObjectMapper objectMapper;

    @Value("${apifact.aws.sqs.processSummary}")
    private String processSummary;

    // 👇 Ejecuta cada 5 segundos
    @Scheduled(fixedDelay = 5000)  // Se ejecuta cada 5 segundos.
    public void receiveMessageQueueProcessSummary () {
        ReceiveMessageRequest request = new ReceiveMessageRequest(processSummary)
                .withMaxNumberOfMessages(10)  // hasta 10 mensajes
                .withWaitTimeSeconds(20);     // long polling (recomendado)

        List<Message> messages = amazonSQSClient.receiveMessage(request).getMessages();
        ResponsePSE resp = new ResponsePSE();
        for (Message msg : messages) {
            try {
                log.info("Se recibio mensaje Para Procesar resumen diario >>>>>>>>>>>>>>>>>> {}", msg.getBody());
                String userName = ConstantesParameter.USER_API_QUEUE;
                String rucEmisor = null;
                resp = documentsSummaryService.processSummaryTicket(objectMapper.readValue(msg.getBody(), String.class), userName, rucEmisor);

                amazonSQSClient.deleteMessage(processSummary, msg.getReceiptHandle());
                System.out.println("✅ Mensaje eliminado de la cola");

            } catch (Exception e) {
                log.error("❌ ERROR ❌: {}", e.getMessage());
                // No llamamos deleteMessage → el mensaje reaparecerá después del visibility timeout
            }
        }
    }



}
