/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmsprimenum_service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 *
 * @author SlickleZ
 */
public class TextListener implements MessageListener {
    private MessageProducer tempDestProducer;
    private Session session;

    public TextListener(Session session) {
        this.session = session;
        try {
            tempDestProducer = session.createProducer(null);
        } catch(JMSException e) {
            System.err.println("Exception occurred when creating producer"
                    + " in TextListener: " + e.toString());
        }
    }
    
    
    @Override
    public void onMessage(Message message) {
        TextMessage msg = null;
        
        try {
            if (message != null && message instanceof TextMessage) {
                msg = (TextMessage) message;
                System.out.println("Reading message: " + msg.getText()
                            + " [" + msg.getJMSCorrelationID() + "]");
                
                String[] splitText = msg.getText().split(",", 2);
                long lower = new Integer(splitText[0]);
                long upper = new Integer(splitText[1]);
                int amountPrimes = amountOfPrimeInterval(lower, upper);
                
                TextMessage messageToTempDest = session.createTextMessage("The number of primes between "
                        + lower + " and " + upper + " is " + amountPrimes);
                messageToTempDest.setJMSCorrelationID(message.getJMSCorrelationID());
                
                System.out.println("Sending message: " + messageToTempDest.getText());
                tempDestProducer.send(message.getJMSReplyTo(), messageToTempDest);
            } else {
                System.err.println("Message is not a TextMessage");
            }

        } catch (JMSException e) {
            System.err.println("JMSException in onMessage(): " + e.toString());
        } catch (NumberFormatException t) {
            System.err.println("Exception in onMessage():" + t.getMessage());
        }
    }
    
    private int amountOfPrimeInterval(long lower, long upper) {
        int n = 0;
        
        for(; lower <= upper; lower++) {
            if(isPrime(lower)) { n++; }
        }
        
        return n;
    }
    
    private boolean isPrime(long n) {
        int i;
        for (i = 2; i*i <= n; i++) {
            if ((n % i) == 0) {
                return false;
            }
        }
        return true;
    }
    
}
