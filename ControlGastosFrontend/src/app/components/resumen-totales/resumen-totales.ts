import { Component, Input } from '@angular/core';
import { DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-resumen-totales',
  imports: [DecimalPipe],
  templateUrl: './resumen-totales.html',
  styleUrl: './resumen-totales.scss'
})
export class ResumenTotalesComponent {
  @Input() ingresos: number = 0;
  @Input() gastos: number = 0;

  get balance(): number {
    return this.ingresos - this.gastos;
  }
}
