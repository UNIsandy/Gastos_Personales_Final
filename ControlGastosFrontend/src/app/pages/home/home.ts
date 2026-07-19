import { Component, OnInit } from '@angular/core';
import { ResumenTotalesComponent } from '../../components/resumen-totales/resumen-totales';
import { TarjetaGastoComponent } from '../../components/tarjeta-gasto/tarjeta-gasto';
import { TransaccionService } from '../../services/transaccion.service';
import { NotificationService } from '../../services/notification.service';
import { Transaccion } from '../../models';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  imports: [ResumenTotalesComponent, TarjetaGastoComponent, CommonModule],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class HomeComponent implements OnInit {
  transacciones: Transaccion[] = [];
  transaccionesRecientes: Transaccion[] = [];
  error = '';

  constructor(private service: TransaccionService, private notificaciones: NotificationService) {}

  ngOnInit() {
    this.service.listarPorUsuario().subscribe({
      next: (data) => {
        this.transacciones = data;
        const ordenadas = [...data].sort((a, b) => b.fecha.localeCompare(a.fecha));
        this.transaccionesRecientes = ordenadas.slice(0, 5);
      },
      error: (err) => this.error = err.message
    });
  }

  get totalIngresos() {
    return this.transacciones
      .filter(t => t.tipo === 'INGRESO')
      .reduce((s, t) => s + t.monto, 0);
  }

  get totalGastos() {
    return this.transacciones
      .filter(t => t.tipo === 'GASTO')
      .reduce((s, t) => s + t.monto, 0);
  }

  async eliminar(id: number) {
    const confirmado = await this.notificaciones.confirmar(
      '¿Está seguro de eliminar esta transacción?',
      { titulo: 'Eliminar transacción', textoConfirmar: 'Eliminar', peligroso: true }
    );
    if (!confirmado) return;

    this.service.eliminar(id).subscribe({
      next: () => {
        this.transacciones = this.transacciones.filter(t => t.id !== id);
        this.transaccionesRecientes = this.transaccionesRecientes.filter(t => t.id !== id);
        this.notificaciones.exito('Transacción eliminada');
      },
      error: (err) => this.notificaciones.error(err.message)
    });
  }
}
