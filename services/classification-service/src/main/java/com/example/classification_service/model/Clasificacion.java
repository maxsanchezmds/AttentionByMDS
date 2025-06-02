package com.example.classification_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.Lob; // Para textos que pueden superar los 255 caracteres


@Entity // Marca esta clase como una entidad JPA
@Table(name = "nombre") // Define el nombre de la tabla en la base de datos
@Data // Genera getters y setters automáticamente
@Builder // Permite crear objetos de forma más sencilla y menos propensa a errores
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
public class Clasificacion {

    @Id // convierte al atributo en PK
    @GeneratedValue(strategy = GenerationType.IDENTITY)  //El siguiente atributo es auto incremental +1 por cada registro
    private Long id;

    @Column(name = "fecha", nullable = false, unique = false)  //Condiciones de la columna
    private LocalDateTime fecha;

    @Column(name = "idEmpresa", nullable = false, unique = false, precision = 10, scale = 2)  //Condiciones de la columna
    private Long idEmpresa;

    @Column(name = "idCliente", nullable = false, unique = true, precision = 10, scale = 2)  //Condiciones de la columna
    private Long idCliente;

    @Column(name = "idConversacion", nullable = false, unique = true, length = 255)  //Condiciones de la columna
    private Long idConversacion;

    @Column(name = "clasificacion", nullable = true, unique = false, length = 255)  //Condiciones de la columna
    private String clasificacion;

    @Lob
    private String descripcion;
}