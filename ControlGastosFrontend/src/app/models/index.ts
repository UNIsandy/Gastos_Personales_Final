export interface Transaccion {
    id?: number;
    descripcion: string;
    monto: number;
    fecha: string;
    tipo: 'INGRESO' | 'GASTO';
    categoria?: Categoria;
    usuario?: Usuario;
    rutaComprobante?: string;
}

export interface Categoria {
    id?: number;
    nombre: string;
}

export interface Usuario {
    id?: number;
    nombre: string;
    email: string;
    password?: string;
    edad?: number;
}

export interface MetaAhorro {
    id?: number;
    nombre: string;
    montoObjetivo: number;
    montoActual: number;
    fechaLimite: string;
    activa: boolean;
    progreso?: number;
    nivel?: string;
}

export interface LoginResponse {
    token: string;
}
