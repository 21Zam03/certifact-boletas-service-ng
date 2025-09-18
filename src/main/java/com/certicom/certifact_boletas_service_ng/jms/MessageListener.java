package com.certicom.certifact_boletas_service_ng.jms;

import com.certicom.certifact_boletas_service_ng.dto.others.ResponsePSE;
import com.certicom.certifact_boletas_service_ng.service.DocumentsSummaryService;
import com.certicom.certifact_boletas_service_ng.util.ConstantesParameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageListener {

    private final DocumentsSummaryService documentsSummaryService;
    private final MessageProducer messageProducer;

    @JmsListener(destination = "${apifact.aws.sqs.processSummary}")
    public void receiveMessageQueueProcessSummary(@Payload final Message<String> message) {
        log.info("Se recibio mensaje Para Procesar resumen diario >>>>>>>>>>>>>>>>>> " + message);

        ResponsePSE resp = new ResponsePSE();
        try {
            String userName = ConstantesParameter.USER_API_QUEUE;
            String rucEmisor = null;
            resp = documentsSummaryService.processSummaryTicket(message.getPayload(), userName, rucEmisor);

            if (resp != null && !resp.getEstado() && resp.getRespuesta().equals(
                    ConstantesParameter.STATE_SUMMARY_VOIDED_DOCUMENTS_IN_PROCESO)) {

                if (resp.getIntentosGetStatus() < 5) {
                    log.info("El ticket sigue en proceso, reenviando getstatus a la cola {}", message.getPayload());
                    messageProducer.produceProcessSummary(message.getPayload(), rucEmisor);
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

}
