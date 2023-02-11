/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmsprimenum_client;

import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.MessageProducer;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 *
 * @author SlickleZ
 */

/*
 * Concept:
 * - produce to persistence dest and consume from temp dest
 */
public class Main {
    @Resource(mappedName = "jms/ConnectionFactory")
    private static ConnectionFactory connectionFactory;
    @Resource(mappedName = "jms/TempQueue")
    private static Queue queue;
    
    public static void main(String[] args) {
        Connection conn = null;
        
        try {
            conn = connectionFactory.createConnection();
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            Queue tempDest = session.createTemporaryQueue();
            
            /* consume ont temp dest */
            TextListener tempDestListener = new TextListener();
            MessageConsumer tempDestConsumer = session.createConsumer(tempDest);
            tempDestConsumer.setMessageListener(tempDestListener);
            
            /* produce to persistence dest */
            MessageProducer destProducer = session.createProducer(queue);
            
            conn.start();
            
            String sentence = "";
            Scanner userListener = new  Scanner(System.in);
            
            while(true) {
                System.out.println("Enter two numbers. Use ',' to seperate each numbers. "
                        + "To end the program, press <enter>");
                
                sentence = userListener.nextLine();
                if(sentence.equals("")) { break; }
                
                if(Pattern.matches("[0-9]*,[0-9]*", sentence)) {
                    TextMessage messageToDest = session.createTextMessage(sentence);
                    messageToDest.setJMSReplyTo(tempDest);
                    messageToDest.setJMSCorrelationID(UUID.randomUUID().toString());
                    
                    System.out.println("Sending message: " + messageToDest.getText()
                                + " [" + messageToDest.getJMSCorrelationID() + "]");
                    destProducer.send(messageToDest);
                } else {
                    System.err.println("Cannot send! Because input does not match the pattern, "
                            + "Please try again.");
                }
            }
            
         } catch (JMSException e) {
            System.err.println("Exception occurred: " + e.toString());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (JMSException e) {
                }
            }
        }
    }
}
