/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmsprimenum_service;

import java.util.Scanner;
import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

/**
 *
 * @author SlickleZ
 */

/* 
 * Concept:
 * - consume from persistence dest and produce to temp dest
 */

public class Main {
    @Resource(mappedName = "jms/ConnectionFactory")
    private static ConnectionFactory connectionFactory;
    @Resource(mappedName = "jms/TempQueue")
    private static Queue queue;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Connection conn = null;
        
        try {
            conn = connectionFactory.createConnection();
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer destConsumer = session.createConsumer(queue);
            
            TextListener destListener = new TextListener(session);
            destConsumer.setMessageListener(destListener);
            
            conn.start();
            
            String sentence  = "";
            Scanner uerListener = new Scanner(System.in);
            while(true) {
                System.out.println("Press q to quit ");
                sentence = uerListener.nextLine();
                if (sentence.equals("q")) {
                    break;
                }
            }
            
        } catch(JMSException e) {
            System.err.println("Exception occurred: " + e.toString());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch(JMSException e) {
                    System.err.println("Exception occurred when closing connection: "
                            + e.toString());
                }
            }
        }
    }
}
