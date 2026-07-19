package com.cgp.controlgasto.Repository;

import com.cgp.controlgasto.Model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE u.nombre = ?1")
    List<Usuario> buscarPorNombre(String nombre);

    @Query("SELECT u FROM Usuario u WHERE u.edad >= :edad")
    List<Usuario> buscarMayoresDe(@Param("edad") Integer edad);

    @Query("SELECT u FROM Usuario u WHERE u.nombre LIKE %:nombre%")
    List<Usuario> buscarPorNombreContiene(@Param("nombre") String nombre);

    @Query("SELECT u FROM Usuario u WHERE u.email LIKE %:email%")
    List<Usuario> buscarPorEmailContiene(@Param("email") String email);

    @Query("SELECT u FROM Usuario u WHERE u.edad BETWEEN :edadMin AND :edadMax")
    List<Usuario> buscarPorRangoEdad(@Param("edadMin") Integer edadMin, @Param("edadMax") Integer edadMax);
}