import { Component, OnInit, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, registerables } from 'chart.js';
import { AnalyticsService, DashboardData, Presupuesto } from '../../services/analytics.service';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class DashboardComponent implements OnInit {
  @ViewChild('pieCanvas') pieCanvas!: ElementRef;
  @ViewChild('barCanvas') barCanvas!: ElementRef;
  @ViewChild('lineCanvas') lineCanvas!: ElementRef;

  data: DashboardData | null = null;
  presupuestos: Presupuesto[] = [];
  error = '';
  cargando = true;
  alertas: { tipo: string; mensaje: string }[] = [];

  meses = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
  colores = ['#38BDF8', '#A855F7', '#7C3AED', '#22D3EE', '#8B5CF6', '#67E8F9', '#C084FC', '#2DD4BF'];

  constructor(private service: AnalyticsService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.service.getDashboard().subscribe({
      next: (d) => {
        this.data = d;
        this.cargando = false;
        this.generarAlertas();
        this.cdr.detectChanges();
        this.crearGraficos();
      },
      error: (err) => { this.error = err.message; this.cargando = false; }
    });

    this.service.getPresupuestos().subscribe({
      next: (p) => this.presupuestos = p,
      error: () => {}
    });
  }

  private generarAlertas() {
    if (!this.data) return;
    const mes = new Date().getMonth() + 1;
    const anio = new Date().getFullYear();
    const presupuesto = this.presupuestos.find(p => p.mes === mes && p.anio === anio);

    if (presupuesto && this.data.gastoMesActual > 0 && presupuesto.montoLimite > 0) {
      const pct = Math.round((this.data.gastoMesActual / presupuesto.montoLimite) * 100);
      if (pct >= 100) {
        this.alertas.push({ tipo: 'peligro', mensaje: ` Has superado tu presupuesto mensual (${pct}%)` });
      } else if (pct >= 80) {
        this.alertas.push({ tipo: 'advertencia', mensaje: ` Cuidado: has gastado el ${pct}% de tu presupuesto` });
      }
    }

    if (this.data.tendencia === '😬') {
      this.alertas.push({ tipo: 'info', mensaje: ' La predicción del próximo mes es mayor a tus gastos actuales' });
    }
  }

  private crearGraficos() {
    if (!this.data) return;

    this.crearDoughnut();
    this.crearBarras();
    this.crearLineas();
  }

  private crearDoughnut() {
    if (!this.pieCanvas?.nativeElement) return;
    const categorias = Object.entries(this.data!.gastosPorCategoria);
    if (categorias.length === 0) return;

    new Chart(this.pieCanvas.nativeElement, {
      type: 'doughnut',
      data: {
        labels: categorias.map(([k]) => k),
        datasets: [{
          data: categorias.map(([, v]) => v),
          backgroundColor: this.colores.slice(0, categorias.length),
          borderWidth: 2, borderColor: '#1A2238'
        }]
      },
      options: {
        responsive: true,
        animation: { animateRotate: true, duration: 1000 },
        plugins: {
          legend: {
            position: 'bottom',
            labels: { color: '#FFFFFF', font: { size: 12 } }
          },
          tooltip: {
            bodyColor: '#FFFFFF',
            titleColor: '#A7B1C2',
            backgroundColor: '#1A2238',
            borderColor: '#2A344A',
            borderWidth: 1
          }
        }
      }
    });
  }

  private crearBarras() {
    if (!this.barCanvas?.nativeElement) return;
    const categorias = Object.entries(this.data!.gastosPorCategoria);
    if (categorias.length === 0) return;

    new Chart(this.barCanvas.nativeElement, {
      type: 'bar',
      data: {
        labels: categorias.map(([k]) => k),
        datasets: [{
          label: 'Gastos',
          data: categorias.map(([, v]) => v),
          backgroundColor: this.colores.slice(0, categorias.length),
          borderRadius: 6
        }]
      },
      options: {
        responsive: true,
        animation: { duration: 800, easing: 'easeOutBounce' },
        scales: {
          x: { ticks: { color: '#A7B1C2' }, grid: { color: 'rgba(255,255,255,0.05)' } },
          y: { beginAtZero: true, ticks: { color: '#A7B1C2', callback: v => 'S/ ' + v }, grid: { color: 'rgba(255,255,255,0.05)' } }
        },
        plugins: {
          legend: { display: false },
          tooltip: { bodyColor: '#FFFFFF', titleColor: '#A7B1C2', backgroundColor: '#1A2238', borderColor: '#2A344A', borderWidth: 1 }
        }
      }
    });
  }

  private crearLineas() {
    if (!this.lineCanvas?.nativeElement) return;

    const gastos = this.data!.gastosPorMes || {};
    const ingresos = this.data!.ingresosPorMes || {};
    const labels = this.meses;

    const gastosData = labels.map((_, i) => gastos[i + 1] || 0);
    const ingresosData = labels.map((_, i) => ingresos[i + 1] || 0);

    new Chart(this.lineCanvas.nativeElement, {
      type: 'line',
      data: {
        labels,
        datasets: [
          {
            label: 'Ingresos',
            data: ingresosData,
            borderColor: '#38BDF8',
            backgroundColor: 'rgba(56, 189, 248, 0.1)',
            fill: true,
            tension: 0.4,
            pointRadius: 4
          },
          {
            label: 'Gastos',
            data: gastosData,
            borderColor: '#A855F7',
            backgroundColor: 'rgba(168, 85, 247, 0.1)',
            fill: true,
            tension: 0.4,
            pointRadius: 4
          }
        ]
      },
      options: {
        responsive: true,
        animation: { duration: 1200, easing: 'easeInOutQuart' },
        interaction: { mode: 'index', intersect: false },
        scales: {
          x: { ticks: { color: '#A7B1C2' }, grid: { color: 'rgba(255,255,255,0.05)' } },
          y: { beginAtZero: true, ticks: { color: '#A7B1C2', callback: v => 'S/ ' + v }, grid: { color: 'rgba(255,255,255,0.05)' } }
        },
        plugins: {
          legend: { labels: { color: '#FFFFFF', font: { size: 12 } } },
          tooltip: { bodyColor: '#FFFFFF', titleColor: '#A7B1C2', backgroundColor: '#1A2238', borderColor: '#2A344A', borderWidth: 1 }
        }
      }
    });
  }
}
