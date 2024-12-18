package ru.spbstu.telematics.java;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TicketQueueSimulationTest {
    
    @Test
    public void testQueueSimulation() {
        int maxTickets = 10;
        int totalCustomers = 15;
        int poolSize = 5;
        
        // Запускаем симуляцию
        int served = TicketQueueSimulation.runSimulation(maxTickets, totalCustomers, poolSize);
        
        // Проверяем, что все клиенты были обслужены
        assertEquals(totalCustomers, served, "All customers should have been served");
    }
}
