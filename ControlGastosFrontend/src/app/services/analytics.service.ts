import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

export interface DashboardData {
    totalIngresos: number;
    totalGastos: number;
    balance: number;
    gastosPorCategoria: { [key: string]: number };
    prediccionProximoMes: number;
    gastoMesActual: number;
    tendencia: string;
    gastosPorMes: { [key: number]: number };
    ingresosPorMes: { [key: number]: number };
    consejos: Consejo[];
}

export interface Presupuesto {
    id?: number;
    montoLimite: number;
    mes: number;
    anio: number;
}

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
    private apiUrl = `${environment.apiUrl}/api/analytics`;
    private presupuestoUrl = `${environment.apiUrl}/api/presupuestos`;

    constructor(private http: HttpClient, private auth: AuthService) {}

    getDashboard(): Observable<DashboardData> {
        return this.auth.getUsuarioId().pipe(
            switchMap(id => {
                if (!id) throw new Error('Usuario no autenticado');
                return this.http.get<DashboardData>(`${this.apiUrl}/dashboard/${id}`)
                    .pipe(catchError(() => throwError(() => new Error('Error al cargar dashboard'))));
            })
        );
    }

    getPresupuestos(): Observable<Presupuesto[]> {
        return this.auth.getUsuarioId().pipe(
            switchMap(id => this.http.get<Presupuesto[]>(`${this.presupuestoUrl}/usuario/${id}`)
                .pipe(catchError(() => throwError(() => new Error('Error al cargar presupuestos')))))
        );
    }
}

export interface Consejo {
    titulo: string;
    descripcion: string;
    tipo: string;
    icono: string;
    prioridad: number;
}