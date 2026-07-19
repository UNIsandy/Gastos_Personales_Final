import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { MetaAhorro } from '../models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class MetasService {
    private apiUrl = `${environment.apiUrl}/api/metas`;

    constructor(private http: HttpClient, private auth: AuthService) {}

    listar(): Observable<MetaAhorro[]> {
        return this.auth.getUsuarioId().pipe(
            switchMap(id => this.http.get<MetaAhorro[]>(`${this.apiUrl}/usuario/${id}`))
        );
    }

    crear(meta: Partial<MetaAhorro>): Observable<MetaAhorro> {
        return this.auth.getUsuarioId().pipe(
            switchMap(id => this.http.post<MetaAhorro>(this.apiUrl, { ...meta, usuario: { id } }))
        );
    }

    actualizar(id: number, meta: Partial<MetaAhorro>): Observable<MetaAhorro> {
        const { nombre, montoObjetivo, fechaLimite } = meta;
        return this.http.put<MetaAhorro>(`${this.apiUrl}/${id}`, { nombre, montoObjetivo, fechaLimite });
    }

    aportar(id: number, monto: number): Observable<MetaAhorro> {
        return this.http.post<MetaAhorro>(`${this.apiUrl}/${id}/aportar`, { monto });
    }

    eliminar(id: number): Observable<string> {
        return this.http.delete<string>(`${this.apiUrl}/${id}`, { responseType: 'text' as 'json' });
    }
}
