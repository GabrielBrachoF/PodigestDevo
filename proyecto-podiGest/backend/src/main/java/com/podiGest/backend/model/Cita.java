package com.podiGest.backend.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Cita {

    private String id; // Usaremos un UUID (String) para un ID único
    private String pacienteCedula; // Cédula del paciente (link a Usuario)
    private String especialistaCedula; // Cédula del especialista (link a Usuario)

    // Usamos LocalDateTime para tener fecha y hora juntas
    private LocalDateTime fechaHoraInicio;

    // Según el ERS, las citas duran 60 min
    private LocalDateTime fechaHoraFin;

    private String estado; // "PENDIENTE", "CONFIRMADA", "CANCELADA"
    private String descripcion;

    public Cita() {
        // Constructor vacío para Jackson (JSON)
    }


    // Constructor para crear una cita nueva
    public Cita(String id, String pacienteCedula, String especialistaCedula, LocalDateTime fechaHoraInicio, String descripcion) {
        this.id = id;
        this.pacienteCedula = pacienteCedula;
        this.especialistaCedula = especialistaCedula;
        this.fechaHoraInicio = fechaHoraInicio;
        // Regla de Negocio: 60 minutos por cita
        this.fechaHoraFin = fechaHoraInicio.plusMinutes(60);
        this.estado = "PENDIENTE";
        this.descripcion = (descripcion != null) ? descripcion : "";
    }
}