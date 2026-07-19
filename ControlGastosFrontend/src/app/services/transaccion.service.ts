import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { Transaccion } from '../models';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class TransaccionService {
    private apiUrl = `${environment.apiUrl}/api/transacciones`;

    constructor(private http: HttpClient, private auth: AuthService) {}

    listarPorUsuario(): Observable<Transaccion[]> {
        return this.auth.getUsuarioId().pipe(
            switchMap(id => this.http.get<Transaccion[]>(`${this.apiUrl}/usuario/${id}`)
                .pipe(catchError(this.handleError)))
        );
    }

    buscarPorId(id: number): Observable<Transaccion> {
        return this.http.get<Transaccion>(`${this.apiUrl}/${id}`).pipe(catchError(this.handleError));
    }

    crear(data: Transaccion): Observable<Transaccion> {
        return this.http.post<Transaccion>(this.apiUrl, data).pipe(catchError(this.handleError));
    }

    actualizar(id: number, data: Transaccion): Observable<Transaccion> {
        return this.http.put<Transaccion>(`${this.apiUrl}/${id}`, data).pipe(catchError(this.handleError));
    }

    verificarRiesgo(data: any): Observable<any> {
        return this.http.post(`${this.apiUrl}/verificar-riesgo`, data).pipe(catchError(this.handleError));
    }

    listarProgramadas(usuarioId: number): Observable<Transaccion[]> {
        return this.http.get<Transaccion[]>(`${this.apiUrl}/programadas/${usuarioId}`).pipe(catchError(this.handleError));
    }

    crearProgramada(data: Transaccion): Observable<Transaccion> {
        return this.http.post<Transaccion>(`${this.apiUrl}/programadas`, data).pipe(catchError(this.handleError));
    }

    eliminar(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`)
            .pipe(catchError(this.handleError));
    }

    private handleError(error: HttpErrorResponse) {
        const msg = error.error?.error || error.error?.message || error.message || 'Error desconocido';
        return throwError(() => new Error(msg));
    }
}
