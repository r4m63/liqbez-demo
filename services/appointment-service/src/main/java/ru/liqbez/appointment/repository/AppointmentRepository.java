package ru.liqbez.appointment.repository;

import org.springframework.stereotype.Repository;

@Repository
public class AppointmentRepository {
    // Демо-запись на приём пока задана в памяти.
    public boolean exists(long id) {
        return id == 1;
    }
}
