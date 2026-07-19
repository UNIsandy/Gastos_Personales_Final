import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { MetaAhorro } from '../models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class MetasService {
    private apiUrl = `${environment.apiUrl}/api/metas`;

    constructor(private http: HttpClient, private auth: AuthService) {}

    listar(): Observable<MetaAhorro[]> {
        return this.auth.getUsuarioId().pipe(
            switchMap(id => this.http.get<MetaAhorro[]>(`${this.apiUrl}/usuario/${id}`)
                .pipe(catchError(this.handleError)))
        );
    }

    crear(meta: Partial<MetaAhorro>): Observable<MetaAhorro> {
        return this.auth.getUsuarioId().pipe(
            switchMap(id => this.http.post<MetaAhorro>(this.apiUrl, { ...meta, usuario: { id } })
                .pipe(catchError(this.handleError)))
        );
    }

    actualizar(id: number, meta: Partial<MetaAhorro>): Observable<MetaAhorro> {
        const { nombre, montoObjetivo, fechaLimite } = meta;
        return this.http.put<MetaAhorro>(`${this.apiUrl}/${id}`, { nombre, montoObjetivo, fechaLimite })
            .pipe(catchError(this.handleError));
    }

    aportar(id: number, monto: number): Observable<MetaAhorro> {
        return this.http.post<MetaAhorro>(`${this.apiUrl}/${id}/aportar`, { monto })
            .pipe(catchError(this.handleError));
    }

    reactivar(id: number, nuevaFechaLimite: string, aporteInicial: number): Observable<MetaAhorro> {
        return this.http.post<MetaAhorro>(`${this.apiUrl}/${id}/reactivar`, { nuevaFechaLimite, aporteInicial })
            .pipe(catchError(this.handleError));
    }

    eliminar(id: number): Observable<string> {
        return this.http.delete<string>(`${this.apiUrl}/${id}`, { responseType: 'text' as 'json' })
            .pipe(catchError(this.handleError));
    }

    private handleError(error: HttpErrorResponse) {
        const msg = error.error?.error || error.error?.message || error.message || 'Error desconocido';
        return throwError(() => new Error(msg));
    }
}
