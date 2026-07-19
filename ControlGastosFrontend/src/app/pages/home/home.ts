import { Component, OnInit } from '@angular/core';
import { ResumenTotalesComponent } from '../../components/resumen-totales/resumen-totales';
import { TarjetaGastoComponent } from '../../components/tarjeta-gasto/tarjeta-gasto';
import { TransaccionService } from '../../services/transaccion.service';
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

  constructor(private service: TransaccionService) {}

  ngOnInit() {
    this.service.listarPorUsuario().subscribe({
      next: (data) => {
        this.transacciones = data;
        this.transaccionesRecientes = data.slice(0, 5);
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
}
