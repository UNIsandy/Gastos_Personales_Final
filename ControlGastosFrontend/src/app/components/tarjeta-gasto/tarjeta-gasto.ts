import { Component, Input, Output, EventEmitter } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-tarjeta-gasto',
  imports: [DecimalPipe, RouterLink],
  templateUrl: './tarjeta-gasto.html',
  styleUrl: './tarjeta-gasto.scss'
})
export class TarjetaGastoComponent {
  @Input() id: number = 0;
  @Input() tipo: 'INGRESO' | 'GASTO' = 'GASTO';
  @Input() monto: number = 0;
  @Input() descripcion: string = '';
  @Input() categoria: string = '';
  @Input() fecha: string = '';

  @Output() eliminar = new EventEmitter<number>();

  onEliminar() {
    this.eliminar.emit(this.id);
  }
}
