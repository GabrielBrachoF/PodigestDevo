package com.podiGest.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.podiGest.backend.model.Cita;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CitaService {

    private List<Cita> listaCitas;
    private static final String CITAS_JSON_FILE = "citas.json";
    private final ObjectMapper mapper;

    // --- Lógica de Carga/Guardado (Adaptada de CrearUsuarioService) ---

    public CitaService() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule()); // ¡Importante para LocalDateTime!
        this.listaCitas = cargarCitasDesdeJson(CITAS_JSON_FILE);
    }

    private List<Cita> cargarCitasDesdeJson(String fileName) {
        Path path = resolveWritablePath(fileName);
        try {
            if (Files.exists(path) && Files.size(path) > 0) {
                return mapper.readValue(path.toFile(), new TypeReference<List<Cita>>() {
                });
            }
        } catch (IOException e) {
            System.err.println("ERROR: No se pudo leer el archivo JSON en: " + path);
        }
        List<Cita> citas = cargarDesdeClasspath(fileName);
        if (!citas.isEmpty()) {
            try {
                guardarCitasAJson(citas, fileName);
            } catch (IOException e) {
                System.err.println("ERROR: No se pudo inicializar el archivo JSON en: " + path);
            }
        }
        return citas;
    }

    private List<Cita> cargarDesdeClasspath(String resourceName) {
        try (InputStream inputStream = new ClassPathResource(resourceName).getInputStream()) {
            return mapper.readValue(inputStream, new TypeReference<List<Cita>>() {
            });
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void guardarCitasAJson(List<Cita> citas, String fileName) throws IOException {
        Path path = resolveWritablePath(fileName);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), citas);
    }

    private Path resolveWritablePath(String fileName) {
        // (Este método es idéntico al de CrearUsuarioService)
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        if (workingDir.getFileName() != null && workingDir.getFileName().toString().equals("backend")) {
            return workingDir.resolve("src").resolve("main").resolve("resources").resolve(fileName);
        }
        Path backendDir = workingDir.resolve("backend");
        if (Files.exists(backendDir)) {
            return backendDir.resolve("src").resolve("main").resolve("resources").resolve(fileName);
        }
        return workingDir.resolve(fileName);
    }

       // --- Lógica de Negocio (Lo que implementa el Sprint 1) ---

    // (Añadiremos los métodos para HU 1.1.3, 1.1.4, 1.1.2 aquí)

       //* Metodo para obtener todas las citas (para pruebas)
    public List<Cita> obtenerTodasLasCitas() {
        return listaCitas;
    }

    // Metodo para obtener citas de un especialista en un día específico
    public List<Cita> obtenerCitasPorEspecialistaYFecha(String especialistaCedula, LocalDate fecha) {
        return listaCitas.stream()
                .filter(cita -> cita.getEspecialistaCedula().equals(especialistaCedula) &&
                        cita.getFechaHoraInicio().toLocalDate().equals(fecha))
                .collect(Collectors.toList());
    }


    private static final LocalTime HORA_INICIO_CLINICA = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN_CLINICA = LocalTime.of(20, 0);
    private static final int DURACION_CITA_MINUTOS = 60;


    /**
     * HU 1.1.3: Consultar citas disponibles
     * Genera los slots de 1 hora (de 8am a 7pm) y filtra los que ya están ocupados.
     */
    public List<LocalTime> obtenerHorariosDisponibles(String especialistaCedula, LocalDate fecha) {

        // 1. Obtener todas las citas YA AGENDADAS para ese especialista ese día
        List<Cita> citasExistentes = obtenerCitasPorEspecialistaYFecha(especialistaCedula, fecha);

        // 2. Extraer las horas de inicio de esas citas
        List<LocalTime> horasOcupadas = citasExistentes.stream()
                .map(cita -> cita.getFechaHoraInicio().toLocalTime())
                .collect(Collectors.toList());

        // 3. Generar todos los slots posibles del día
        List<LocalTime> todosLosSlots = new ArrayList<>();
        LocalTime slotActual = HORA_INICIO_CLINICA;

        // Genera slots de 8:00, 9:00, ... hasta las 19:00 (la última cita)
        while (slotActual.isBefore(HORA_FIN_CLINICA)) {
            todosLosSlots.add(slotActual);
            slotActual = slotActual.plusMinutes(DURACION_CITA_MINUTOS);
        }

        // 4. Filtrar: Devolver solo los slots que NO estén en la lista de horasOcupadas
        return todosLosSlots.stream()
                .filter(slot -> !horasOcupadas.contains(slot))
                .collect(Collectors.toList());
    }

    public Cita agendarCita(String pacienteCedula, String especialistaCedula, java.time.LocalDateTime fechaHoraInicio, String descripcion) throws IOException {
        Cita nuevaCita = new Cita(
                UUID.randomUUID().toString(),
                pacienteCedula,
                especialistaCedula,
                fechaHoraInicio,
                descripcion
        );
        listaCitas.add(nuevaCita);
        guardarCitasAJson(listaCitas, CITAS_JSON_FILE);
        return nuevaCita;
    }
}