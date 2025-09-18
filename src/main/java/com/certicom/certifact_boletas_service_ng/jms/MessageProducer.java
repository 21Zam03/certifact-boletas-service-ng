package com.certicom.certifact_boletas_service_ng.jms;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import jakarta.annotation.Resource;
import com.amazon.sqs.javamessaging.SQSMessagingClientConstants;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Slf4j
@Component
public class MessageProducer {
/*
    @Resource
    protected JmsTemplate jmsTemplate;
*/
    @Resource
    ObjectMapper objectMapper;

    /*
    @Autowired
    EmailSendRepository emailSendRepository;

    @Autowired
    EmailService emailService;
    * */

    @Value("${apifact.aws.sqs.sendBill}")
    private String sendBill;

    @Value("${apifact.aws.sqs.processVoided}")
    private String processVoided;

    @Value("${apifact.aws.sqs.processSummary}")
    private String processSummary;

    @Value("${apifact.aws.sqs.otrosCpe}")
    private String otrosCpe;

    @Value("${apifact.aws.sqs.guiaRemision}")
    private String guiaRemision;

    @Value("${apifact.aws.sqs.getStatusCdr}")
    private String getStatusCdr;

    @Value("${apifact.aws.sqs.emailSenderVoided}")
    private String emailSenderVoided;

    @Value("${apifact.aws.sqs.emailSender}")
    private String emailSender;


    @Value("${apifact.aws.sqs.emailExcelSender}")
    private String emailExcelSender;
/*
    public void produceProcessSummary(String ticket, String rucEmisor) {
        try {
            send(processSummary, ticket);
            log.info("TICKET: {}",ticket);
        }catch (Exception e){
            log.error("ERROR: {}",e.getMessage());
        }
    }

    private <MESSAGE extends Serializable> void send(String queue, MESSAGE payload) {
        jmsTemplate.send(queue, new MessageCreator() {
            public jakarta.jms.Message createMessage(Session session) throws JMSException {
                try {
                    jakarta.jms.Message createMessage = session.createTextMessage(objectMapper.writeValueAsString(payload));
                    createMessage.setStringProperty(SQSMessagingClientConstants.JMSX_GROUP_ID, "messageGroup1");
                    createMessage.setStringProperty(SQSMessagingClientConstants.JMS_SQS_DEDUPLICATION_ID, "1" +
                            System.currentTimeMillis());
                    createMessage.setStringProperty("documentType", payload.getClass().getName());
                    System.out.println("SE ENVIO MENSAJE A LA COLA SQS GUIA");
                    return createMessage;
                } catch (Exception | Error e) {
                    log.error("Fail to send message {}", payload);
                    throw new RuntimeException(e);
                }
            }
        });

    }
*/
}
