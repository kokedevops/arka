package com.arka.arka.infrastructure.scheduler;

import com.arka.arka.domain.model.Cart;
import com.arka.arka.domain.port.in.CartUseCase;
import com.arka.arka.domain.port.out.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduler for automated e-commerce tasks
 * Handles abandoned cart detection and notifications
 */
@Component
public class EcommerceScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(EcommerceScheduler.class);
    
    private final CartUseCase cartUseCase;
    private final NotificationPort notificationPort;
    
    public EcommerceScheduler(CartUseCase cartUseCase, NotificationPort notificationPort) {
        this.cartUseCase = cartUseCase;
        this.notificationPort = notificationPort;
    }
    
    /**
     * Process abandoned carts every hour
     * Sends reminder emails to customers with abandoned carts
     */
    @Scheduled(fixedRate = 3600000) // Every hour (3,600,000 ms)
    public void processAbandonedCarts() {
        logger.info("🔄 Processing abandoned carts...");
        
        try {
            List<Cart> abandonedCarts = cartUseCase.getAbandonedCarts();
            
            if (abandonedCarts.isEmpty()) {
                logger.info("✅ No abandoned carts found");
                return;
            }
            
            logger.info("📧 Found {} abandoned carts, sending reminders...", abandonedCarts.size());
            
            int emailsSent = 0;
            for (Cart cart : abandonedCarts) {
                if (cart.getCliente() != null && 
                    cart.getCliente().getEmail() != null && 
                    cart.getCliente().isValidEmail()) {
                    
                    notificationPort.sendAbandonedCartReminder(
                        cart.getCliente().getEmail(),
                        cart.getCliente().getNombre(),
                        cart.getId()
                    );
                    emailsSent++;
                    
                    // Small delay to avoid overwhelming email service
                    Thread.sleep(200);
                }
            }
            
            logger.info("✅ Sent {} abandoned cart reminder emails", emailsSent);
            
        } catch (Exception e) {
            logger.error("❌ Error processing abandoned carts: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Demo method - process abandoned carts immediately (for testing)
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes for demo purposes
    public void processAbandonedCartsDemo() {
        logger.info("🚀 DEMO: Checking for abandoned carts...");
        
        try {
            List<Cart> abandonedCarts = cartUseCase.getAbandonedCarts();
            
            if (!abandonedCarts.isEmpty()) {
                logger.info("🛒 DEMO: Found {} abandoned carts", abandonedCarts.size());
                
                // Just log for demo, don't send emails every 5 minutes
                for (Cart cart : abandonedCarts) {
                    if (cart.getCliente() != null) {
                        logger.info("📋 DEMO: Abandoned cart ID {} belongs to customer: {}", 
                                  cart.getId(), cart.getCliente().getNombre());
                    }
                }
            } else {
                logger.info("✅ DEMO: No abandoned carts at the moment");
            }
            
        } catch (Exception e) {
            logger.error("❌ DEMO: Error checking abandoned carts: {}", e.getMessage());
        }
    }
}
