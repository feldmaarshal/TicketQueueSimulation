package ru.spbstu.telematics.java;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TicketQueueSimulation {

    public static class TicketMachine {
        private final int MAX_TICKET;
        private final AtomicInteger currentTicket;

        public TicketMachine(int maxTicket) {
            this.MAX_TICKET = maxTicket;
            this.currentTicket = new AtomicInteger(1);
        }

        public synchronized int getTicket() {
            return currentTicket.getAndUpdate(ticket -> (ticket % MAX_TICKET) + 1);
        }
    }

    public static class ServiceDisplay {
        private final int MAX_TICKET;
        private final AtomicInteger currentDisplay;

        public ServiceDisplay(int maxTicket) {
            this.MAX_TICKET = maxTicket;
            this.currentDisplay = new AtomicInteger(1);
        }

        public synchronized int getCurrentDisplay() {
            return currentDisplay.get();
        }

        public synchronized void next() {
            currentDisplay.updateAndGet(disp -> (disp % MAX_TICKET) + 1);
        }
    }

    public static class Customer implements Runnable {
        private final TicketMachine ticketMachine;
        private final ServiceDisplay serviceDisplay;
        private final AtomicInteger servedCustomers;
        private final int totalCustomers;

        public Customer(TicketMachine ticketMachine, ServiceDisplay serviceDisplay, AtomicInteger servedCustomers, int totalCustomers) {
            this.ticketMachine = ticketMachine;
            this.serviceDisplay = serviceDisplay;
            this.servedCustomers = servedCustomers;
            this.totalCustomers = totalCustomers;
        }

        @Override
        public void run() {
            int ticket = ticketMachine.getTicket();
            System.out.println(Thread.currentThread().getName() + " received ticket " + ticket);

            // Ожидание своей очереди
            while (serviceDisplay.getCurrentDisplay() != ticket) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            System.out.println(Thread.currentThread().getName() + " is being served with ticket " + ticket);

            // Симуляция времени обслуживания
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(500, 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.println(Thread.currentThread().getName() + " service completed for ticket " + ticket);
            serviceDisplay.next();

            // Увеличение количества обслуженных клиентов
            servedCustomers.incrementAndGet();
        }
    }

    // Добавляем метод для удобства тестирования
    public static int runSimulation(int maxTickets, int totalCustomers, int poolSize) {
        TicketMachine ticketMachine = new TicketMachine(maxTickets);
        ServiceDisplay serviceDisplay = new ServiceDisplay(maxTickets);
        AtomicInteger servedCustomers = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

        for (int i = 0; i < totalCustomers; i++) {
            executor.execute(new Customer(ticketMachine, serviceDisplay, servedCustomers, totalCustomers));
        }

        executor.shutdown();
        try {
            while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                if (servedCustomers.get() == totalCustomers) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("All customers have been served.");
        return servedCustomers.get();
    }

    public static void main(String[] args) {
        int served = runSimulation(10, 15, 5);
        System.out.println("Served: " + served);
    }
}
