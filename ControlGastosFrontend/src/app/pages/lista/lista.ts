import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TarjetaGastoComponent } from '../../components/tarjeta-gasto/tarjeta-gasto';
import { TransaccionService } from '../../services/transaccion.service';
import { Transaccion } from '../../models';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-lista',
  imports: [FormsModule, RouterLink, TarjetaGastoComponent, CommonModule],
  templateUrl: './lista.html',
  styleUrl: './lista.scss'
})
export class ListaComponent implements OnInit {
  transacciones: Transaccion[] = [];
  error = '';
  filtroTipo = '';

  private http = inject(HttpClient);

  constructor(private service: TransaccionService, private notificaciones: NotificationService) {}

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.service.listarPorUsuario().subscribe({
      next: (data) => this.transacciones = data,
      error: (err) => this.error = err.message
    });
  }

  get transaccionesFiltradas() {
    if (!this.filtroTipo) return this.transacciones;
    return this.transacciones.filter(t => t.tipo === this.filtroTipo);
  }

  async onEliminar(id: number) {
    const confirmado = await this.notificaciones.confirmar(
      '¿Está seguro de eliminar esta transacción?',
      { titulo: 'Eliminar transacción', textoConfirmar: 'Eliminar', peligroso: true }
    );
    if (!confirmado) return;

    this.service.eliminar(id).subscribe({
      next: () => {
        this.cargar();
        this.notificaciones.exito('Se eliminó exitosamente');
      },
      error: (err) => {
        this.error = err.message;
        this.notificaciones.error(err.message || 'Error al eliminar la transacción');
      }
    });
  }

  descargarPDF() {
    const userData = localStorage.getItem('user_data');
    if (!userData) { this.error = 'No autenticado'; return; }
    const usuarioId = JSON.parse(userData).id;
    if (!usuarioId) { this.error = 'Usuario no identificado'; return; }
    const hoy = new Date();
    const mes = hoy.getMonth() + 1;
    const anio = hoy.getFullYear();
    this.http.get(`http://localhost:8080/api/reportes/mensual/${usuarioId}/${mes}/${anio}`, {
      responseType: 'blob'
    }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `reporte_${mes}_${anio}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => this.error = err.error?.error || 'Error al descargar PDF'
    });
  }
}