package com.example.pulseup.domain.repository

import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.NuevoPerfilFisico
import com.example.pulseup.domain.model.PerfilFisico

interface PerfilFisicoRepository {
    /** Éxito con null = el usuario aún no ha llenado su perfil. */
    suspend fun obtener(): Resultado<PerfilFisico?>

    /** Crea o reemplaza el perfil. Falla con el mensaje del backend (400) si algún dato no es válido. */
    suspend fun guardar(nuevo: NuevoPerfilFisico): Resultado<PerfilFisico>

    /** Borra el perfil. El backend responde bien aunque no existiera. */
    suspend fun eliminar(): Resultado<Unit>
}
